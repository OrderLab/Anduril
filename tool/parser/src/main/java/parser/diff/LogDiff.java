package parser.diff;

import parser.Log;

import java.util.*;
import java.util.function.Consumer;

public final class LogDiff {
    private final Log good, bad;
    private final Map<String, ThreadDiff> common;

    public LogDiff(final Log good, final Log bad) {
        this.good = good;
        this.bad = bad;
        this.common = new HashMap<>();
        // compute good threads
        final Set<String> goodThreads = new HashSet<>();
        for (final parser.LogEntry logEntry : this.good.entries) {
            goodThreads.add(logEntry.thread);
        }
        // compute common threads;
        final Set<String> commonThreads = new HashSet<>();
        for (final parser.LogEntry logEntry : bad.entries) {
            if (goodThreads.contains(logEntry.thread)) {
                commonThreads.add(logEntry.thread);
            }
        }
        // get entries for each common thread
        final Map<String, ArrayList<parser.LogEntry>> goodCommon = new HashMap<>();
        final Map<String, ArrayList<parser.LogEntry>> badCommon = new HashMap<>();
        for (final String thread : commonThreads) {
            goodCommon.put(thread, new ArrayList<>());
            badCommon.put(thread, new ArrayList<>());
        }
        for (final parser.LogEntry logEntry : this.good.entries) {
            if (commonThreads.contains(logEntry.thread)) {
                goodCommon.get(logEntry.thread).add(logEntry);
            }
        }
        for (final parser.LogEntry logEntry : this.bad.entries) {
            if (commonThreads.contains(logEntry.thread)) {
                badCommon.get(logEntry.thread).add(logEntry);
            }
        }
        // compute diff for each common thread
        for (final String thread : commonThreads) {
            common.put(thread, new ThreadDiff(thread, goodCommon.get(thread), badCommon.get(thread)));
        }
    }

    public void dumpBadDiff(final Consumer<ThreadDiff.ThreadLogEntry> consumer) {
        for (final ThreadDiff diff : common.values()) {
            diff.dumpBadDiff(consumer);
        }
        for (final parser.LogEntry logEntry : this.bad.entries) {
            if (!common.containsKey(logEntry.thread)) {
                consumer.accept(new ThreadDiff.ThreadLogEntry(logEntry));
            }
        }
    }
}
