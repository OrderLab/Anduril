package runtime.time;

import java.io.Serializable;
import java.util.*;

public final class TimePriorityTable implements Serializable {
    public final boolean distributed;
    public final int nodes;

    // injection -> (pid,occur) -> log -> priority
    public final Map<Integer, Map<Key, UtilityReducer>> injections = new TreeMap<>();

    // (pid,injection) -> # of occur
    public final Map<BoundaryKey, Integer> boundaries = new HashMap<>();

    public final static class Key implements Serializable {
        public final int pid, occurrence;
        public Key(final int pid, final int occurrence) {
            this.pid = pid;
            this.occurrence = occurrence;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return pid == key.pid && occurrence == key.occurrence;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pid, occurrence);
        }
    }

    public final static class BoundaryKey implements Serializable {
        public final int pid, injection;
        public BoundaryKey(final int pid, final int injection) {
            this.pid = pid;
            this.injection = injection;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BoundaryKey)) return false;
            BoundaryKey that = (BoundaryKey) o;
            return pid == that.pid && injection == that.injection;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pid, injection);
        }
    }

    public final static class UtilityReducer implements Serializable {
        public Map<Integer, Integer> timePriorities = new TreeMap<>();  // location -> time priority
        private long utility = 0;
        private int size = 0;

        public void add(final int location, final int priority) {
            if (!timePriorities.containsKey(location)) {
                return;
            }
            utility += PriorityCalculator.compute(timePriorities.get(location), priority);
            size++;
        }

        public long computeUtility(final ArrayList<Long> priorities) {
            final long priority = utility / size;
            priorities.add(priority);
            return priority;
        }
    }

    public TimePriorityTable(final boolean distributed, final int nodes) {
        this.distributed = distributed;
        this.nodes = nodes;
    }

//    public TimePriorityTable(final TreeMap<Integer, int[]> standalone) {
//        this.standalone = standalone;
//        this.nodes = null;
//        this.size = standalone.values().stream().map(a -> a.length).reduce(0, Integer::sum);
//    }
//
//    public TimePriorityTable(final TreeMap<Integer, int[]>[] nodes) {
//        this.standalone = null;
//        this.nodes = new TreeMap<>();
//        Arrays.stream(nodes).forEach(m -> m.forEach((k, v) -> this.nodes.putIfAbsent(k, new int[nodes.length][])));
//        for (int i = 0; i < nodes.length; i++) {
//            final int finalI = i;
//            nodes[i].forEach((k, v) -> this.nodes.get(k)[finalI] = v);
//        }
//        this.size = Arrays.stream(nodes).map(
//                m -> m.values().stream().map(a -> a.length).reduce(0, Integer::sum)
//        ).reduce(0, Integer::sum);
//    }
}
