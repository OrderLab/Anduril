package runtime;

import runtime.time.TimeFeedbackManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedInjectionManager extends LocalInjectionManager {
    public static class ProcessRecord {
        public final ConcurrentMap<Integer, Integer> id2times = new ConcurrentHashMap<>();
        public final ConcurrentMap<Integer, ConcurrentMap<Integer, ThreadTimePair>> id2times2time = new ConcurrentHashMap<>();
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
    public void recordInjectionTime(final int pid, final int id, final String thread) {
        LocalDateTime now = LocalDateTime.now();
        final ProcessRecord record = processRecords[pid];
        record.id2times2time.putIfAbsent(id,new ConcurrentHashMap<>());
        final ConcurrentMap<Integer,ThreadTimePair> injection_trace = record.id2times2time.get(id);
        final int occurrence;
        // Get the current occurrence
        // Warn: should not be called at the same time with inject()
        synchronized (injection_trace) {
            occurrence = record.id2times.getOrDefault(id, 0) + 1;
            record.id2times.put(id, occurrence);
        }
        injection_trace.put(occurrence,new ThreadTimePair(now,thread));
    }

    public void printRecordInjectionTime() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");
        try (final PrintWriter csv = new PrintWriter(
                Files.newOutputStream(Paths.get(this.trialsPath + "/" + "InjectionTimeRecord" + ".csv")))) {
            csv.println("pid,id,occurrence,time,thread");
            for (int pid = 0; pid < processRecords.length;pid++) {
                int finalPid = pid;
                processRecords[pid].id2times2time.forEach((id, times2time)->
                        times2time.forEach((times,time_thread_pair)->
                                csv.printf("%d,%d,%d,%s,%s\n", finalPid,id,times,
                                        time_thread_pair.time.format(format),time_thread_pair.thread_name)));
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
