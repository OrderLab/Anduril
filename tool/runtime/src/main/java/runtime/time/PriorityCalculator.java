package runtime.time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.TraceAgent;

import java.util.ArrayList;
import java.util.Collections;

final class PriorityCalculator {
    private static final Logger LOG = LoggerFactory.getLogger(PriorityCalculator.class);

    private enum Mode {
        ADD,
        TIMES,
    }

    private static final Mode mode;

    static {
        switch (TraceAgent.config.timeFeedbackMode) {
            case "add"   : mode = Mode.ADD; break;
            case "times" : mode = Mode.TIMES; break;
            default: mode = null; LOG.error("invalid time feedback formula"); System.exit(-1);
        }
    }

    static long compute(final int location, final int time) {
        switch (mode) {
            case ADD   : return location + time;
            case TIMES : return ((long) location) * time;
            default: throw new RuntimeException("invalid formula");
        }
    }

    // TODO: improve to O(n)
    static long kth(final ArrayList<Long> arr, final int k) {
        Collections.sort(arr);
        return arr.get(k - 1);
    }
}
