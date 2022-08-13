package feedback.time;

import feedback.parser.DistributedLog;
import org.joda.time.DateTime;

public final class TimeDifference {
    public final DateTime good, bad;
    public final long difference;  // bad - good

    public TimeDifference(final DateTime good, final DateTime bad) {
        this.good = good;
        this.bad = bad;
        this.difference = this.bad.getMillis() - this.good.getMillis();
    }

    public TimeDifference(final DistributedLog goodRun, final DistributedLog badRun) {
        DateTime good = goodRun.logs[0].entries[0].datetime, bad = badRun.logs[0].entries[0].datetime;
        for (int i = 1; i < goodRun.logs.length; i++) {
            if (good.isBefore(goodRun.logs[i].entries[0].datetime)) {
                good = goodRun.logs[i].entries[0].datetime;
                bad = badRun.logs[i].entries[0].datetime;
            }
        }
        this.good = good;
        this.bad = bad;
        this.difference = this.bad.getMillis() - this.good.getMillis();
    }

    public TimeDifference(final DateTime good, final DateTime bad, final TimeDifference timeDifference) {
        this(timeDifference.good2bad(good), bad);
    }

    // a valid difference must < 1 hour = 3,600,000 ms
    public int getTimeScore() {
        if (difference < 0) {
            return -3 * (int) difference;
        }
        return (int) difference;
    }

    public DateTime good2bad(final DateTime good) {
        return good.plus(this.difference);
    }
}
