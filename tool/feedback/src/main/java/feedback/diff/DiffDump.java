package feedback.diff;

import feedback.common.ActionMayThrow;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

public interface DiffDump extends Serializable {
    void dumpBadDiff(final ActionMayThrow<ThreadDiff.CodeLocation> action)
            throws ExecutionException, InterruptedException;
}
