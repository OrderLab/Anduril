package feedback.diff;

import feedback.parser.DistributedLog;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class DistributedLogDiff implements Serializable {
    private final LogDiff[] diffs;
    public DistributedLogDiff(final DistributedLog good, final DistributedLog bad) throws Exception {
        if (!good.distributed || !bad.distributed) {
            throw new Exception("not comply with distributed mode");
        }
        if (good.logs.length != bad.logs.length) {
            throw new Exception("the number of logs differs");
        }
        this.diffs = new LogDiff[good.logs.length];
        for (int i = 0; i < good.logs.length; i++) {
            this.diffs[i] = new LogDiff(good.logs[i], bad.logs[i]);
        }
    }

    public void dumpBadDiff(final Consumer<ThreadDiff.ThreadLogEntry> consumer) {
        final Set<ThreadDiff.ThreadLogEntry> entries = new HashSet<>();
        for (final LogDiff diff : this.diffs) {
            diff.dumpBadDiff(entry -> {
                if (entries.add(entry)) {
                    consumer.accept(entry);
                }
            });
        }
    }
}
