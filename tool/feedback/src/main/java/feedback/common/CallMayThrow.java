package feedback.common;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

// Do not define with Scala so that we can preserve the Java exception checking
public interface CallMayThrow<V> extends Runnable, Callable<V> {
    @Override
    default V call() {
        try {
            return callMayThrow();
        } catch (final IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    default void run() {
        call();
    }

    V callMayThrow() throws IOException, ExecutionException, InterruptedException;
}
