package feedback.diff;

import feedback.common.ActionMayThrow;
import feedback.common.ThreadUtil;
import feedback.log.LogFile;
import feedback.log.entry.LogEntry;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class LogFileDiff implements DiffDump {
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
            common.put(thread, ThreadUtil.submit(builder::build));
        }
    }

    // don't filter the duplicate entries
    // TODO: filter them
    @Override
    public void dumpBadDiff(final ActionMayThrow<ThreadDiff.CodeLocation> action)
            throws ExecutionException, InterruptedException {
        for (final Future<ThreadDiff> diff : common.values()) {
            diff.get().dumpBadDiff(action);
        }
        for (final LogEntry logEntry : this.bad.entries()) {
            if (!common.containsKey(logEntry.thread())) {
                action.accept(new ThreadDiff.CodeLocation(logEntry));
            }
        }
    }
}
