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

package org.apache.hadoop.hbase.regionserver;

import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.metrics.BaseSourceImpl;
import org.apache.hadoop.metrics2.MetricHistogram;
import org.apache.hadoop.metrics2.MetricsCollector;
import org.apache.hadoop.metrics2.MetricsRecordBuilder;
import org.apache.hadoop.metrics2.lib.Interns;
import org.apache.hadoop.metrics2.lib.MutableCounterLong;

/**
 * Hadoop2 implementation of MetricsRegionServerSource.
 *
 * Implements BaseSource through BaseSourceImpl, following the pattern
 */
@InterfaceAudience.Private
public class MetricsRegionServerSourceImpl
    extends BaseSourceImpl implements MetricsRegionServerSource {


  final MetricsRegionServerWrapper rsWrap;
  private final MetricHistogram putHisto;
  private final MetricHistogram deleteHisto;
  private final MetricHistogram getHisto;
  private final MetricHistogram incrementHisto;
  private final MetricHistogram appendHisto;
  private final MetricHistogram replayHisto;
  private final MetricHistogram scanNextHisto;

  private final MutableCounterLong slowPut;
  private final MutableCounterLong slowDelete;
  private final MutableCounterLong slowGet;
  private final MutableCounterLong slowIncrement;
  private final MutableCounterLong slowAppend;
  private final MutableCounterLong splitRequest;
  private final MutableCounterLong splitSuccess;

  private final MetricHistogram splitTimeHisto;
  private final MetricHistogram flushTimeHisto;

  public MetricsRegionServerSourceImpl(MetricsRegionServerWrapper rsWrap) {
    this(METRICS_NAME, METRICS_DESCRIPTION, METRICS_CONTEXT, METRICS_JMX_CONTEXT, rsWrap);
  }

  public MetricsRegionServerSourceImpl(String metricsName,
                                       String metricsDescription,
                                       String metricsContext,
                                       String metricsJmxContext,
                                       MetricsRegionServerWrapper rsWrap) {
    super(metricsName, metricsDescription, metricsContext, metricsJmxContext);
    this.rsWrap = rsWrap;

    putHisto = getMetricsRegistry().newTimeHistogram(MUTATE_KEY);
    slowPut = getMetricsRegistry().newCounter(SLOW_MUTATE_KEY, SLOW_MUTATE_DESC, 0L);

    deleteHisto = getMetricsRegistry().newTimeHistogram(DELETE_KEY);
    slowDelete = getMetricsRegistry().newCounter(SLOW_DELETE_KEY, SLOW_DELETE_DESC, 0L);

    getHisto = getMetricsRegistry().newTimeHistogram(GET_KEY);
    slowGet = getMetricsRegistry().newCounter(SLOW_GET_KEY, SLOW_GET_DESC, 0L);

    incrementHisto = getMetricsRegistry().newTimeHistogram(INCREMENT_KEY);
    slowIncrement = getMetricsRegistry().newCounter(SLOW_INCREMENT_KEY, SLOW_INCREMENT_DESC, 0L);

    appendHisto = getMetricsRegistry().newTimeHistogram(APPEND_KEY);
    slowAppend = getMetricsRegistry().newCounter(SLOW_APPEND_KEY, SLOW_APPEND_DESC, 0L);
    
    replayHisto = getMetricsRegistry().newTimeHistogram(REPLAY_KEY);
    scanNextHisto = getMetricsRegistry().newTimeHistogram(SCAN_NEXT_KEY);

    splitTimeHisto = getMetricsRegistry().newTimeHistogram(SPLIT_KEY);
    flushTimeHisto = getMetricsRegistry().newTimeHistogram(FLUSH_KEY);

    splitRequest = getMetricsRegistry().newCounter(SPLIT_REQUEST_KEY, SPLIT_REQUEST_DESC, 0L);
    splitSuccess = getMetricsRegistry().newCounter(SPLIT_SUCCESS_KEY, SPLIT_SUCCESS_DESC, 0L);
  }

  @Override
  public void updatePut(long t) {
    putHisto.add(t);
  }

  @Override
  public void updateDelete(long t) {
    deleteHisto.add(t);
  }

  @Override
  public void updateGet(long t) {
    getHisto.add(t);
  }

  @Override
  public void updateIncrement(long t) {
    incrementHisto.add(t);
  }

  @Override
  public void updateAppend(long t) {
    appendHisto.add(t);
  }

  @Override
  public void updateReplay(long t) {
    replayHisto.add(t);
  }

  @Override
  public void updateScannerNext(long scanSize) {
    scanNextHisto.add(scanSize);
  }

  @Override
  public void incrSlowPut() {
   slowPut.incr();
  }

  @Override
  public void incrSlowDelete() {
    slowDelete.incr();
  }

  @Override
  public void incrSlowGet() {
    slowGet.incr();
  }

  @Override
  public void incrSlowIncrement() {
    slowIncrement.incr();
  }

  @Override
  public void incrSlowAppend() {
    slowAppend.incr();
  }

  @Override
  public void incrSplitRequest() {
    splitRequest.incr();
  }

  @Override
  public void incrSplitSuccess() {
    splitSuccess.incr();
  }

  @Override
  public void updateSplitTime(long t) {
    splitTimeHisto.add(t);
  }

  @Override
  public void updateFlushTime(long t) {
    flushTimeHisto.add(t);
  }

  /**
   * Yes this is a get function that doesn't return anything.  Thanks Hadoop for breaking all
   * expectations of java programmers.  Instead of returning anything Hadoop metrics expects
   * getMetrics to push the metrics into the collector.
   *
   * @param metricsCollector Collector to accept metrics
   * @param all              push all or only changed?
   */
  @Override
  public void getMetrics(MetricsCollector metricsCollector, boolean all) {

    MetricsRecordBuilder mrb = metricsCollector.addRecord(metricsName);

    // rsWrap can be null because this function is called inside of init.
    if (rsWrap != null) {
      mrb.addGauge(Interns.info(REGION_COUNT, REGION_COUNT_DESC), rsWrap.getNumOnlineRegions())
          .addGauge(Interns.info(STORE_COUNT, STORE_COUNT_DESC), rsWrap.getNumStores())
          .addGauge(Interns.info(WALFILE_COUNT, WALFILE_COUNT_DESC), rsWrap.getNumWALFiles())
          .addGauge(Interns.info(WALFILE_SIZE, WALFILE_SIZE_DESC), rsWrap.getWALFileSize())
          .addGauge(Interns.info(STOREFILE_COUNT, STOREFILE_COUNT_DESC), rsWrap.getNumStoreFiles())
          .addGauge(Interns.info(MEMSTORE_SIZE, MEMSTORE_SIZE_DESC), rsWrap.getMemstoreSize())
          .addGauge(Interns.info(STOREFILE_SIZE, STOREFILE_SIZE_DESC), rsWrap.getStoreFileSize())
          .addGauge(Interns.info(RS_START_TIME_NAME, RS_START_TIME_DESC),
              rsWrap.getStartCode())
          .addCounter(Interns.info(TOTAL_REQUEST_COUNT, TOTAL_REQUEST_COUNT_DESC),
              rsWrap.getTotalRequestCount())
          .addCounter(Interns.info(READ_REQUEST_COUNT, READ_REQUEST_COUNT_DESC),
              rsWrap.getReadRequestsCount())
          .addCounter(Interns.info(FILTERED_READ_REQUEST_COUNT, FILTERED_READ_REQUEST_COUNT_DESC),
              rsWrap.getFilteredReadRequestsCount())
          .addCounter(Interns.info(WRITE_REQUEST_COUNT, WRITE_REQUEST_COUNT_DESC),
              rsWrap.getWriteRequestsCount())
          .addCounter(Interns.info(RPC_GET_REQUEST_COUNT, RPC_GET_REQUEST_COUNT_DESC),
            rsWrap.getRpcGetRequestsCount())
          .addCounter(Interns.info(RPC_SCAN_REQUEST_COUNT, RPC_SCAN_REQUEST_COUNT_DESC),
            rsWrap.getRpcScanRequestsCount())
          .addCounter(Interns.info(RPC_MULTI_REQUEST_COUNT, RPC_MULTI_REQUEST_COUNT_DESC),
            rsWrap.getRpcMultiRequestsCount())
          .addCounter(Interns.info(RPC_MUTATE_REQUEST_COUNT, RPC_MUTATE_REQUEST_COUNT_DESC),
            rsWrap.getRpcMutateRequestsCount())
          .addCounter(Interns.info(CHECK_MUTATE_FAILED_COUNT, CHECK_MUTATE_FAILED_COUNT_DESC),
              rsWrap.getCheckAndMutateChecksFailed())
          .addCounter(Interns.info(CHECK_MUTATE_PASSED_COUNT, CHECK_MUTATE_PASSED_COUNT_DESC),
              rsWrap.getCheckAndMutateChecksPassed())
          .addGauge(Interns.info(STOREFILE_INDEX_SIZE, STOREFILE_INDEX_SIZE_DESC),
              rsWrap.getStoreFileIndexSize())
          .addGauge(Interns.info(STATIC_INDEX_SIZE, STATIC_INDEX_SIZE_DESC),
              rsWrap.getTotalStaticIndexSize())
          .addGauge(Interns.info(STATIC_BLOOM_SIZE, STATIC_BLOOM_SIZE_DESC),
            rsWrap.getTotalStaticBloomSize())
          .addGauge(
            Interns.info(NUMBER_OF_MUTATIONS_WITHOUT_WAL, NUMBER_OF_MUTATIONS_WITHOUT_WAL_DESC),
              rsWrap.getNumMutationsWithoutWAL())
          .addGauge(Interns.info(DATA_SIZE_WITHOUT_WAL, DATA_SIZE_WITHOUT_WAL_DESC),
              rsWrap.getDataInMemoryWithoutWAL())
          .addGauge(Interns.info(PERCENT_FILES_LOCAL, PERCENT_FILES_LOCAL_DESC),
              rsWrap.getPercentFileLocal())
          .addGauge(Interns.info(PERCENT_FILES_LOCAL_SECONDARY_REGIONS,
              PERCENT_FILES_LOCAL_SECONDARY_REGIONS_DESC),
              rsWrap.getPercentFileLocalSecondaryRegions())
          .addGauge(Interns.info(SPLIT_QUEUE_LENGTH, SPLIT_QUEUE_LENGTH_DESC),
              rsWrap.getSplitQueueSize())
          .addGauge(Interns.info(COMPACTION_QUEUE_LENGTH, COMPACTION_QUEUE_LENGTH_DESC),
              rsWrap.getCompactionQueueSize())
          .addGauge(Interns.info(FLUSH_QUEUE_LENGTH, FLUSH_QUEUE_LENGTH_DESC),
              rsWrap.getFlushQueueSize())
          .addGauge(Interns.info(BLOCK_CACHE_FREE_SIZE, BLOCK_CACHE_FREE_DESC),
              rsWrap.getBlockCacheFreeSize())
          .addGauge(Interns.info(BLOCK_CACHE_COUNT, BLOCK_CACHE_COUNT_DESC),
              rsWrap.getBlockCacheCount())
          .addGauge(Interns.info(BLOCK_CACHE_SIZE, BLOCK_CACHE_SIZE_DESC),
              rsWrap.getBlockCacheSize())
          .addCounter(Interns.info(BLOCK_CACHE_HIT_COUNT, BLOCK_CACHE_HIT_COUNT_DESC),
              rsWrap.getBlockCacheHitCount())
          .addCounter(Interns.info(BLOCK_CACHE_PRIMARY_HIT_COUNT,
            BLOCK_CACHE_PRIMARY_HIT_COUNT_DESC), rsWrap.getBlockCachePrimaryHitCount())
          .addCounter(Interns.info(BLOCK_CACHE_MISS_COUNT, BLOCK_COUNT_MISS_COUNT_DESC),
              rsWrap.getBlockCacheMissCount())
          .addCounter(Interns.info(BLOCK_CACHE_PRIMARY_MISS_COUNT,
            BLOCK_COUNT_PRIMARY_MISS_COUNT_DESC), rsWrap.getBlockCachePrimaryMissCount())
          .addCounter(Interns.info(BLOCK_CACHE_EVICTION_COUNT, BLOCK_CACHE_EVICTION_COUNT_DESC),
              rsWrap.getBlockCacheEvictedCount())
          .addCounter(Interns.info(BLOCK_CACHE_PRIMARY_EVICTION_COUNT,
            BLOCK_CACHE_PRIMARY_EVICTION_COUNT_DESC), rsWrap.getBlockCachePrimaryEvictedCount())
          .addGauge(Interns.info(BLOCK_CACHE_HIT_PERCENT, BLOCK_CACHE_HIT_PERCENT_DESC),
              rsWrap.getBlockCacheHitPercent())
          .addGauge(Interns.info(BLOCK_CACHE_EXPRESS_HIT_PERCENT,
              BLOCK_CACHE_EXPRESS_HIT_PERCENT_DESC), rsWrap.getBlockCacheHitCachingPercent())
          .addCounter(Interns.info(BLOCK_CACHE_FAILED_INSERTION_COUNT,
              BLOCK_CACHE_FAILED_INSERTION_COUNT_DESC),rsWrap.getBlockCacheFailedInsertions())
          .addCounter(Interns.info(UPDATES_BLOCKED_TIME, UPDATES_BLOCKED_DESC),
              rsWrap.getUpdatesBlockedTime())
          .addCounter(Interns.info(FLUSHED_CELLS, FLUSHED_CELLS_DESC),
              rsWrap.getFlushedCellsCount())
          .addCounter(Interns.info(COMPACTED_CELLS, COMPACTED_CELLS_DESC),
              rsWrap.getCompactedCellsCount())
          .addCounter(Interns.info(MAJOR_COMPACTED_CELLS, MAJOR_COMPACTED_CELLS_DESC),
              rsWrap.getMajorCompactedCellsCount())
          .addCounter(Interns.info(FLUSHED_CELLS_SIZE, FLUSHED_CELLS_SIZE_DESC),
              rsWrap.getFlushedCellsSize())
          .addCounter(Interns.info(COMPACTED_CELLS_SIZE, COMPACTED_CELLS_SIZE_DESC),
              rsWrap.getCompactedCellsSize())
          .addCounter(Interns.info(MAJOR_COMPACTED_CELLS_SIZE, MAJOR_COMPACTED_CELLS_SIZE_DESC),
              rsWrap.getMajorCompactedCellsSize())

          .addCounter(
              Interns.info(CELLS_COUNT_COMPACTED_FROM_MOB, CELLS_COUNT_COMPACTED_FROM_MOB_DESC),
              rsWrap.getCellsCountCompactedFromMob())
          .addCounter(Interns.info(CELLS_COUNT_COMPACTED_TO_MOB, CELLS_COUNT_COMPACTED_TO_MOB_DESC),
              rsWrap.getCellsCountCompactedToMob())
          .addCounter(
              Interns.info(CELLS_SIZE_COMPACTED_FROM_MOB, CELLS_SIZE_COMPACTED_FROM_MOB_DESC),
              rsWrap.getCellsSizeCompactedFromMob())
          .addCounter(Interns.info(CELLS_SIZE_COMPACTED_TO_MOB, CELLS_SIZE_COMPACTED_TO_MOB_DESC),
              rsWrap.getCellsSizeCompactedToMob())
          .addCounter(Interns.info(MOB_FLUSH_COUNT, MOB_FLUSH_COUNT_DESC),
              rsWrap.getMobFlushCount())
          .addCounter(Interns.info(MOB_FLUSHED_CELLS_COUNT, MOB_FLUSHED_CELLS_COUNT_DESC),
              rsWrap.getMobFlushedCellsCount())
          .addCounter(Interns.info(MOB_FLUSHED_CELLS_SIZE, MOB_FLUSHED_CELLS_SIZE_DESC),
              rsWrap.getMobFlushedCellsSize())
          .addCounter(Interns.info(MOB_SCAN_CELLS_COUNT, MOB_SCAN_CELLS_COUNT_DESC),
              rsWrap.getMobScanCellsCount())
          .addCounter(Interns.info(MOB_SCAN_CELLS_SIZE, MOB_SCAN_CELLS_SIZE_DESC),
              rsWrap.getMobScanCellsSize())
          .addGauge(Interns.info(MOB_FILE_CACHE_COUNT, MOB_FILE_CACHE_COUNT_DESC),
              rsWrap.getMobFileCacheCount())
          .addCounter(Interns.info(MOB_FILE_CACHE_ACCESS_COUNT, MOB_FILE_CACHE_ACCESS_COUNT_DESC),
              rsWrap.getMobFileCacheAccessCount())
          .addCounter(Interns.info(MOB_FILE_CACHE_MISS_COUNT, MOB_FILE_CACHE_MISS_COUNT_DESC),
              rsWrap.getMobFileCacheMissCount())
          .addCounter(
              Interns.info(MOB_FILE_CACHE_EVICTED_COUNT, MOB_FILE_CACHE_EVICTED_COUNT_DESC),
              rsWrap.getMobFileCacheEvictedCount())
          .addGauge(Interns.info(MOB_FILE_CACHE_HIT_PERCENT, MOB_FILE_CACHE_HIT_PERCENT_DESC),
              rsWrap.getMobFileCacheHitPercent())

          .addCounter(Interns.info(HEDGED_READS, HEDGED_READS_DESC), rsWrap.getHedgedReadOps())
          .addCounter(Interns.info(HEDGED_READ_WINS, HEDGED_READ_WINS_DESC),
              rsWrap.getHedgedReadWins())

          .addCounter(Interns.info(BLOCKED_REQUESTS_COUNT, BLOCKED_REQUESTS_COUNT_DESC),
            rsWrap.getBlockedRequestsCount())

          .tag(Interns.info(ZOOKEEPER_QUORUM_NAME, ZOOKEEPER_QUORUM_DESC),
              rsWrap.getZookeeperQuorum())
          .tag(Interns.info(SERVER_NAME_NAME, SERVER_NAME_DESC), rsWrap.getServerName())
          .tag(Interns.info(CLUSTER_ID_NAME, CLUSTER_ID_DESC), rsWrap.getClusterId());
    }

    metricsRegistry.snapshot(mrb, all);
  }
}
