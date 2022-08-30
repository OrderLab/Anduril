package feedback.time;

import feedback.Algorithms.*;
import runtime.graph.PriorityGraph;
import runtime.time.TimePriorityTable;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public final class Timeline {
    public static TimePriorityTable computeTimeFeedback(final Timing[] timeline,
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
                return reachable.contains(id.injection()) ? id : null;
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
                    timing instanceof CriticalLogTiming && ((CriticalLogTiming) timing).id() == i;
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
