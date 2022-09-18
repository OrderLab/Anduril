package feedback.diff;

import feedback.common.ActionMayThrow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface DiffDump extends Serializable {
    ArrayList<ThreadDiff.CodeLocation> sortCodeLocationInThreadOrder()
            throws ExecutionException, InterruptedException;

    void dumpBadDiff(final ActionMayThrow<ThreadDiff.CodeLocation> action)
            throws ExecutionException, InterruptedException;
}
