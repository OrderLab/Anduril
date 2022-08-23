package feedback.common;

import java.util.function.Function;

public interface FunctionMayThrow<T, R> extends Function<T, R> {
    @Override
    default R apply(final T t) {
        try {
            return applyMayThrow(t);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    R applyMayThrow(final T t) throws Exception;
}
