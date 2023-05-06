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

package org.apache.hadoop.hbase.replication;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.Waiter;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.replication.ReplicationAdmin;
import org.apache.hadoop.hbase.mapreduce.replication.VerifyReplication;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.regionserver.MultiVersionConcurrencyControl;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.replication.regionserver.Replication;
import org.apache.hadoop.hbase.replication.regionserver.ReplicationSource;
import org.apache.hadoop.hbase.replication.regionserver.ReplicationSourceInterface;
import org.apache.hadoop.hbase.shaded.protobuf.generated.WALProtos;
import org.apache.hadoop.hbase.snapshot.SnapshotTestingUtils;
import org.apache.hadoop.hbase.testclassification.LargeTests;
import org.apache.hadoop.hbase.testclassification.ReplicationTests;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.EnvironmentEdgeManager;
import org.apache.hadoop.hbase.util.FSUtils;
import org.apache.hadoop.hbase.util.JVMClusterUtil;
import org.apache.hadoop.hbase.wal.AbstractFSWALProvider;
import org.apache.hadoop.hbase.wal.WAL;
import org.apache.hadoop.hbase.wal.WALKey;
import org.apache.hadoop.mapreduce.Job;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;

import com.google.common.collect.Lists;

@Category({ReplicationTests.class, LargeTests.class})
public class TestReplicationSmallTests extends TestReplicationBase {

  private static final Log LOG = LogFactory.getLog(TestReplicationSmallTests.class);
  private static final String PEER_ID = "2";

  @Rule
  public TestName name = new TestName();

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    // Starting and stopping replication can make us miss new logs,
    // rolling like this makes sure the most recent one gets added to the queue
    for ( JVMClusterUtil.RegionServerThread r :
        utility1.getHBaseCluster().getRegionServerThreads()) {
      utility1.getAdmin().rollWALWriter(r.getRegionServer().getServerName());
    }
    int rowCount = utility1.countRows(tableName);
    utility1.deleteTableData(tableName);
    // truncating the table will send one Delete per row to the slave cluster
    // in an async fashion, which is why we cannot just call deleteTableData on
    // utility2 since late writes could make it to the slave in some way.
    // Instead, we truncate the first table and wait for all the Deletes to
    // make it to the slave.
    Scan scan = new Scan();
    int lastCount = 0;
    for (int i = 0; i < NB_RETRIES; i++) {
      if (i==NB_RETRIES-1) {
        fail("Waited too much time for truncate");
      }
      ResultScanner scanner = htable2.getScanner(scan);
      Result[] res = scanner.next(rowCount);
      scanner.close();
      if (res.length != 0) {
        if (res.length < lastCount) {
          i--; // Don't increment timeout if we make progress
        }
        lastCount = res.length;
        LOG.info("Still got " + res.length + " rows");
        Thread.sleep(SLEEP_TIME);
      } else {
        break;
      }
    }
  }

  /**
   * Add a row, check it's replicated, delete it, check's gone
   * @throws Exception
   */
//  @Test(timeout=300000)
  public void testSimplePutDelete() throws Exception {
    LOG.info("testSimplePutDelete");
    Put put = new Put(row);
    put.addColumn(famName, row, row);

    htable1 = utility1.getConnection().getTable(tableName);
    htable1.put(put);

    Get get = new Get(row);
    for (int i = 0; i < NB_RETRIES; i++) {
      if (i==NB_RETRIES-1) {
        fail("Waited too much time for put replication");
      }
      Result res = htable2.get(get);
      if (res.isEmpty()) {
        LOG.info("Row not available");
        Thread.sleep(SLEEP_TIME);
      } else {
        assertArrayEquals(res.value(), row);
        break;
      }
    }

    Delete del = new Delete(row);
    htable1.delete(del);

    get = new Get(row);
    for (int i = 0; i < NB_RETRIES; i++) {
      if (i==NB_RETRIES-1) {
        fail("Waited too much time for del replication");
      }
      Result res = htable2.get(get);
      if (res.size() >= 1) {
        LOG.info("Row not deleted");
        Thread.sleep(SLEEP_TIME);
      } else {
        break;
      }
    }
  }

  @Test
  public void testEmptyWALRecovery() throws Exception {
    final int numRs = utility1.getHBaseCluster().getRegionServerThreads().size();

    // roll the original wal, which enqueues a new wal behind our empty wal
    for (int i = 0; i < numRs; i++) {
      HRegionInfo regionInfo =
          utility1.getHBaseCluster().getRegions(htable1.getName()).get(0).getRegionInfo();
      WAL wal = utility1.getHBaseCluster().getRegionServer(i).getWAL(regionInfo);
      wal.rollWriter(true);
    }

    // for each RS, create an empty wal with same walGroupId
    final List<Path> emptyWalPaths = new ArrayList<>();
    long ts = System.currentTimeMillis();
    for (int i = 0; i < numRs; i++) {
      HRegionInfo regionInfo =
          utility1.getHBaseCluster().getRegions(htable1.getName()).get(0).getRegionInfo();
      WAL wal = utility1.getHBaseCluster().getRegionServer(i).getWAL(regionInfo);
      Path currentWalPath = AbstractFSWALProvider.getCurrentFileName(wal);
      String walGroupId = AbstractFSWALProvider.getWALPrefixFromWALName(currentWalPath.getName());
      Path emptyWalPath = new Path(utility1.getDataTestDir(), walGroupId + "." + ts);
      utility1.getTestFileSystem().create(emptyWalPath).close();
      emptyWalPaths.add(emptyWalPath);
    }

    // inject our empty wal into the replication queue
    for (int i = 0; i < numRs; i++) {
      Replication replicationService =
          (Replication) utility1.getHBaseCluster().getRegionServer(i).getReplicationSourceService();
      replicationService.preLogRoll(null, emptyWalPaths.get(i));
      replicationService.postLogRoll(null, emptyWalPaths.get(i));
    }

    // wait for ReplicationSource to start reading from our empty wal
    waitForLogAdvance(numRs, emptyWalPaths, false);

  }

  /**
   * Waits for the ReplicationSource to start reading from the given paths
   * @param numRs number of regionservers
   * @param emptyWalPaths path for each regionserver
   * @param invert if true, waits until ReplicationSource is NOT reading from the given paths
   */
  private void waitForLogAdvance(final int numRs, final List<Path> emptyWalPaths,
      final boolean invert) throws Exception {
    Waiter.waitFor(conf1, 10000, new Waiter.Predicate<Exception>() {
      @Override
      public boolean evaluate() throws Exception {
        for (int i = 0; i < numRs; i++) {
          Replication replicationService = (Replication) utility1.getHBaseCluster()
              .getRegionServer(i).getReplicationSourceService();
          for (ReplicationSourceInterface rsi : replicationService.getReplicationManager()
              .getSources()) {
            ReplicationSource source = (ReplicationSource) rsi;
            if (!invert && !emptyWalPaths.get(i).equals(source.getCurrentPath())) {
              return false;
            }
            if (invert && emptyWalPaths.get(i).equals(source.getCurrentPath())) {
              return false;
            }
          }
        }
        return true;
      }
    });
  }
}
