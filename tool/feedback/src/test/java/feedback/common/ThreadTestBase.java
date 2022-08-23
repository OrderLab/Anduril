package feedback.common;

import org.junit.jupiter.api.AfterEach;

public abstract class ThreadTestBase {
//    protected interface Executable extends org.junit.jupiter.api.function.Executable {
//        @Override
//        default void execute() throws Throwable {
//            execute2();
//        }
//
//        void execute2() throws Throwable;
//    }

    @AfterEach
    public final void shutdown() {
        Env.shutdown();
    }
}
