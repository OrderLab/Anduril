package feedback.common;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public interface FunctionMayThrow<T, R> extends Function<T, R> {
    @Override
    default R apply(final T t) {
        try {
            return applyMayThrow(t);
        } catch (final IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    R applyMayThrow(final T t) throws IOException, ExecutionException, InterruptedException;
}
