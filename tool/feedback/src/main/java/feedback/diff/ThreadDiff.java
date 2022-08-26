package feedback.diff;

import feedback.NativeAlgorithms;
import feedback.common.ActionMayThrow;
import feedback.log.entry.LogEntry;

import java.io.Serializable;
import java.util.*;

public final class ThreadDiff implements DiffDump {
    public static final class CodeLocation implements Serializable {
        public final String classname;
        public final int fileLogLine;

        CodeLocation(final LogEntry logEntry) {
            this.classname = logEntry.classname();
            this.fileLogLine = logEntry.fileLogLine();
        }

        public CodeLocation(final String classname, final int fileLogLine) {
            this.classname = classname;
            this.fileLogLine = fileLogLine;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CodeLocation)) return false;
            CodeLocation that = (CodeLocation) o;
            return fileLogLine == that.fileLogLine && classname.equals(that.classname);
        }

        @Override
        public int hashCode() {
            return Objects.hash(classname, fileLogLine);
        }

        @Override
        public String toString() {
            return classname + " " + fileLogLine;
        }
    }

    final static class Builder {
        final String thread;
        final ArrayList<LogEntry> good, bad;

        Builder(final String thread, final ArrayList<LogEntry> good, final ArrayList<LogEntry> bad) {
            this.thread = thread;
            this.good = good;
            this.bad = bad;
        }

        ThreadDiff build() {
            return new ThreadDiff(thread, good, bad);
        }
    }

    public final String thread;
    private final CodeLocation[] good, bad;
    private final int[] diff;
    private final List<CodeLocation> badOnly;

    private static CodeLocation[] convertLogEntries(final ArrayList<LogEntry> logEntries) {
        final CodeLocation[] result = new CodeLocation[logEntries.size()];
        for (int i = 0; i < logEntries.size(); i++) {
            result[i] = new CodeLocation(logEntries.get(i));
        }
        return result;
    }

    ThreadDiff(final String thread, final ArrayList<LogEntry> good, final ArrayList<LogEntry> bad) {
        this.thread = thread;
        this.good = convertLogEntries(good);
        this.bad = convertLogEntries(bad);
        final Map<CodeLocation, Integer> map = new HashMap<>();
        for (final CodeLocation location : this.good) {
            map.putIfAbsent(location, map.size());
        }
        for (final CodeLocation location : this.bad) {
            map.putIfAbsent(location, map.size());
        }
        final int[] g = new int[this.good.length], b = new int[this.bad.length];
        for (int i = 0; i < g.length; i++) {
            g[i] = map.get(this.good[i]);
        }
        for (int i = 0; i < b.length; i++) {
            b[i] = map.get(this.bad[i]);
        }
        this.diff = NativeAlgorithms.diff(g, b);
        int i = 0, j = 0;
        this.badOnly = new ArrayList<>();
        for (final int choice : this.diff) {
            switch (choice) {
                case 0: i++; break;
                case 1: this.badOnly.add(this.bad[j]); j++; break;
                case 2: i++; j++; break;
                default: throw new RuntimeException("invalid path choice");
            }
        }
    }

    // don't filter the duplicate entries
    @Override
    public void dumpBadDiff(final ActionMayThrow<CodeLocation> action) {
        this.badOnly.forEach(action);
    }
}
