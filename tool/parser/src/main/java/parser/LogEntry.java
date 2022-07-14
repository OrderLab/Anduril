package parser;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Objects;

public final class LogEntry implements Serializable {
    public final DateTime datetime;
    public final String type;
    public final String thread;
    public final String file;
    public final int fileLogLine;
    public final String msg;
    public final int logLine;

    public LogEntry(DateTime datetime, String type, String thread, String file, int fileLogLine, String msg, int logLine) {
        this.datetime = datetime;
        this.type = type;
        this.thread = thread;
        this.file = file;
        this.fileLogLine = fileLogLine;
        this.msg = msg;
        this.logLine = logLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogEntry)) return false;
        LogEntry logEntry = (LogEntry) o;
        return fileLogLine == logEntry.fileLogLine && logLine == logEntry.logLine && datetime.equals(logEntry.datetime) && type.equals(logEntry.type) && thread.equals(logEntry.thread) && file.equals(logEntry.file) && msg.equals(logEntry.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datetime, type, thread, file, fileLogLine, msg, logLine);
    }
}
