package feedback.diff;

import feedback.common.ActionMayThrow;

import java.io.Serializable;

public interface DiffDump extends Serializable {
    void dumpBadDiff(final ActionMayThrow<ThreadDiff.CodeLocation> action) throws Exception;
}
