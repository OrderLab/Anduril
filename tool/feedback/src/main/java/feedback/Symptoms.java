package feedback;

import feedback.parser.DistributedLog;
import feedback.parser.LogEntry;

import javax.json.JsonObject;
import java.util.function.Consumer;

final class Symptoms {
    private static void error() throws Exception {
        throw new Exception("symptom matching error");
    }

    static void complementSymptom(final DistributedLog trial, final JsonObject spec, final Consumer<Integer> consumer)
            throws Exception {
        switch (spec.getString("case")) {
            case "zookeeper-3006": {
                if (trial.distributed) {
                    error();
                }
                for (final LogEntry entry : trial.logs[0].entries) {
                    if (entry.msg.contains("\n1) testAbsentRecentSnapshot(org.apache.zookeeper.test.ZkDatabaseCorruptionTest)\njava.lang.NullPointerException\n")) {
                        return;
                    }
                }
                consumer.accept(0);
                break;
            }
            default: throw new Exception("can't recognize the bug case");
        }
    }
}
