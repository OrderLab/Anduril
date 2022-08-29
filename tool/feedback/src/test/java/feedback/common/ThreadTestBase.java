package feedback.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.ExecutionException;

public abstract class ThreadTestBase {
    protected static final Class<ExecutionException> threadExceptionClass = ExecutionException.class;

    @BeforeEach
    public final void setup() {
        Env.enter();
    }

    @AfterEach
    public final void shutdown() {
        Env.exit();
    }
}
