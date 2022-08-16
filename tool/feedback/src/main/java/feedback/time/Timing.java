package feedback.time;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Objects;

public abstract class Timing implements Serializable, Comparable<Timing> {
    public final DateTime datetime;

    public Timing(final DateTime datetime) {
        this.datetime = datetime;
    }

    @Override
    public int compareTo(final Timing timing) {
        return this.datetime.compareTo(timing.datetime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Timing)) return false;
        Timing timing = (Timing) o;
        return datetime.equals(timing.datetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datetime);
    }
}
