package feedback.time;

import java.io.Serializable;

public final class FeedbackTable implements Serializable {
    private int pid = -1;

    public void setPid(final int pid) {
        this.pid = pid;
    }
}
