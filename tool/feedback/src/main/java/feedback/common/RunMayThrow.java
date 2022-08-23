package feedback.common;

import java.util.concurrent.Callable;

// Do not define with Scala so that we can preserve the Java exception checking
public interface RunMayThrow extends Runnable, Callable<Void> {
    @Override
    default Void call() {
        run();
        return null;
    }

    @Override
    default void run() {
        try {
            runMayThrow();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    void runMayThrow() throws Exception;
}
