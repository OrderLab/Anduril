package feedback.time;

import org.joda.time.DateTime;

public final class InjectionRequestRecord {
    public final DateTime datetime;
    public final String thread;
    public final int injection;
    public final int logLine;

    public InjectionRequestRecord(DateTime datetime, String thread, int injection, int logLine) {
        this.datetime = datetime;
        this.thread = thread;
        this.injection = injection;
        this.logLine = logLine;
    }
}
