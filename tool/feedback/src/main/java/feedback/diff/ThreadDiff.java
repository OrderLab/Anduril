package feedback.diff;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
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
    private final ArrayList<ThreadLogEntry> good, bad;
    private final Patch<ThreadLogEntry> patch;

    private static ArrayList<ThreadLogEntry> convertLogEntries(final ArrayList<LogEntry> logEntries) {
        final ArrayList<ThreadLogEntry> result = new ArrayList<ThreadLogEntry>(logEntries.size());
        for (final LogEntry logEntry : logEntries) {
            result.add(new ThreadLogEntry(logEntry));
        }
        return result;
    }

    ThreadDiff(final String thread, final ArrayList<LogEntry> good, final ArrayList<LogEntry> bad) {
        this.thread = thread;
        this.good = convertLogEntries(good);
        this.bad = convertLogEntries(bad);
        this.patch = DiffUtils.diff(this.good, this.bad);
    }

    void dumpBadDiff(final Consumer<ThreadLogEntry> consumer) {
        for (final AbstractDelta<ThreadLogEntry> delta : this.patch.getDeltas()) {
            switch (delta.getType()) {
                case CHANGE:
                case INSERT:
                    delta.getTarget().getLines().forEach(consumer);
                default:
            }
        }
    }
}
