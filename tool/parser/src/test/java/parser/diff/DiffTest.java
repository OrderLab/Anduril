package parser.diff;

import org.junit.jupiter.api.Test;
import parser.LogTestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiffTest {
    private static final String[] testCases = {
            "hbase-15252",
            "hbase-19608",
//            "hbase-25905",
//            "hbase-18137",
//            "hbase-20492",
            "hdfs-12070",
            "hdfs-15963",
            "hdfs-12248",
            "zookeeper-2247",
            "zookeeper-3157",
            "zookeeper-3006",
            "zookeeper-4203",
            "kafka-9374",
            "kafka-12508",
            "kafka-10340",
    };

    @Test
    void testLogDiff() throws IOException {
        for (final String bug : testCases) {
            final ArrayList<String> actual = new ArrayList<>(), expected = new ArrayList<>();
            new LogDiff(LogTestUtil.getLog("ground-truth/" + bug + "/good-run-log.txt"),
                    LogTestUtil.getLog("ground-truth/" + bug + "/bad-run-log.txt"))
                    .dumpBadDiff(e -> actual.add(e.toString()));
            for (final String text : LogTestUtil.getFileLines("ground-truth/" + bug + "/diff_log.txt")) {
                if (!text.isEmpty()) {
                    expected.add(text);
                }
            }
            Collections.sort(actual);
            Collections.sort(expected);
            assertEquals(expected, actual);
        }
    }
}
