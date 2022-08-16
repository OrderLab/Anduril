package feedback.parser;

import feedback.time.Timing;
import org.joda.time.DateTime;

import java.io.Serializable;

public final class LogEntry extends Timing implements Serializable {
    public final LogType logType;
    public final String thread;
    public final String file;
    public final int fileLogLine;
    public final String msg;
    public final int logLine;
    public final NestedException[] exceptions;

    public LogEntry(DateTime datetime, LogType logType, String thread, String file, int fileLogLine, String msg, int logLine, NestedException[] exceptions) {
        super(datetime);
        this.logType = logType;
        this.thread = thread;
        this.file = file;
        this.fileLogLine = fileLogLine;
        this.msg = msg;
        this.logLine = logLine;
        this.exceptions = exceptions;
    }
}
