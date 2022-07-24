package feedback;

import feedback.diff.DistributedLogDiff;
import feedback.diff.LogDiff;
import feedback.diff.ThreadDiff;
import feedback.parser.DistributedLog;

import javax.json.JsonObject;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;

final class Algorithm {
    static void computeTimeFeedback(final DistributedLog good, final DistributedLog bad, final DistributedLog trial,
                                    final JsonObject spec, final Consumer<Object[]> consumer) {
    }

    static void computeDiff(final DistributedLog good, final DistributedLog bad,
                            final Consumer<ThreadDiff.ThreadLogEntry> consumer) throws Exception {
        if (good.distributed ^ bad.distributed) {
            throw new Exception("distributed mode not matched");
        }
        if (good.distributed) {
            if (!Arrays.equals(Arrays.stream(good.dirs).map(File::getName).toArray(String[]::new),
                    Arrays.stream(good.dirs).map(File::getName).toArray(String[]::new))) {
                throw new Exception("distributed log not matched");
            }
            new DistributedLogDiff(good, bad).dumpBadDiff(consumer);
        } else {
            new LogDiff(good.logs[0], bad.logs[0]).dumpBadDiff(consumer);
        }
    }

    static void computeLocationFeedback(final DistributedLog good, final DistributedLog bad, final DistributedLog trial,
                                        final JsonObject spec, final Consumer<Integer> consumer) {
    }
}
