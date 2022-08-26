package feedback.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.ExecutionException;

public abstract class ThreadTestBase {
//    protected interface Executable extends org.junit.jupiter.api.function.Executable {
//        @Override
//        default void execute() throws Throwable {
//            execute2();
//        }
//
//        void execute2() throws Throwable;
//    }

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
