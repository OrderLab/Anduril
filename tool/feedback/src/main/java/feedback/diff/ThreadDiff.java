package feedback.diff;

import feedback.log.entry.LogEntry;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

public final class ThreadDiff implements DiffDump {
    public static final class CodeLocation {
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
    private final FastDiff<CodeLocation> diff;

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
        this.diff = new FastDiff<>(this.good, this.bad);
    }

    // don't filter the duplicate entries
    @Override
    public void dumpBadDiff(final Consumer<CodeLocation> action) {
        this.diff.badOnly.forEach(action);
    }
}
