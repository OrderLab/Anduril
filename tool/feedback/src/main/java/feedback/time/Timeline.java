package feedback.time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.graph.PriorityGraph;
import runtime.time.TimePriorityTable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Timeline {
    private static final Logger LOG = LoggerFactory.getLogger(Timeline.class);

    public static TimePriorityTable computeTimeFeedback(final Timing[] timeline,
                                                        final int eventNumber,
                                                        final PriorityGraph graph,
                                                        final TimePriorityTable table,
                                                        final int limit ) {
        for (final Timing timing : timeline) {
            if (timing instanceof InjectionTiming) {
                final InjectionTiming id = (InjectionTiming) timing;
                table.boundaries.merge(new TimePriorityTable.BoundaryKey(id.pid(), id.injection()), 1, Integer::sum);
            }
        }
        for (int i_ = 0; i_ < eventNumber; i_++) {
            final int i = i_;
            final Map<Integer, Integer> reachable = new TreeMap<>();
            graph.calculatePriorities(i, 0, reachable::put);
            final Function<Timing, InjectionTiming> getId = timing -> {
                if (timing instanceof InjectionTiming) {
                    final InjectionTiming id = ((InjectionTiming) timing);
                    return reachable.containsKey(id.injection()) ? id : null;
                }
                return null;
            };
            final UpdateAgent<Timing, InjectionTiming> update = new UpdateAgent<Timing, InjectionTiming>(
                    timeline, timing -> !(timing instanceof InjectionTiming)) {
                @Override
                int backwardDistance(final int index, final int target) {
                    return super.backwardDistance(index, target) * 3;
                }
                @Override
                void update(final InjectionTiming id, final int target, final int weight) {
                    if (weight < 0) {
                        table.injections.computeIfAbsent(id.injection(),
                                k -> new TreeMap<>()).computeIfAbsent(new TimePriorityTable.Key(id.pid(), id.occurrence()),
                                k -> new TimePriorityTable.UtilityReducer()).timePriorities.put(i, limit);
                    } else {
                        table.injections.computeIfAbsent(id.injection(),
                                k -> new TreeMap<>()).computeIfAbsent(new TimePriorityTable.Key(id.pid(), id.occurrence()),
                                k -> new TimePriorityTable.UtilityReducer()).timePriorities.put(i, weight);
                    }
                }
            };
            final Predicate<Timing> isTarget = timing ->
                    timing instanceof CriticalLogTiming && ((CriticalLogTiming) timing).id() == i;
            if (Arrays.stream(timeline).anyMatch(timing -> getId.apply(timing) != null)) {
                updateTimeline(timeline, isTarget, getId, update, i_);
            } else {
                LOG.warn("None of the injections can trigger event {}", i);
            }
        }
        for (int i = 0; i < eventNumber; i++) {
            final Integer finalI = i;
            graph.calculatePriorities(i, 0, (injectionId, weight) ->
                table.injections.get(injectionId).forEach((m, u) ->
                        table.distances.computeIfAbsent(injectionId, k -> new HashMap<>()).computeIfAbsent(m, k -> new HashMap<>()).put(finalI, weight)));
        }
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
                                      final UpdateAgent<T, U> update,
                                      final int eventnumber) {
        int prev = -1;
        int next = 0;
        int fail = 0;
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
                    fail = 1;
                    //throw new RuntimeException("either prev or next must exist");
                    // Then the distance will be the total distance(or there may be overflow)
                    // Feeding in -1 will automatically result in length of the bad run log
                    update.update(id, next, -1);
                    continue;
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
        if (fail == 1) {
            System.out.println("Prev and next are missed in " + eventnumber + ", replaced with bad run log");
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
