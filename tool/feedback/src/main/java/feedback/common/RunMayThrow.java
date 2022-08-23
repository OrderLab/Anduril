package feedback.common;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

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
        } catch (final IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void runMayThrow() throws IOException, ExecutionException, InterruptedException;
}
