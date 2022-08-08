package feedback;

import feedback.diff.DistributedLogDiff;
import feedback.diff.LogDiff;
import feedback.diff.ThreadDiff;
import feedback.parser.DistributedLog;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

final class Algorithms {
    private static void check(final DistributedLog good, final DistributedLog bad) throws Exception {
        if (good.distributed ^ bad.distributed) {
            throw new Exception("distributed mode not matched");
        }
        if (good.distributed &&
                !Arrays.equals(Arrays.stream(good.dirs).map(File::getName).toArray(String[]::new),
                        Arrays.stream(good.dirs).map(File::getName).toArray(String[]::new))) {
            throw new Exception("distributed log not matched");
        }
    }

    static void computeDiff(final DistributedLog good, final DistributedLog bad,
                            final Consumer<ThreadDiff.ThreadLogEntry> consumer) throws Exception {
        check(good, bad);
        if (good.distributed) {
            new DistributedLogDiff(good, bad).dumpBadDiff(consumer);
        } else {
            new LogDiff(good.logs[0], bad.logs[0]).dumpBadDiff(consumer);
        }
    }

    private static void computeLocationFeedback(final Consumer<Consumer<ThreadDiff.ThreadLogEntry>> dumpExpectedDiff,
                                                final Consumer<Consumer<ThreadDiff.ThreadLogEntry>> dumpActualDiff,
                                                final JsonObject spec, final Consumer<Integer> consumer)
            throws Exception {
        final int eventNumber = spec.getInt("start");
        final Map<ThreadDiff.ThreadLogEntry, Integer> wanted = new HashMap<>();
        dumpExpectedDiff.accept(e -> wanted.put(e, 0));
        if (wanted.size() + (Symptoms.isSymptomLogged(spec)? 0 : 1) != eventNumber) {
            throw new Exception("wrong diff");
        }
        final JsonArray array = spec.getJsonArray("nodes");
        for (int i = 0; i < array.size(); i++) {
            final JsonObject node = array.getJsonObject(i);
            final int id = node.getInt("id");
            if (id >= eventNumber) {
                continue;
            }
            if (id == 0) {
                continue;
            }
            if (!node.getString("type").equals("location_event")) {
                throw new Exception("wrong diff");
            }
            final JsonObject location = node.getJsonObject("location");
            final String cls = location.getString("class");
            final ThreadDiff.ThreadLogEntry entry =
                    new ThreadDiff.ThreadLogEntry(cls.substring(cls.lastIndexOf('.') + 1),
                            location.getInt("line_number"));
            if (wanted.containsKey(entry)) {
                wanted.put(entry, id);
            } else {
                throw new Exception("wrong diff");
            }
        }
        dumpActualDiff.accept(wanted::remove);
        if (Symptoms.isSymptomLogged(spec)) {
            wanted.values().forEach(i -> {
                if (i != 0) {
                    consumer.accept(i);
                }
            });
        } else {
            wanted.values().forEach(consumer);
        }
    }

    static void computeLocationFeedback(final DistributedLog good, final DistributedLog bad, final DistributedLog trial,
                                        final JsonObject spec, final Consumer<Integer> consumer) throws Exception {
        check(good, bad);
        check(good, trial);
        if (good.distributed) {
            computeLocationFeedback(new DistributedLogDiff(good, bad)::dumpBadDiff,
                    new DistributedLogDiff(good, trial)::dumpBadDiff, spec, consumer);
        } else {
            computeLocationFeedback(new LogDiff(good.logs[0], bad.logs[0])::dumpBadDiff,
                    new LogDiff(good.logs[0], trial.logs[0])::dumpBadDiff, spec, consumer);
        }
        if (!Symptoms.checkSymptom(trial, spec)) {
            consumer.accept(0);
        }
    }

//    static void computeTimeFeedback(final DistributedLog good, final DistributedLog bad, final DistributedLog trial,
//                                    final JsonObject spec, final Consumer<FeedbackTable> consumer) throws Exception {
//        check(good, bad);
//        check(good, trial);
//        if (good.distributed) {
//            for (int i = 0; i < good.logs.length; i++) {
//                final FeedbackTable table = computeTimeFeedback(good.logs[i], bad.logs[i], trial.logs[i], spec);
//                table.setPid(Parser.parseLogDirId(good.dirs[i].getName()));
//                consumer.accept(table);
//            }
//        } else {
//            computeTimeFeedback(good.logs[0], bad.logs[0], trial.logs[0], spec);
//        }
//    }
//

    static Serializable computeTimeFeedback(final DistributedLog good, final DistributedLog bad, final DistributedLog trial,
                                            final JsonObject spec) throws Exception {
        check(good, bad);
        check(good, trial);
    }
}
