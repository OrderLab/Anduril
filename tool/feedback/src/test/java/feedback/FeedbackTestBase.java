package feedback;

import org.junit.jupiter.api.AfterEach;

import java.io.Serializable;

public abstract class FeedbackTestBase implements Serializable {
    @AfterEach
    public final void shutdown() {
        ThreadUtil.shutdown();
    }
}
