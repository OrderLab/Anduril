package parser.diff;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

final class ThreadDiff {
    static final class LogEntry {
        final String file;
        final int line;

        LogEntry(final parser.LogEntry logEntry) {
            this.file = logEntry.file;
            this.line = logEntry.fileLogLine;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LogEntry)) return false;
            LogEntry logEntry = (LogEntry) o;
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
    private final ArrayList<LogEntry> good, bad;
    private final Patch<LogEntry> patch;

    private static ArrayList<LogEntry> convertLogEntries(final ArrayList<parser.LogEntry> logEntries) {
        final ArrayList<LogEntry> result = new ArrayList<LogEntry>(logEntries.size());
        for (final parser.LogEntry logEntry : logEntries) {
            result.add(new LogEntry(logEntry));
        }
        return result;
    }

    ThreadDiff(final String thread, final ArrayList<parser.LogEntry> good, final ArrayList<parser.LogEntry> bad) {
        this.thread = thread;
        this.good = convertLogEntries(good);
        this.bad = convertLogEntries(bad);
        this.patch = DiffUtils.diff(this.good, this.bad);
    }

    void dumpBadDiff(final Consumer<LogEntry> consumer) {
        for (final AbstractDelta<LogEntry> delta : this.patch.getDeltas()) {
            switch (delta.getType()) {
                case CHANGE:
                case INSERT:
                    delta.getTarget().getLines().forEach(consumer);
                default:
            }
        }
    }
}
