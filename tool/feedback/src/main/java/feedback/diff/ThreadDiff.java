package feedback.diff;

import feedback.parser.LogEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

public final class ThreadDiff implements Serializable {
    public static final class ThreadLogEntry {
        final String file;
        final int line;

        ThreadLogEntry(final LogEntry logEntry) {
            this.file = logEntry.file;
            this.line = logEntry.fileLogLine;
        }

        public ThreadLogEntry(final String file, final int line) {
            this.file = file;
            this.line = line;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ThreadLogEntry)) return false;
            ThreadLogEntry logEntry = (ThreadLogEntry) o;
            return line == logEntry.line && file.equals(logEntry.file);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, line);
        }

        @Override
        public String toString() {
            return file + " " + line;
        }
    }

    public final String thread;
    private final ThreadLogEntry[] good, bad;
    private final FastDiff<ThreadLogEntry> diff;

    private static ThreadLogEntry[] convertLogEntries(final ArrayList<LogEntry> logEntries) {
        final ThreadLogEntry[] result = new ThreadLogEntry[logEntries.size()];
        for (int i = 0; i < logEntries.size(); i++) {
            result[i] = new ThreadLogEntry(logEntries.get(i));
        }
        return result;
    }

    ThreadDiff(final String thread, final ArrayList<LogEntry> good, final ArrayList<LogEntry> bad) {
        this.thread = thread;
        this.good = convertLogEntries(good);
        this.bad = convertLogEntries(bad);
        this.diff = new FastDiff<>(this.good, this.bad);
    }

    void dumpBadDiff(final Consumer<ThreadLogEntry> consumer) {
        this.diff.badOnly.forEach(consumer);
    }
}
