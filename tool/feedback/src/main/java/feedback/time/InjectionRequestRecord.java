package feedback.time;

import org.joda.time.DateTime;

import java.io.Serializable;

public final class InjectionRequestRecord extends Timing implements Serializable {
    public final String thread;
    public final int injection;
    public final int logLine;

    public InjectionRequestRecord(DateTime datetime, String thread, int injection, int logLine) {
        super(datetime);
        this.thread = thread;
        this.injection = injection;
        this.logLine = logLine;
    }
}
