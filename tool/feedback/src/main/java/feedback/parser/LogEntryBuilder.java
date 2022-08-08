package feedback.parser;

import org.joda.time.DateTime;

import java.io.Serializable;

final class LogEntryBuilder implements Serializable {
    public final DateTime datetime;
    public final String type;
    public final String thread;
    public final String file;
    public final int fileLine;

    private int logLine;
    private final StringBuilder msg;

    LogEntryBuilder(String datetimeText, String logTypeText, String locationText, String msg) {
        this.datetime = Parser.parseDatetime(datetimeText);
        this.type = Parser.parseLogType(logTypeText);
        final scala.Tuple3<String, String, Integer> location = Parser.parseLocation(locationText);
        this.thread = location._1();
        this.file = location._2();
        this.fileLine = location._3();
        this.logLine = -1;    // should be set later
        this.msg = new StringBuilder(msg);
    }

    void setLogLine(int logLine) {
        this.logLine = logLine;
    }

    int getLogLine() {
        return this.logLine;
    }

    void appendNewLine(String s) {
        this.msg.append('\n').append(s);
    }

    LogEntry build() {
        if (logLine == -1) {
            throw new RuntimeException("Bad log line");
        }
        return new LogEntry(datetime, type, thread, file, fileLine, msg.toString(), logLine);
    }

    String getMsg() {
        return msg.toString();
    }

    LogEntry buildWithoutLogLine() {
        return new LogEntry(datetime, type, thread, file, fileLine, msg.toString(), -1);
    }
}
