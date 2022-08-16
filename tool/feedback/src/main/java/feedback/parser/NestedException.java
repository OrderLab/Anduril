package feedback.parser;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public final class NestedException implements Serializable {
    public final String exception;
    public final String exceptionMsg;
    public final String[] stacktrace;

    public NestedException(final String exception, final String exceptionMsg, final String[] stacktrace) {
        this.exception = exception;
        this.exceptionMsg = exceptionMsg;
        this.stacktrace = stacktrace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NestedException)) return false;
        NestedException that = (NestedException) o;
        return exception.equals(that.exception) && exceptionMsg.equals(that.exceptionMsg) && Arrays.equals(stacktrace, that.stacktrace);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(exception, exceptionMsg);
        result = 31 * result + Arrays.hashCode(stacktrace);
        return result;
    }
}
