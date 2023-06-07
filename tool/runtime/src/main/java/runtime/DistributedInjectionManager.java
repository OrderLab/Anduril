package runtime;

import com.oracle.tools.packager.Log;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedInjectionManager extends LocalInjectionManager {
    public static class ProcessRecord {
        public final ConcurrentMap<Integer, Integer> id2times = new ConcurrentHashMap<>();
        public final ConcurrentMap<Integer, ConcurrentMap<Integer,LocalDateTime>> id2times2time = new ConcurrentHashMap<>();
//        public final Map<Integer, Integer> thread2block = new TreeMap<>();
//        public final Map<Integer, Integer> block2times = new TreeMap<>();
    }
    private final ProcessRecord[] processRecords;
    private final AtomicInteger injectionCounter = new AtomicInteger();

    public DistributedInjectionManager(final int processNumber,
                                       final String trialsPath,
                                       final String injectionPointsPath,
                                       final String injectionResultPath) {
        super(trialsPath, injectionPointsPath, injectionResultPath);
        processRecords = new ProcessRecord[processNumber];
        for (int i = 0; i < processNumber; i++) {
            processRecords[i] = new ProcessRecord();
        }
    }

    public int inject(final int pid, final int id, final int blockId) {
        if (injected.get()) {
            return 0;
        }
        if (!TraceAgent.config.isTimeFeedback && !feedbackManager.isAllowed(id)) {
            return 0;
        }
        final ProcessRecord record = processRecords[pid];
        final String name = id2name.get(id);
        if (name != null) {
            // WARN: it assumes that each injection point (id) has distinct name instance
            synchronized (name) {
                final int occurrence = record.id2times.getOrDefault(id, 0) + 1;
                record.id2times.put(id, occurrence);
                if (TraceAgent.config.fixPointInjectionMode) {
                    if (id == TraceAgent.config.targetId) {
                        if (injectionCounter.incrementAndGet() == TraceAgent.config.times) {
                            return 1;
                        }
                    }
                    return 0;
                }
                final InjectionIndex index = new InjectionIndex(pid, id, name, occurrence, blockId);
                if (TraceAgent.config.isTimeFeedback) {
                    if (feedbackManager.isAllowed(pid, id, occurrence) && !injectionSet.containsKey(index) &&
                            injected.compareAndSet(false, true)) {
                        injectionPoint = index;
                        return 1;
                    }
                    return 0;
                }
                final boolean ok;
                if (TraceAgent.config.isProbabilityFeedback) {
                    ok = Math.random() < TraceAgent.config.probability;
                } else {
                    ok = occurrence <= TraceAgent.config.injectionOccurrenceLimit;
                }
                if (ok && !injectionSet.containsKey(index) &&
                        injected.compareAndSet(false, true)) {
                    injectionPoint = index;
                    return 1;
                }
            }
        }
        return 0;
    }

    // Note: This method also utilize the id2Times for counter
    public void recordInjectionTime(final int pid, final int id) {
        LocalDateTime now = LocalDateTime.now();
        final ProcessRecord record = processRecords[pid];
        record.id2times2time.putIfAbsent(id,new ConcurrentHashMap<>());
        final ConcurrentMap<Integer,LocalDateTime> injection_trace = record.id2times2time.get(id);
        final int occurrence;
        // Get the current occurrence
        // Warn: should not be called at the same time with inject()
        synchronized (injection_trace) {
            occurrence = record.id2times.getOrDefault(id, 0) + 1;
            record.id2times.put(id, occurrence);
        }
        injection_trace.put(occurrence,now);
    }

    public void printRecordInjectionTime() {
        for (ProcessRecord record:processRecords) {
            Log.info(record.id2times2time.toString());
        }
    }

}
