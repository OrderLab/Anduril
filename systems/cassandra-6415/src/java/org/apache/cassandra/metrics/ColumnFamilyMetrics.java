/*
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
package org.apache.cassandra.metrics;

import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.util.RatioGauge;

import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.io.sstable.SSTableMetadata;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.utils.EstimatedHistogram;

/**
 * Metrics for {@link ColumnFamilyStore}.
 */
public class ColumnFamilyMetrics
{
    /** Total amount of data stored in the memtable, including column related overhead. */
    public final Gauge<Long> memtableDataSize;
    /** Total number of columns present in the memtable. */
    public final Gauge<Long> memtableColumnsCount;
    /** Number of times flush has resulted in the memtable being switched out. */
    public final Counter memtableSwitchCount;
    /** Current compression ratio for all SSTables */
    public final Gauge<Double> compressionRatio;
    /** Histogram of estimated row size (in bytes). */
    public final Gauge<long[]> estimatedRowSizeHistogram;
    /** Histogram of estimated number of columns. */
    public final Gauge<long[]> estimatedColumnCountHistogram;
    /** Histogram of the number of sstable data files accessed per read */
    public final Histogram sstablesPerReadHistogram;
    /** (Local) read metrics */
    public final LatencyMetrics readLatency;
    /** (Local) write metrics */
    public final LatencyMetrics writeLatency;
    /** Estimated number of tasks pending for this column family */
    public final Gauge<Integer> pendingTasks;
    /** Number of SSTables on disk for this CF */
    public final Gauge<Integer> liveSSTableCount;
    /** Disk space used by SSTables belonging to this CF */
    public final Counter liveDiskSpaceUsed;
    /** Total disk space used by SSTables belonging to this CF, including obsolete ones waiting to be GC'd */
    public final Counter totalDiskSpaceUsed;
    /** Size of the smallest compacted row */
    public final Gauge<Long> minRowSize;
    /** Size of the largest compacted row */
    public final Gauge<Long> maxRowSize;
    /** Size of the smallest compacted row */
    public final Gauge<Long> meanRowSize;
    /** Number of false positives in bloom filter */
    public final Gauge<Long> bloomFilterFalsePositives;
    /** Number of false positives in bloom filter from last read */
    public final Gauge<Long> recentBloomFilterFalsePositives;
    /** False positive ratio of bloom filter */
    public final Gauge<Double> bloomFilterFalseRatio;
    /** False positive ratio of bloom filter from last read */
    public final Gauge<Double> recentBloomFilterFalseRatio;
    /** Disk space used by bloom filter */
    public final Gauge<Long> bloomFilterDiskSpaceUsed;
    /** Key cache hit rate  for this CF */
    public final Gauge<Double> keyCacheHitRate;
    /** Tombstones scanned in queries on this CF */
    public final Histogram tombstoneScannedHistogram;
    /** Live cells scanned in queries on this CF */
    public final Histogram liveScannedHistogram;

    public final Timer coordinatorReadLatency;
    public final Timer coordinatorScanLatency;

    private final MetricNameFactory factory;

    public final Counter speculativeRetries;

    // for backward compatibility
    @Deprecated public final EstimatedHistogram sstablesPerRead = new EstimatedHistogram(35);
    @Deprecated public final EstimatedHistogram recentSSTablesPerRead = new EstimatedHistogram(35);

