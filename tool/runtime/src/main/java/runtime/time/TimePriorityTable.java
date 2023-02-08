package runtime.time;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public final class TimePriorityTable implements Serializable {
    public final boolean distributed;
    public final int nodes;

    // injection -> (pid,occur) -> log -> priority
    public final Map<Integer, Map<Key, UtilityReducer>> injections = new TreeMap<>();

    // injection -> log -> priority
    public final Map<Integer, Map<Integer, Integer>> distances = new TreeMap<>();

    // (pid,injection) -> # of occur
    public final Map<BoundaryKey, Integer> boundaries = new HashMap<>();

    public final static class Key implements Comparable<Key>, Serializable {
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

        @Override
        public int compareTo(final Key o) {
            if (pid == o.pid) {
                return occurrence - o.occurrence;
            }
            return pid - o.pid;
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
        public final Map<Integer, Integer> timePriorities = new TreeMap<>();     // log location -> time priority
        public final Map<Integer, Integer> locationPriorities = new TreeMap<>(); // log location -> location priority
    }

    public TimePriorityTable(final boolean distributed, final int nodes) {
        this.distributed = distributed;
        this.nodes = nodes;
    }

    static public TimePriorityTable load(final String timePriorityTable) {
        try (final ObjectInputStream objectInputStream = new ObjectInputStream(
                Files.newInputStream(Paths.get(timePriorityTable)))) {
            return (TimePriorityTable) objectInputStream.readObject();
        } catch (final IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
