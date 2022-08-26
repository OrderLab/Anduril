/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.replication.regionserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.MetaTableAccessor;
import org.apache.hadoop.hbase.Server;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.replication.ReplicationException;
import org.apache.hadoop.hbase.replication.ReplicationListener;
import org.apache.hadoop.hbase.replication.ReplicationPeer;
import org.apache.hadoop.hbase.replication.ReplicationPeer.PeerState;
import org.apache.hadoop.hbase.replication.ReplicationPeers;
import org.apache.hadoop.hbase.replication.ReplicationQueueInfo;
import org.apache.hadoop.hbase.replication.ReplicationQueueStorage;
import org.apache.hadoop.hbase.replication.ReplicationTracker;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.hadoop.hbase.wal.AbstractFSWALProvider;
import org.apache.yetus.audience.InterfaceAudience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hbase.thirdparty.com.google.common.annotations.VisibleForTesting;
import org.apache.hbase.thirdparty.com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * This class is responsible to manage all the replication sources. There are two classes of
 * sources:
 * <ul>
 * <li>Normal sources are persistent and one per peer cluster</li>
 * <li>Old sources are recovered from a failed region server and our only goal is to finish
 * replicating the WAL queue it had</li>
 * </ul>
 * <p>
 * When a region server dies, this class uses a watcher to get notified and it tries to grab a lock
 * in order to transfer all the queues in a local old source.
 * <p>
 * Synchronization specification:
 * <ul>
 * <li>No need synchronized on {@link #sources}. {@link #sources} is a ConcurrentHashMap and there
 * is a Lock for peer id in {@link PeerProcedureHandlerImpl}. So there is no race for peer
 * operations.</li>
 * <li>Need synchronized on {@link #walsById}. There are four methods which modify it,
 * {@link #addPeer(String)}, {@link #removePeer(String)},
 * {@link #cleanOldLogs(SortedSet, String, String)} and {@link #preLogRoll(Path)}.
 * {@link #walsById} is a ConcurrentHashMap and there is a Lock for peer id in
 * {@link PeerProcedureHandlerImpl}. So there is no race between {@link #addPeer(String)} and
 * {@link #removePeer(String)}. {@link #cleanOldLogs(SortedSet, String, String)} is called by
 * {@link ReplicationSourceInterface}. So no race with {@link #addPeer(String)}.
 * {@link #removePeer(String)} will terminate the {@link ReplicationSourceInterface} firstly, then
 * remove the wals from {@link #walsById}. So no race with {@link #removePeer(String)}. The only
 * case need synchronized is {@link #cleanOldLogs(SortedSet, String, String)} and
 * {@link #preLogRoll(Path)}.</li>
 * <li>No need synchronized on {@link #walsByIdRecoveredQueues}. There are three methods which
 * modify it, {@link #removePeer(String)} , {@link #cleanOldLogs(SortedSet, String, String)} and
 * {@link ReplicationSourceManager.NodeFailoverWorker#run()}.
 * {@link #cleanOldLogs(SortedSet, String, String)} is called by {@link ReplicationSourceInterface}.
 * {@link #removePeer(String)} will terminate the {@link ReplicationSourceInterface} firstly, then
 * remove the wals from {@link #walsByIdRecoveredQueues}. And
 * {@link ReplicationSourceManager.NodeFailoverWorker#run()} will add the wals to
 * {@link #walsByIdRecoveredQueues} firstly, then start up a {@link ReplicationSourceInterface}. So
 * there is no race here. For {@link ReplicationSourceManager.NodeFailoverWorker#run()} and
 * {@link #removePeer(String)}, there is already synchronized on {@link #oldsources}. So no need
 * synchronized on {@link #walsByIdRecoveredQueues}.</li>
 * <li>Need synchronized on {@link #latestPaths} to avoid the new open source miss new log.</li>
 * <li>Need synchronized on {@link #oldsources} to avoid adding recovered source for the
 * to-be-removed peer.</li>
 * </ul>
 */
@InterfaceAudience.Private
public class ReplicationSourceManager implements ReplicationListener {
  private static final Logger LOG = LoggerFactory.getLogger(ReplicationSourceManager.class);
  // all the sources that read this RS's logs and every peer only has one replication source
  private final ConcurrentMap<String, ReplicationSourceInterface> sources;
  // List of all the sources we got from died RSs
  private final List<ReplicationSourceInterface> oldsources;
  private final ReplicationQueueStorage queueStorage;
  private final ReplicationTracker replicationTracker;
  private final ReplicationPeers replicationPeers;
  // UUID for this cluster
  private final UUID clusterId;
  // All about stopping
  private final Server server;

  // All logs we are currently tracking
  // Index structure of the map is: queue_id->logPrefix/logGroup->logs
  // For normal replication source, the peer id is same with the queue id
  private final ConcurrentMap<String, Map<String, SortedSet<String>>> walsById;
  // Logs for recovered sources we are currently tracking
  // the map is: queue_id->logPrefix/logGroup->logs
  // For recovered source, the queue id's format is peer_id-servername-*
  private final ConcurrentMap<String, Map<String, SortedSet<String>>> walsByIdRecoveredQueues;

  private final Configuration conf;
  private final FileSystem fs;
  // The paths to the latest log of each wal group, for new coming peers
  private final Set<Path> latestPaths;
  // Path to the wals directories
  private final Path logDir;
  // Path to the wal archive
  private final Path oldLogDir;
  private final WALFileLengthProvider walFileLengthProvider;
  // The number of ms that we wait before moving znodes, HBASE-3596
  private final long sleepBeforeFailover;
  // Homemade executer service for replication
  private final ThreadPoolExecutor executor;

  private final boolean replicationForBulkLoadDataEnabled;

  private Connection connection;
  private long replicationWaitTime;

  private AtomicLong totalBufferUsed = new AtomicLong();

  /**
   * Creates a replication manager and sets the watch on all the other registered region servers
   * @param queueStorage the interface for manipulating replication queues
   * @param replicationPeers
   * @param replicationTracker
   * @param conf the configuration to use
   * @param server the server for this region server
   * @param fs the file system to use
   * @param logDir the directory that contains all wal directories of live RSs
   * @param oldLogDir the directory where old logs are archived
   * @param clusterId
   */
  public ReplicationSourceManager(ReplicationQueueStorage queueStorage,
      ReplicationPeers replicationPeers, ReplicationTracker replicationTracker, Configuration conf,
      Server server, FileSystem fs, Path logDir, Path oldLogDir, UUID clusterId,
      WALFileLengthProvider walFileLengthProvider) throws IOException {
    // CopyOnWriteArrayList is thread-safe.
    // Generally, reading is more than modifying.
    this.sources = new ConcurrentHashMap<>();
    this.queueStorage = queueStorage;
    this.replicationPeers = replicationPeers;
    this.replicationTracker = replicationTracker;
    this.server = server;
    this.walsById = new ConcurrentHashMap<>();
    this.walsByIdRecoveredQueues = new ConcurrentHashMap<>();
    this.oldsources = new ArrayList<>();
    this.conf = conf;
    this.fs = fs;
    this.logDir = logDir;
    this.oldLogDir = oldLogDir;
    this.sleepBeforeFailover = conf.getLong("replication.sleep.before.failover", 30000); // 30
                                                                                         // seconds
    this.clusterId = clusterId;
    this.walFileLengthProvider = walFileLengthProvider;
    this.replicationTracker.registerListener(this);
    // It's preferable to failover 1 RS at a time, but with good zk servers
    // more could be processed at the same time.
    int nbWorkers = conf.getInt("replication.executor.workers", 1);
    // use a short 100ms sleep since this could be done inline with a RS startup
    // even if we fail, other region servers can take care of it
    this.executor = new ThreadPoolExecutor(nbWorkers, nbWorkers, 100, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>());
    ThreadFactoryBuilder tfb = new ThreadFactoryBuilder();
    tfb.setNameFormat("ReplicationExecutor-%d");
    tfb.setDaemon(true);
    this.executor.setThreadFactory(tfb.build());
    this.latestPaths = new HashSet<Path>();
    replicationForBulkLoadDataEnabled = conf.getBoolean(HConstants.REPLICATION_BULKLOAD_ENABLE_KEY,
      HConstants.REPLICATION_BULKLOAD_ENABLE_DEFAULT);
    this.replicationWaitTime = conf.getLong(HConstants.REPLICATION_SERIALLY_WAITING_KEY,
      HConstants.REPLICATION_SERIALLY_WAITING_DEFAULT);
    connection = ConnectionFactory.createConnection(conf);
  }

  /**
   * Adds a normal source per registered peer cluster and tries to process all old region server wal
   * queues
   * <p>
   * The returned future is for adoptAbandonedQueues task.
   */
  Future<?> init() throws IOException {
    for (String id : this.replicationPeers.getAllPeerIds()) {
      addSource(id);
      if (replicationForBulkLoadDataEnabled) {
        // Check if peer exists in hfile-refs queue, if not add it. This can happen in the case
        // when a peer was added before replication for bulk loaded data was enabled.
        throwIOExceptionWhenFail(() -> this.queueStorage.addPeerToHFileRefs(id));
      }
    }
    return this.executor.submit(this::adoptAbandonedQueues);
  }

  private void adoptAbandonedQueues() {
    List<ServerName> currentReplicators = null;
    try {
      currentReplicators = queueStorage.getListOfReplicators();
    } catch (ReplicationException e) {
      server.abort("Failed to get all replicators", e);
      return;
    }
    if (currentReplicators == null || currentReplicators.isEmpty()) {
      return;
    }
    List<ServerName> otherRegionServers = replicationTracker.getListOfRegionServers().stream()
        .map(ServerName::valueOf).collect(Collectors.toList());
    LOG.info(
      "Current list of replicators: " + currentReplicators + " other RSs: " + otherRegionServers);

    // Look if there's anything to process after a restart
    for (ServerName rs : currentReplicators) {
      if (!otherRegionServers.contains(rs)) {
        transferQueues(rs);
      }
    }
  }

  /**
   * 1. Add peer to replicationPeers 2. Add the normal source and related replication queue 3. Add
   * HFile Refs
   * @param peerId the id of replication peer
   */
  public void addPeer(String peerId) throws IOException {
    boolean added = false;
    try {
      added = this.replicationPeers.addPeer(peerId);
    } catch (ReplicationException e) {
      throw new IOException(e);
    }
    if (added) {
      addSource(peerId);
      if (replicationForBulkLoadDataEnabled) {
        throwIOExceptionWhenFail(() -> this.queueStorage.addPeerToHFileRefs(peerId));
      }
    }
  }

  /**
   * 1. Remove peer for replicationPeers 2. Remove all the recovered sources for the specified id
   * and related replication queues 3. Remove the normal source and related replication queue 4.
   * Remove HFile Refs
   * @param peerId the id of the replication peer
   */
  public void removePeer(String peerId) {
    replicationPeers.removePeer(peerId);
    String terminateMessage = "Replication stream was removed by a user";
    List<ReplicationSourceInterface> oldSourcesToDelete = new ArrayList<>();
    // synchronized on oldsources to avoid adding recovered source for the to-be-removed peer
    // see NodeFailoverWorker.run
    synchronized (this.oldsources) {
      // First close all the recovered sources for this peer
      for (ReplicationSourceInterface src : oldsources) {
        if (peerId.equals(src.getPeerId())) {
          oldSourcesToDelete.add(src);
        }
      }
      for (ReplicationSourceInterface src : oldSourcesToDelete) {
        src.terminate(terminateMessage);
        removeRecoveredSource(src);
      }
    }
    LOG.info(
      "Number of deleted recovered sources for " + peerId + ": " + oldSourcesToDelete.size());
    // Now close the normal source for this peer
    ReplicationSourceInterface srcToRemove = this.sources.get(peerId);
    if (srcToRemove != null) {
      srcToRemove.terminate(terminateMessage);
      removeSource(srcToRemove);
    } else {
      // This only happened in unit test TestReplicationSourceManager#testPeerRemovalCleanup
      // Delete queue from storage and memory and queue id is same with peer id for normal
      // source
      deleteQueue(peerId);
      this.walsById.remove(peerId);
    }

    // Remove HFile Refs
    abortWhenFail(() -> this.queueStorage.removePeerFromHFileRefs(peerId));
  }

  /**
   * Factory method to create a replication source
   * @param queueId the id of the replication queue
   * @return the created source
   */
  private ReplicationSourceInterface createSource(String queueId, ReplicationPeer replicationPeer)
      throws IOException {
    ReplicationSourceInterface src = ReplicationSourceFactory.create(conf, queueId);

    MetricsSource metrics = new MetricsSource(queueId);
    // init replication source
    src.init(conf, fs, this, queueStorage, replicationPeer, server, queueId, clusterId,
      walFileLengthProvider, metrics);
    return src;
  }

  /**
   * Add a normal source for the given peer on this region server. Meanwhile, add new replication
   * queue to storage. For the newly added peer, we only need to enqueue the latest log of each wal
   * group and do replication
   * @param peerId the id of the replication peer
   * @return the source that was created
   */
  @VisibleForTesting
  ReplicationSourceInterface addSource(String peerId) throws IOException {
    ReplicationPeer peer = replicationPeers.getPeer(peerId);
    ReplicationSourceInterface src = createSource(peerId, peer);
    // synchronized on latestPaths to avoid missing the new log
    synchronized (this.latestPaths) {
      this.sources.put(peerId, src);
      Map<String, SortedSet<String>> walsByGroup = new HashMap<>();
      this.walsById.put(peerId, walsByGroup);
      // Add the latest wal to that source's queue
      if (this.latestPaths.size() > 0) {
        for (Path logPath : latestPaths) {
          String name = logPath.getName();
          String walPrefix = AbstractFSWALProvider.getWALPrefixFromWALName(name);
          SortedSet<String> logs = new TreeSet<>();
          logs.add(name);
          walsByGroup.put(walPrefix, logs);
          // Abort RS and throw exception to make add peer failed
          abortAndThrowIOExceptionWhenFail(
            () -> this.queueStorage.addWAL(server.getServerName(), peerId, name));
          src.enqueueLog(logPath);
        }
      }
    }
    src.startup();
    return src;
  }

  /**
   * Close the previous replication sources of this peer id and open new sources to trigger the new
   * replication state changes or new replication config changes. Here we don't need to change
   * replication queue storage and only to enqueue all logs to the new replication source
   * @param peerId the id of the replication peer
   * @throws IOException
   */
  public void refreshSources(String peerId) throws IOException {
    String terminateMessage = "Peer " + peerId +
      " state or config changed. Will close the previous replication source and open a new one";
    ReplicationPeer peer = replicationPeers.getPeer(peerId);
    ReplicationSourceInterface src = createSource(peerId, peer);
    // synchronized on latestPaths to avoid missing the new log
    synchronized (this.latestPaths) {
      ReplicationSourceInterface toRemove = this.sources.put(peerId, src);
      if (toRemove != null) {
        LOG.info("Terminate replication source for " + toRemove.getPeerId());
        toRemove.terminate(terminateMessage);
      }
      for (SortedSet<String> walsByGroup : walsById.get(peerId).values()) {
        walsByGroup.forEach(wal -> src.enqueueLog(new Path(this.logDir, wal)));
      }
    }
    LOG.info("Startup replication source for " + src.getPeerId());
    src.startup();

    List<ReplicationSourceInterface> toStartup = new ArrayList<>();
    // synchronized on oldsources to avoid race with NodeFailoverWorker
    synchronized (this.oldsources) {
      List<String> previousQueueIds = new ArrayList<>();
      for (ReplicationSourceInterface oldSource : this.oldsources) {
        if (oldSource.getPeerId().equals(peerId)) {
          previousQueueIds.add(oldSource.getQueueId());
          oldSource.terminate(terminateMessage);
          this.oldsources.remove(oldSource);
        }
      }
      for (String queueId : previousQueueIds) {
        ReplicationSourceInterface replicationSource = createSource(queueId, peer);
        this.oldsources.add(replicationSource);
        for (SortedSet<String> walsByGroup : walsByIdRecoveredQueues.get(queueId).values()) {
          walsByGroup.forEach(wal -> src.enqueueLog(new Path(wal)));
        }
        toStartup.add(replicationSource);
      }
    }
    for (ReplicationSourceInterface replicationSource : oldsources) {
      replicationSource.startup();
    }
  }

  /**
   * Clear the metrics and related replication queue of the specified old source
   * @param src source to clear
   */
  void removeRecoveredSource(ReplicationSourceInterface src) {
    LOG.info("Done with the recovered queue " + src.getQueueId());
    src.getSourceMetrics().clear();
    this.oldsources.remove(src);
    // Delete queue from storage and memory
    deleteQueue(src.getQueueId());
    this.walsByIdRecoveredQueues.remove(src.getQueueId());
  }

  /**
   * Clear the metrics and related replication queue of the specified old source
   * @param src source to clear
   */
  void removeSource(ReplicationSourceInterface src) {
    LOG.info("Done with the queue " + src.getQueueId());
    src.getSourceMetrics().clear();
    this.sources.remove(src.getPeerId());
    // Delete queue from storage and memory
    deleteQueue(src.getQueueId());
    this.walsById.remove(src.getQueueId());
  }

  /**
   * Delete a complete queue of wals associated with a replication source
   * @param queueId the id of replication queue to delete
   */
  private void deleteQueue(String queueId) {
    abortWhenFail(() -> this.queueStorage.removeQueue(server.getServerName(), queueId));
  }

  @FunctionalInterface
  private interface ReplicationQueueOperation {
    void exec() throws ReplicationException;
  }

  private void abortWhenFail(ReplicationQueueOperation op) {
    try {
      op.exec();
    } catch (ReplicationException e) {
      server.abort("Failed to operate on replication queue", e);
    }
  }

  private void throwIOExceptionWhenFail(ReplicationQueueOperation op) throws IOException {
    try {
      op.exec();
    } catch (ReplicationException e) {
      throw new IOException(e);
    }
  }

  private void abortAndThrowIOExceptionWhenFail(ReplicationQueueOperation op) throws IOException {
    try {
      op.exec();
    } catch (ReplicationException e) {
      server.abort("Failed to operate on replication queue", e);
      throw new IOException(e);
    }
  }

  /**
   * This method will log the current position to storage. And also clean old logs from the
   * replication queue.
   * @param log Path to the log currently being replicated
   * @param queueId id of the replication queue
   * @param position current location in the log
   * @param queueRecovered indicates if this queue comes from another region server
   */
  public void logPositionAndCleanOldLogs(Path log, String queueId, long position,
      boolean queueRecovered) {
    String fileName = log.getName();
    abortWhenFail(
      () -> this.queueStorage.setWALPosition(server.getServerName(), queueId, fileName, position));
    cleanOldLogs(fileName, queueId, queueRecovered);
  }

  /**
   * Cleans a log file and all older logs from replication queue. Called when we are sure that a log
   * file is closed and has no more entries.
   * @param log Path to the log
   * @param queueId id of the replication queue
   * @param queueRecovered Whether this is a recovered queue
   */
  @VisibleForTesting
  void cleanOldLogs(String log, String queueId, boolean queueRecovered) {
    String logPrefix = AbstractFSWALProvider.getWALPrefixFromWALName(log);
    if (queueRecovered) {
      SortedSet<String> wals = walsByIdRecoveredQueues.get(queueId).get(logPrefix);
      if (wals != null && !wals.first().equals(log)) {
        cleanOldLogs(wals, log, queueId);
      }
    } else {
      // synchronized on walsById to avoid race with preLogRoll
      synchronized (this.walsById) {
        SortedSet<String> wals = walsById.get(queueId).get(logPrefix);
        if (wals != null && !wals.first().equals(log)) {
          cleanOldLogs(wals, log, queueId);
        }
      }
    }
  }

  private void cleanOldLogs(SortedSet<String> wals, String key, String id) {
    SortedSet<String> walSet = wals.headSet(key);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing " + walSet.size() + " logs in the list: " + walSet);
    }
    for (String wal : walSet) {
      abortWhenFail(() -> this.queueStorage.removeWAL(server.getServerName(), id, wal));
    }
    walSet.clear();
  }

  // public because of we call it in TestReplicationEmptyWALRecovery
  @VisibleForTesting
  public void preLogRoll(Path newLog) throws IOException {
    String logName = newLog.getName();
    String logPrefix = AbstractFSWALProvider.getWALPrefixFromWALName(logName);
    // synchronized on latestPaths to avoid the new open source miss the new log
    synchronized (this.latestPaths) {
      // Add log to queue storage
      for (ReplicationSourceInterface source : this.sources.values()) {
        // If record log to queue storage failed, abort RS and throw exception to make log roll
        // failed
        abortAndThrowIOExceptionWhenFail(
          () -> this.queueStorage.addWAL(server.getServerName(), source.getQueueId(), logName));
      }

      // synchronized on walsById to avoid race with cleanOldLogs
      synchronized (this.walsById) {
        // Update walsById map
        for (Map.Entry<String, Map<String, SortedSet<String>>> entry : this.walsById.entrySet()) {
          String peerId = entry.getKey();
          Map<String, SortedSet<String>> walsByPrefix = entry.getValue();
          boolean existingPrefix = false;
          for (Map.Entry<String, SortedSet<String>> walsEntry : walsByPrefix.entrySet()) {
            SortedSet<String> wals = walsEntry.getValue();
            if (this.sources.isEmpty()) {
              // If there's no slaves, don't need to keep the old wals since
              // we only consider the last one when a new slave comes in
              wals.clear();
            }
            if (logPrefix.equals(walsEntry.getKey())) {
              wals.add(logName);
              existingPrefix = true;
            }
          }
          if (!existingPrefix) {
            // The new log belongs to a new group, add it into this peer
            LOG.debug("Start tracking logs for wal group " + logPrefix + " for peer " + peerId);
            SortedSet<String> wals = new TreeSet<>();
            wals.add(logName);
            walsByPrefix.put(logPrefix, wals);
          }
        }
      }

      // Add to latestPaths
      Iterator<Path> iterator = latestPaths.iterator();
      while (iterator.hasNext()) {
        Path path = iterator.next();
        if (path.getName().contains(logPrefix)) {
          iterator.remove();
          break;
        }
      }
      this.latestPaths.add(newLog);
    }
  }

  // public because of we call it in TestReplicationEmptyWALRecovery
  @VisibleForTesting
  public void postLogRoll(Path newLog) throws IOException {
    // This only updates the sources we own, not the recovered ones
    for (ReplicationSourceInterface source : this.sources.values()) {
      source.enqueueLog(newLog);
    }
  }

  @Override
  public void regionServerRemoved(String regionserver) {
    transferQueues(ServerName.valueOf(regionserver));
  }

  /**
   * Transfer all the queues of the specified to this region server. First it tries to grab a lock
   * and if it works it will move the old queues and finally will delete the old queues.
   * <p>
   * It creates one old source for any type of source of the old rs.
   */
  private void transferQueues(ServerName deadRS) {
    if (server.getServerName().equals(deadRS)) {
      // it's just us, give up
      return;
    }
    NodeFailoverWorker transfer = new NodeFailoverWorker(deadRS);
    try {
      this.executor.execute(transfer);
    } catch (RejectedExecutionException ex) {
      LOG.info("Cancelling the transfer of " + deadRS + " because of " + ex.getMessage());
    }
  }

  /**
   * Class responsible to setup new ReplicationSources to take care of the queues from dead region
   * servers.
   */
  class NodeFailoverWorker extends Thread {

    private final ServerName deadRS;

    @VisibleForTesting
    public NodeFailoverWorker(ServerName deadRS) {
      super("Failover-for-" + deadRS);
      this.deadRS = deadRS;
    }

    @Override
    public void run() {
      // Wait a bit before transferring the queues, we may be shutting down.
      // This sleep may not be enough in some cases.
      try {
        Thread.sleep(sleepBeforeFailover +
          (long) (ThreadLocalRandom.current().nextFloat() * sleepBeforeFailover));
      } catch (InterruptedException e) {
        LOG.warn("Interrupted while waiting before transferring a queue.");
        Thread.currentThread().interrupt();
      }
      // We try to lock that rs' queue directory
      if (server.isStopped()) {
        LOG.info("Not transferring queue since we are shutting down");
        return;
      }
      Map<String, Set<String>> newQueues = new HashMap<>();
      try {
        List<String> queues = queueStorage.getAllQueues(deadRS);
        while (!queues.isEmpty()) {
          Pair<String, SortedSet<String>> peer = queueStorage.claimQueue(deadRS,
            queues.get(ThreadLocalRandom.current().nextInt(queues.size())), server.getServerName());
          long sleep = sleepBeforeFailover / 2;
          if (!peer.getSecond().isEmpty()) {
            newQueues.put(peer.getFirst(), peer.getSecond());
            sleep = sleepBeforeFailover;
          }
          try {
            Thread.sleep(sleep);
          } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting before transferring a queue.");
            Thread.currentThread().interrupt();
          }
          queues = queueStorage.getAllQueues(deadRS);
        }
        if (queues.isEmpty()) {
          queueStorage.removeReplicatorIfQueueIsEmpty(deadRS);
        }
      } catch (ReplicationException e) {
        server.abort("Failed to claim queue from dead regionserver", e);
        return;
      }
      // Copying over the failed queue is completed.
      if (newQueues.isEmpty()) {
        // We either didn't get the lock or the failed region server didn't have any outstanding
        // WALs to replicate, so we are done.
        return;
      }

      for (Map.Entry<String, Set<String>> entry : newQueues.entrySet()) {
        String queueId = entry.getKey();
        Set<String> walsSet = entry.getValue();
        try {
          // there is not an actual peer defined corresponding to peerId for the failover.
          ReplicationQueueInfo replicationQueueInfo = new ReplicationQueueInfo(queueId);
          String actualPeerId = replicationQueueInfo.getPeerId();

          ReplicationPeer peer = replicationPeers.getPeer(actualPeerId);
          if (peer == null) {
            LOG.warn("Skipping failover for peer:" + actualPeerId + " of node " + deadRS +
              ", peer is null");
            abortWhenFail(() -> queueStorage.removeQueue(server.getServerName(), queueId));
            continue;
          }
          if (server instanceof ReplicationSyncUp.DummyServer
              && peer.getPeerState().equals(PeerState.DISABLED)) {
            LOG.warn("Peer {} is disbaled. ReplicationSyncUp tool will skip "
                + "replicating data to this peer.",
              actualPeerId);
            continue;
          }
          // track sources in walsByIdRecoveredQueues
          Map<String, SortedSet<String>> walsByGroup = new HashMap<>();
          walsByIdRecoveredQueues.put(queueId, walsByGroup);
          for (String wal : walsSet) {
            String walPrefix = AbstractFSWALProvider.getWALPrefixFromWALName(wal);
            SortedSet<String> wals = walsByGroup.get(walPrefix);
            if (wals == null) {
              wals = new TreeSet<>();
              walsByGroup.put(walPrefix, wals);
            }
            wals.add(wal);
          }

          ReplicationSourceInterface src = createSource(queueId, peer);
          // synchronized on oldsources to avoid adding recovered source for the to-be-removed peer
          synchronized (oldsources) {
            if (!replicationPeers.getAllPeerIds().contains(src.getPeerId())) {
              src.terminate("Recovered queue doesn't belong to any current peer");
              removeRecoveredSource(src);
              continue;
            }
            oldsources.add(src);
            for (String wal : walsSet) {
              src.enqueueLog(new Path(oldLogDir, wal));
            }
            src.startup();
          }
        } catch (IOException e) {
          // TODO manage it
          LOG.error("Failed creating a source", e);
        }
      }
    }
  }

  /**
   * Terminate the replication on this region server
   */
  public void join() {
    this.executor.shutdown();
    for (ReplicationSourceInterface source : this.sources.values()) {
      source.terminate("Region server is closing");
    }
  }

  /**
   * Get a copy of the wals of the normal sources on this rs
   * @return a sorted set of wal names
   */
  @VisibleForTesting
  Map<String, Map<String, SortedSet<String>>> getWALs() {
    return Collections.unmodifiableMap(walsById);
  }

  /**
   * Get a copy of the wals of the recovered sources on this rs
   * @return a sorted set of wal names
   */
  @VisibleForTesting
  Map<String, Map<String, SortedSet<String>>> getWalsByIdRecoveredQueues() {
    return Collections.unmodifiableMap(walsByIdRecoveredQueues);
  }

  /**
   * Get a list of all the normal sources of this rs
   * @return list of all normal sources
   */
  public List<ReplicationSourceInterface> getSources() {
    return new ArrayList<>(this.sources.values());
  }

  /**
   * Get a list of all the recovered sources of this rs
   * @return list of all recovered sources
   */
  public List<ReplicationSourceInterface> getOldSources() {
    return this.oldsources;
  }

  /**
   * Get the normal source for a given peer
   * @return the normal source for the give peer if it exists, otherwise null.
   */
  @VisibleForTesting
  public ReplicationSourceInterface getSource(String peerId) {
    return this.sources.get(peerId);
  }

  @VisibleForTesting
  List<String> getAllQueues() throws IOException {
    List<String> allQueues = Collections.emptyList();
    try {
      allQueues = queueStorage.getAllQueues(server.getServerName());
    } catch (ReplicationException e) {
      throw new IOException(e);
    }
    return allQueues;
  }

  @VisibleForTesting
  int getSizeOfLatestPath() {
    synchronized (latestPaths) {
      return latestPaths.size();
    }
  }

  @VisibleForTesting
  public AtomicLong getTotalBufferUsed() {
    return totalBufferUsed;
  }

  /**
   * Get the directory where wals are archived
   * @return the directory where wals are archived
   */
  public Path getOldLogDir() {
    return this.oldLogDir;
  }

  /**
   * Get the directory where wals are stored by their RSs
   * @return the directory where wals are stored by their RSs
   */
  public Path getLogDir() {
    return this.logDir;
  }

  /**
   * Get the handle on the local file system
   * @return Handle on the local file system
   */
  public FileSystem getFs() {
    return this.fs;
  }

  public Connection getConnection() {
    return this.connection;
  }

  /**
   * Get the ReplicationPeers used by this ReplicationSourceManager
   * @return the ReplicationPeers used by this ReplicationSourceManager
   */
  public ReplicationPeers getReplicationPeers() {
    return this.replicationPeers;
  }

  /**
   * Get a string representation of all the sources' metrics
   */
  public String getStats() {
    StringBuilder stats = new StringBuilder();
    for (ReplicationSourceInterface source : this.sources.values()) {
      stats.append("Normal source for cluster " + source.getPeerId() + ": ");
      stats.append(source.getStats() + "\n");
    }
    for (ReplicationSourceInterface oldSource : oldsources) {
      stats.append("Recovered source for cluster/machine(s) " + oldSource.getPeerId() + ": ");
      stats.append(oldSource.getStats() + "\n");
    }
    return stats.toString();
  }

  public void addHFileRefs(TableName tableName, byte[] family, List<Pair<Path, Path>> pairs)
      throws IOException {
    for (ReplicationSourceInterface source : this.sources.values()) {
      throwIOExceptionWhenFail(() -> source.addHFileRefs(tableName, family, pairs));
    }
  }

  public void cleanUpHFileRefs(String peerId, List<String> files) {
    abortWhenFail(() -> this.queueStorage.removeHFileRefs(peerId, files));
  }

  int activeFailoverTaskCount() {
    return executor.getActiveCount();
  }

  /**
   * Whether an entry can be pushed to the peer or not right now. If we enable serial replication,
   * we can not push the entry until all entries in its region whose sequence numbers are smaller
   * than this entry have been pushed. For each ReplicationSource, we need only check the first
   * entry in each region, as long as it can be pushed, we can push all in this ReplicationSource.
   * This method will be blocked until we can push.
   * @return the first barrier of entry's region, or -1 if there is no barrier. It is used to
   *         prevent saving positions in the region of no barrier.
   */
  void waitUntilCanBePushed(byte[] encodedName, long seq, String peerId)
      throws IOException, InterruptedException {
    /**
     * There are barriers for this region and position for this peer. N barriers form N intervals,
     * (b1,b2) (b2,b3) ... (bn,max). Generally, there is no logs whose seq id is not greater than
     * the first barrier and the last interval is start from the last barrier. There are several
     * conditions that we can push now, otherwise we should block: 1) "Serial replication" is not
     * enabled, we can push all logs just like before. This case should not call this method. 2)
     * There is no barriers for this region, or the seq id is smaller than the first barrier. It is
     * mainly because we alter REPLICATION_SCOPE = 2. We can not guarantee the order of logs that is
     * written before altering. 3) This entry is in the first interval of barriers. We can push them
     * because it is the start of a region. But if the region is created by region split, we should
     * check if the parent regions are fully pushed. 4) If the entry's seq id and the position are
     * in same section, or the pos is the last number of previous section. Because when open a
     * region we put a barrier the number is the last log's id + 1. 5) Log's seq is smaller than pos
     * in meta, we are retrying. It may happen when a RS crashes after save replication meta and
     * before save zk offset.
     */
    List<Long> barriers = MetaTableAccessor.getReplicationBarriers(connection, encodedName);
    if (barriers.isEmpty() || seq <= barriers.get(0)) {
      // Case 2
      return;
    }
    int interval = Collections.binarySearch(barriers, seq);
    if (interval < 0) {
      interval = -interval - 1;// get the insert position if negative
    }
    if (interval == 1) {
      // Case 3
      // Check if there are parent regions
      String parentValue =
          MetaTableAccessor.getSerialReplicationParentRegion(connection, encodedName);
      if (parentValue == null) {
        // This region has no parent or the parent's log entries are fully pushed.
        return;
      }
      while (true) {
        boolean allParentDone = true;
        String[] parentRegions = parentValue.split(",");
        for (String parent : parentRegions) {
          byte[] region = Bytes.toBytes(parent);
          long pos = MetaTableAccessor.getReplicationPositionForOnePeer(connection, region, peerId);
          List<Long> parentBarriers = MetaTableAccessor.getReplicationBarriers(connection, region);
          if (parentBarriers.size() > 0 &&
            parentBarriers.get(parentBarriers.size() - 1) - 1 > pos) {
            allParentDone = false;
            // For a closed region, we will write a close event marker to WAL whose sequence id is
            // larger than final barrier but still smaller than next region's openSeqNum.
            // So if the pos is larger than last barrier, we can say we have read the event marker
            // which means the parent region has been fully pushed.
            LOG.info(
              Bytes.toString(encodedName) + " can not start pushing because parent region's" +
                " log has not been fully pushed: parent=" + Bytes.toString(region) + " pos=" + pos +
                " barriers=" + Arrays.toString(barriers.toArray()));
            break;
          }
        }
        if (allParentDone) {
          return;
        } else {
          Thread.sleep(replicationWaitTime);
        }
      }

    }

    while (true) {
      long pos =
          MetaTableAccessor.getReplicationPositionForOnePeer(connection, encodedName, peerId);
      if (seq <= pos) {
        // Case 5
      }
      if (pos >= 0) {
        // Case 4
        int posInterval = Collections.binarySearch(barriers, pos);
        if (posInterval < 0) {
          posInterval = -posInterval - 1;// get the insert position if negative
        }
        if (posInterval == interval || pos == barriers.get(interval - 1) - 1) {
          return;
        }
      }

      LOG.info(Bytes.toString(encodedName) + " can not start pushing to peer " + peerId +
        " because previous log has not been pushed: sequence=" + seq + " pos=" + pos +
        " barriers=" + Arrays.toString(barriers.toArray()));
      Thread.sleep(replicationWaitTime);
    }
  }
}
