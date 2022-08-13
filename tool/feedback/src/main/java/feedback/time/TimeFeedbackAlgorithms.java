package feedback.time;

import feedback.Symptoms;
import feedback.diff.ThreadDiff;
import feedback.parser.DistributedLog;
import feedback.parser.LogEntry;
import org.joda.time.DateTime;
import runtime.graph.PriorityGraph;
import runtime.time.TimePriorityTable;

import javax.json.JsonObject;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public final class TimeFeedbackAlgorithms {
    private static abstract class Timing {
        final DateTime time;
        Timing(final DateTime time) {
            this.time = time;
        }
    }

    private static final class InjectionTiming extends Timing {
        final int pid;
        final int injection;
        final int occurrence;
        InjectionTiming(final DateTime time, final int pid, final int injection, final int occurrence) {
            super(time);
            this.pid = pid;
            this.injection = injection;
            this.occurrence = occurrence;
        }
    }

    private static class LogTiming extends Timing {
        LogTiming(final DateTime time) {
            super(time);
        }
    }

    private static final class CriticalLogTiming extends LogTiming {
        final int id;
        CriticalLogTiming(final DateTime time, final int id) {
            super(time);
            this.id = id;
        }
    }

    public static TimePriorityTable computeTimeFeedback(final ThreadDiff.ThreadLogEntry[] events,
                                                        final JsonObject spec,
                                                        final DistributedLog good,
                                                        final DistributedLog bad) {
        final TimeDifference timeDifference = new TimeDifference(good, bad);
        final List<Timing> timeline = new ArrayList<>();
        final Map<Integer, Integer> occurrences = new TreeMap<>();
        final BiConsumer<Integer, InjectionRequestRecord[]> putInjection = (pid, records) ->
                Arrays.stream(records).forEach(record -> timeline.add(new InjectionTiming(
                        timeDifference.good2bad(record.datetime), pid, record.injection,
                        occurrences.merge(record.injection, 1, Integer::sum))));
        if (good.distributed) {
            for (int i = 0; i < good.logs.length; i++) {
                putInjection.accept(i, good.logs[i].injections);
            }
        } else {
            putInjection.accept(-1, good.logs[0].injections);
        }
        final boolean isSymptomLogged = Symptoms.isSymptomLogged(spec);
        final BiConsumer<Integer, LogEntry[]> putLog = (pid, entries) ->
                Arrays.stream(entries).forEach(entry -> {
                    int i = isSymptomLogged ? 0 : 1;
                    for (; i < events.length; i++) {
                        if (events[i].file.equals(entry.file) && events[i].line == entry.fileLogLine) {
                            break;
                        }
                    }
                    if (i < events.length) {
                        timeline.add(new CriticalLogTiming(entry.datetime, pid));
                    } else {
                        timeline.add(new LogTiming(entry.datetime));
                    }
                });
        if (bad.distributed) {
            for (int i = 0; i < bad.logs.length; i++) {
                putLog.accept(i, bad.logs[i].entries);
            }
        } else {
            putLog.accept(-1, bad.logs[0].entries);
        }
        if (!isSymptomLogged) {
            Symptoms.getSymptom(bad, spec).forEach((pid, entries) -> entries.forEach(entry ->
                    timeline.add(new CriticalLogTiming(entry.datetime, 0))));
        }
        // list sort is stable merge sort; don't use array sort, which is unstable quick sort and discards the order
        timeline.sort(Comparator.comparing(timing -> timing.time));
        final TimePriorityTable table = new TimePriorityTable(bad.distributed, bad.logs.length);
        return computeTimeFeedback(timeline.toArray(new Timing[0]), events.length, new PriorityGraph(spec), table);
    }

    private static TimePriorityTable computeTimeFeedback(final Timing[] timeline,
                                                 final int eventNumber,
                                                 final PriorityGraph graph,
                                                 final TimePriorityTable table) {
        IntStream.range(0, eventNumber).forEach(i -> {
            final Set<Integer> reachable = new TreeSet<>();
            graph.calculatePriorities(i, 0, (injection, weight) -> reachable.add(injection));
            final Function<Timing, InjectionTiming> getId = timing -> {
                if (!(timing instanceof InjectionTiming)) {
                    return null;
                }
                final InjectionTiming id = ((InjectionTiming) timing);
                return reachable.contains(id.injection) ? id : null;
            };
            final UpdateAgent<Timing, InjectionTiming> update = new UpdateAgent<Timing, InjectionTiming>(
                    timeline, timing -> !(timing instanceof InjectionTiming)) {
                @Override
                int backwardDistance(final int index, final int target) {
                    return super.backwardDistance(index, target) * 3;
                }
                @Override
                void update(final InjectionTiming id, final int target, final int weight) {
                }
            };
            final Predicate<Timing> isTarget = timing ->
                    timing instanceof CriticalLogTiming && ((CriticalLogTiming) timing).id == i;
            updateTimeline(timeline, isTarget, getId, update);
        });
        return table;
    }

    /*
     * Update all utility values for all injection, given a target log
     * @param timeline   all events in the timeline, in the order of date time
     * @param isTarget   predicate for testing if it is the target log
     * @param getId      getter of the injection id; null when not injection
     * @param update     the agent for update
     */
    static <T, U> void updateTimeline(final T[] timeline,
                                   final Predicate<T> isTarget,
                                   final Function<T, U> getId,
                                   final UpdateAgent<T, U> update) {
        int prev = -1;
        int next = 0;
        while (next < timeline.length && !isTarget.test(timeline[next])) {
            next++;
        }
        for (int i = 0; i < timeline.length; i++) {
            final T timing = timeline[i];
            final U id = getId.apply(timing);
            if (isTarget.test(timing)) {
                prev = i;
                next++;
                while (next < timeline.length && !isTarget.test(timeline[next])) {
                    next++;
                }
                if (id != null) {
                    throw new RuntimeException("target log should not have an injection id");
                }
            }
            if (id == null) {
                continue;
            }
            if (prev == -1) {
                if (next == timeline.length) {
                    throw new RuntimeException("either prev or next must exist");
                }
                update.update(id, next, update.forwardDistance(i, next));
            } else {
                if (next == timeline.length) {
                    update.update(id, prev, update.backwardDistance(i, prev));
                } else {
                    final int forward = update.forwardDistance(i, next);
                    final int backward = update.backwardDistance(i, prev);
                    //  prefer forward
                    if (backward < forward) {
                        update.update(id, prev, backward);
                    } else {
                        update.update(id, next, forward);
                    }
                }
            }
        }
    }

    static class UpdateAgent<T, U> {
        private final int[] count;

        UpdateAgent(final T[] timeline, final Predicate<T> shouldCount) {
            count = new int[timeline.length + 1];
            count[0] = 0;
            for (int i = 0; i < timeline.length; i++) {
                count[i + 1] = count[i];
                if (shouldCount.test(timeline[i])) {
                    count[i + 1]++;
                }
            }
        }

        int forwardDistance(final int index, final int target) {
            // (index, target] ==> (index + 1, target + 1]
            return this.count[target + 1] - this.count[index + 1];
        }

        int backwardDistance(final int index, final int target) {
            // [target, index) ==> [target + 1, index + 1) ==> (target, index]
            return this.count[index] - this.count[target];
        }

        void update(final U id, final int target, final int weight) { }
    }
}
