package feedback.diff;

import feedback.NativeAlgorithms;
import feedback.common.ActionMayThrow;
import feedback.log.entry.LogEntry;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutionException;

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

    private final static int THRESHOLD = 300;

    public final String thread;
    public final scala.Tuple2<Integer, Integer>[] common;
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
        final CodeLocation[] goodLocations = convertLogEntries(good);
        final CodeLocation[] badLocations = convertLogEntries(bad);
        if (goodLocations.length * badLocations.length < THRESHOLD) {
            final FastDiff<CodeLocation> diff = new FastDiff<>(goodLocations, badLocations);
            this.badOnly = diff.badOnly;
            this.common = new scala.Tuple2[diff.common];
            for (int i = 0; i < diff.common; i++) {
                this.common[i] = new scala.Tuple2<>(good.get(diff.intervals[i]._1).logLine(),
                        bad.get(diff.intervals[i]._2).logLine());
            }
        } else {
            final Map<CodeLocation, Integer> map = new HashMap<>();
            for (final CodeLocation location : goodLocations) {
                map.putIfAbsent(location, map.size());
            }
            for (final CodeLocation location : badLocations) {
                map.putIfAbsent(location, map.size());
            }
            final int[] g = new int[goodLocations.length], b = new int[badLocations.length];
            for (int i = 0; i < g.length; i++) {
                g[i] = map.get(goodLocations[i]);
            }
            for (int i = 0; i < b.length; i++) {
                b[i] = map.get(badLocations[i]);
            }
            final int[] diff = NativeAlgorithms.diff(g, b);
            final List<scala.Tuple2<Integer, Integer>> common = new ArrayList<>();
            int i = 0, j = 0;
            this.badOnly = new ArrayList<>();
            for (final int choice : diff) {
                switch (choice) {
                    case 0:
                        i++;
                        break;
                    case 1:
                        this.badOnly.add(badLocations[j]);
                        j++;
                        break;
                    case 2:
                        common.add(new scala.Tuple2<>(good.get(i).logLine(), bad.get(j).logLine()));
                        i++;
                        j++;
                        break;
                    default:
                        throw new RuntimeException("invalid path choice");
                }
            }
            this.common = common.toArray(new scala.Tuple2[0]);
        }
    }

    private static CodeLocation[] convertCodeLocations(final ArrayList<ThreadDiff.CodeLocation> codeLocations) {
        final CodeLocation[] result = new CodeLocation[codeLocations.size()];
        for (int i = 0; i < codeLocations.size(); i++) {
            result[i] = codeLocations.get(i);
        }
        return result;
    }

    public ThreadDiff(final ArrayList<ThreadDiff.CodeLocation> good,
                                    final ArrayList<ThreadDiff.CodeLocation> bad)  {
        this.thread = null;
        this.common = null;
        final CodeLocation[] goodLocations = convertCodeLocations(good);
        final CodeLocation[] badLocations = convertCodeLocations(bad);
        final FastDiff<CodeLocation> diff = new FastDiff<>(goodLocations, badLocations);
        this.badOnly = diff.badOnly;
    }

    // Should not be used at all
    @Override
    public ArrayList<CodeLocation> sortCodeLocationInThreadOrder() {
        return null;
    }

    // don't filter the duplicate entries
    @Override
    public void dumpBadDiff(final ActionMayThrow<CodeLocation> action) {
        this.badOnly.forEach(action);
    }
}
