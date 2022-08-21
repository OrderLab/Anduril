package feedback.diff;

import feedback.ScalaUtil;
import feedback.log.LogFile;
import feedback.log.entry.LogEntry;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public final class LogFileDiff implements DiffDump {
    public static final class DistinctConsumer<T> implements Consumer<T> {
        private final Consumer<T> action;
        private final Set<T> set = new HashSet<>();

        public DistinctConsumer(final Consumer<T> action) {
            this.action = action;
        }

        @Override
        public void accept(final T element) {
            if (this.set.add(element)) {
                action.accept(element);
            }
        }
    }

    private final LogFile good, bad;
    private final Map<String, Future<ThreadDiff>> common;

    public LogFileDiff(final LogFile good, final LogFile bad) {
        this.good = good;
        this.bad = bad;
        this.common = new HashMap<>();
        // compute good threads
        final Set<String> goodThreads = new HashSet<>();
        for (final LogEntry logEntry : this.good.entries()) {
            goodThreads.add(logEntry.thread());
        }
        // compute common threads;
        final Set<String> commonThreads = new HashSet<>();
        for (final LogEntry logEntry : bad.entries()) {
            if (goodThreads.contains(logEntry.thread())) {
                commonThreads.add(logEntry.thread());
            }
        }
        // get entries for each common thread
        final Map<String, ArrayList<LogEntry>> goodCommon = new HashMap<>();
        final Map<String, ArrayList<LogEntry>> badCommon = new HashMap<>();
        for (final String thread : commonThreads) {
            goodCommon.put(thread, new ArrayList<>());
            badCommon.put(thread, new ArrayList<>());
        }
        for (final LogEntry logEntry : this.good.entries()) {
            if (commonThreads.contains(logEntry.thread())) {
                goodCommon.get(logEntry.thread()).add(logEntry);
            }
        }
        for (final LogEntry logEntry : this.bad.entries()) {
            if (commonThreads.contains(logEntry.thread())) {
                badCommon.get(logEntry.thread()).add(logEntry);
            }
        }
        // compute diff for each common thread
        for (final String thread : commonThreads) {
            final ThreadDiff.Builder builder =
                    new ThreadDiff.Builder(thread, goodCommon.get(thread), badCommon.get(thread));
            common.put(thread, ScalaUtil.submit(builder::build));
        }
    }

    // don't filter the duplicate entries
    // TODO: filter them
    @Override
    public void dumpBadDiff(final Consumer<ThreadDiff.CodeLocation> action) {
        try {
            for (final Future<ThreadDiff> diff : common.values()) {
                diff.get().dumpBadDiff(action);
            }
            for (final LogEntry logEntry : this.bad.entries()) {
                if (!common.containsKey(logEntry.thread())) {
                    action.accept(new ThreadDiff.CodeLocation(logEntry));
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
