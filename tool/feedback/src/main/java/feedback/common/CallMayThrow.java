package feedback.common;

import java.util.concurrent.Callable;

// Do not define with Scala so that we can preserve the Java exception checking
public interface CallMayThrow<V> extends Runnable, Callable<V> {
    @Override
    default V call() {
        try {
            return callMayThrow();
        } catch (final java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    default void run() {
        call();
    }

    V callMayThrow() throws java.io.IOException;
}
