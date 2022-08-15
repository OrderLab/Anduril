package runtime;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DistributedInjectionManager extends LocalInjectionManager {
    public static class ProcessRecord {
        public final ConcurrentMap<Integer, Integer> id2times = new ConcurrentHashMap<>();
//        public final Map<Integer, Integer> thread2block = new TreeMap<>();
//        public final Map<Integer, Integer> block2times = new TreeMap<>();
    }
    private final ProcessRecord[] processRecords;

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
        if (!TraceAgent.isTimeFeedback && !feedbackManager.isAllowed(id)) {
            return 0;
        }
        final ProcessRecord record = processRecords[pid];
        final String name = id2name.get(id);
        if (name != null) {
            // WARN: it assumes that each injection point (id) has distinct name instance
            synchronized (name) {
                final int occurrence = record.id2times.getOrDefault(id, 0) + 1;
                record.id2times.put(id, occurrence);
                final InjectionIndex index = new InjectionIndex(pid, id, name, occurrence, blockId);
                if (TraceAgent.isTimeFeedback) {
                    if (feedbackManager.isAllowed(pid, id, occurrence) && !injectionSet.containsKey(index) &&
                            injected.compareAndSet(false, true)) {
                        injectionPoint = index;
                        return 1;
                    }
                    return 0;
                }
                final boolean ok;
                if (TraceAgent.isProbabilityFeedback) {
                    ok = Math.random() < TraceAgent.probability;
                } else {
                    ok = occurrence <= TraceAgent.injectionOccurrenceLimit;
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
}