    /**
     * Creates metrics for given {@link ColumnFamilyStore}.
     *
     * @param cfs ColumnFamilyStore to measure metrics
     */
    public ColumnFamilyMetrics(final ColumnFamilyStore cfs)
    {
        factory = new ColumnFamilyMetricNameFactory(cfs);

        memtableColumnsCount = Metrics.newGauge(factory.createMetricName("MemtableColumnsCount"), new Gauge<Long>()
        {
            public Long value()
            {
                return cfs.getDataTracker().getMemtable().getOperations();
            }
        });
        memtableDataSize = Metrics.newGauge(factory.createMetricName("MemtableDataSize"), new Gauge<Long>()
        {
            public Long value()
            {
                return cfs.getDataTracker().getMemtable().getLiveSize();
            }
        });
        memtableSwitchCount = Metrics.newCounter(factory.createMetricName("MemtableSwitchCount"));
        estimatedRowSizeHistogram = Metrics.newGauge(factory.createMetricName("EstimatedRowSizeHistogram"), new Gauge<long[]>()
        {
            public long[] value()
            {
                long[] histogram = new long[90];
                for (SSTableReader sstable : cfs.getSSTables())
                {
                    long[] rowSize = sstable.getEstimatedRowSize().getBuckets(false);
                    for (int i = 0; i < histogram.length; i++)
                        histogram[i] += rowSize[i];
                }
                return histogram;
            }
        });
        estimatedColumnCountHistogram = Metrics.newGauge(factory.createMetricName("EstimatedColumnCountHistogram"), new Gauge<long[]>()
        {
            public long[] value()
            {
                long[] histogram = new long[90];
                for (SSTableReader sstable : cfs.getSSTables())
                {
                    long[] columnSize = sstable.getEstimatedColumnCount().getBuckets(false);
                    for (int i = 0; i < histogram.length; i++)
                        histogram[i] += columnSize[i];
                }
                return histogram;
            }
        });
        sstablesPerReadHistogram = Metrics.newHistogram(factory.createMetricName("SSTablesPerReadHistogram"), true);
        compressionRatio = Metrics.newGauge(factory.createMetricName("CompressionRatio"), new Gauge<Double>()
        {
            public Double value()
            {
                double sum = 0;
                int total = 0;
                for (SSTableReader sstable : cfs.getSSTables())
                {
                    if (sstable.getCompressionRatio() != SSTableMetadata.NO_COMPRESSION_RATIO)
                    {
                        sum += sstable.getCompressionRatio();
                        total++;
                    }
                }
                return total != 0 ? (double) sum / total : 0;
            }
        });
        readLatency = new LatencyMetrics(factory, "Read");
        writeLatency = new LatencyMetrics(factory, "Write");
        pendingTasks = Metrics.newGauge(factory.createMetricName("PendingTasks"), new Gauge<Integer>()
        {
            public Integer value()
            {
                // TODO this actually isn't a good measure of pending tasks
                return Keyspace.switchLock.getQueueLength();
            }
        });
        liveSSTableCount = Metrics.newGauge(factory.createMetricName("LiveSSTableCount"), new Gauge<Integer>()
        {
            public Integer value()
            {
                return cfs.getDataTracker().getSSTables().size();
            }
        });
        liveDiskSpaceUsed = Metrics.newCounter(factory.createMetricName("LiveDiskSpaceUsed"));
        totalDiskSpaceUsed = Metrics.newCounter(factory.createMetricName("TotalDiskSpaceUsed"));
        minRowSize = Metrics.newGauge(factory.createMetricName("MinRowSize"), new Gauge<Long>()
        {
            public Long value()
            {
                long min = 0;
                for (SSTableReader sstable : cfs.getSSTables())
                {
                    if (min == 0 || sstable.getEstimatedRowSize().min() < min)
                        min = sstable.getEstimatedRowSize().min();
                }
                return min;
            }
        });
        maxRowSize = Metrics.newGauge(factory.createMetricName("MaxRowSize"), new Gauge<Long>()
        {
            public Long value()
            {
                long max = 0;
                for (SSTableReader sstable : cfs.getSSTables())
                {
                    if (sstable.getEstimatedRowSize().max() > max)
                        max = sstable.getEstimatedRowSize().max();
                }
                return max;
            }
        });
        meanRowSize = Metrics.newGauge(factory.createMetricName("MeanRowSize"), new Gauge<Long>()
        {
            public Long value()
            {
                long sum = 0;
                long count = 0;
                for (SSTableReader sstable : cfs.getSSTables())
                {
                    sum += sstable.getEstimatedRowSize().mean();
                    count++;
                }
                return count > 0 ? sum / count : 0;
            }
        });
        bloomFilterFalsePositives = Metrics.newGauge(factory.createMetricName("BloomFilterFalsePositives"), new Gauge<Long>()
        {
            public Long value()
            {
                long count = 0L;
                for (SSTableReader sstable: cfs.getSSTables())
                    count += sstable.getBloomFilterFalsePositiveCount();
                return count;
            }
        });
        recentBloomFilterFalsePositives = Metrics.newGauge(factory.createMetricName("RecentBloomFilterFalsePositives"), new Gauge<Long>()
        {
            public Long value()
            {
                long count = 0L;
                for (SSTableReader sstable: cfs.getSSTables())
                    count += sstable.getRecentBloomFilterFalsePositiveCount();
                return count;
            }
        });
        bloomFilterFalseRatio = Metrics.newGauge(factory.createMetricName("BloomFilterFalseRatio"), new Gauge<Double>()
        {
            public Double value()
            {
                long falseCount = 0L;
                long trueCount = 0L;
                for (SSTableReader sstable: cfs.getSSTables())
                {
                    falseCount += sstable.getBloomFilterFalsePositiveCount();
                    trueCount += sstable.getBloomFilterTruePositiveCount();
                }
                if (falseCount == 0L && trueCount == 0L)
                    return 0d;
                return (double) falseCount / (trueCount + falseCount);
            }
        });
        recentBloomFilterFalseRatio = Metrics.newGauge(factory.createMetricName("RecentBloomFilterFalseRatio"), new Gauge<Double>()
        {
            public Double value()
            {
                long falseCount = 0L;
                long trueCount = 0L;
                for (SSTableReader sstable: cfs.getSSTables())
                {
                    falseCount += sstable.getRecentBloomFilterFalsePositiveCount();
                    trueCount += sstable.getRecentBloomFilterTruePositiveCount();
                }
                if (falseCount == 0L && trueCount == 0L)
                    return 0d;
                return (double) falseCount / (trueCount + falseCount);
            }
        });
        bloomFilterDiskSpaceUsed = Metrics.newGauge(factory.createMetricName("BloomFilterDiskSpaceUsed"), new Gauge<Long>()
        {
            public Long value()
            {
                long total = 0;
                for (SSTableReader sst : cfs.getSSTables())
                    total += sst.getBloomFilterSerializedSize();
                return total;
            }
        });
        speculativeRetries = Metrics.newCounter(factory.createMetricName("SpeculativeRetries"));
        keyCacheHitRate = Metrics.newGauge(factory.createMetricName("KeyCacheHitRate"), new RatioGauge()
        {
            protected double getNumerator()
            {
                long hits = 0L;
                for (SSTableReader sstable : cfs.getSSTables())
                    hits += sstable.getKeyCacheHit();
                return hits;
            }

            protected double getDenominator()
            {
                long requests = 0L;
                for (SSTableReader sstable : cfs.getSSTables())
                    requests += sstable.getKeyCacheRequest();
                return Math.max(requests, 1); // to avoid NaN.
            }
        });
        tombstoneScannedHistogram = Metrics.newHistogram(factory.createMetricName("TombstoneScannedHistogram"), true);
        liveScannedHistogram = Metrics.newHistogram(factory.createMetricName("LiveScannedHistogram"), true);
        coordinatorReadLatency = Metrics.newTimer(factory.createMetricName("CoordinatorReadLatency"), TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
        coordinatorScanLatency = Metrics.newTimer(factory.createMetricName("CoordinatorScanLatency"), TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
    }

    public void updateSSTableIterated(int count)
    {
        sstablesPerReadHistogram.update(count);
        recentSSTablesPerRead.add(count);
        sstablesPerRead.add(count);
    }

    /**
     * Release all associated metrics.
     */
    public void release()
    {
        readLatency.release();
        writeLatency.release();
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("MemtableColumnsCount"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("MemtableDataSize"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("MemtableSwitchCount"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("CompressionRatio"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("EstimatedRowSizeHistogram"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("EstimatedColumnCountHistogram"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("SSTablesPerReadHistogram"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("PendingTasks"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("LiveSSTableCount"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("LiveDiskSpaceUsed"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("TotalDiskSpaceUsed"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("MinRowSize"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("MaxRowSize"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("MeanRowSize"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("BloomFilterFalsePositives"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("RecentBloomFilterFalsePositives"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("BloomFilterFalseRatio"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("RecentBloomFilterFalseRatio"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("BloomFilterDiskSpaceUsed"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("KeyCacheHitRate"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("SpeculativeRetry"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("TombstoneScannedHistogram"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("LiveScannedHistogram"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("CoordinatorReadLatency"));
        Metrics.defaultRegistry().removeMetric(factory.createMetricName("CoordinatorScanLatency"));
    }

    class ColumnFamilyMetricNameFactory implements MetricNameFactory
    {
        private final String keyspaceName;
        private final String columnFamilyName;
        private final boolean isIndex;

        ColumnFamilyMetricNameFactory(ColumnFamilyStore cfs)
        {
            this.keyspaceName = cfs.keyspace.getName();
            this.columnFamilyName = cfs.name;
            isIndex = cfs.isIndex();
        }

        public MetricName createMetricName(String metricName)
        {
            String groupName = ColumnFamilyMetrics.class.getPackage().getName();
            String type = isIndex ? "IndexColumnFamily" : "ColumnFamily";

            StringBuilder mbeanName = new StringBuilder();
            mbeanName.append(groupName).append(":");
            mbeanName.append("type=").append(type);
            mbeanName.append(",keyspace=").append(keyspaceName);
            mbeanName.append(",scope=").append(columnFamilyName);
            mbeanName.append(",name=").append(metricName);

            return new MetricName(groupName, type, metricName, keyspaceName + "." + columnFamilyName, mbeanName.toString());
        }
    }
}
