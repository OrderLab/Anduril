package feedback.diff;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public interface DiffDump extends Serializable {
    void dumpBadDiff(final Consumer<ThreadDiff.CodeLocation> action);
}
