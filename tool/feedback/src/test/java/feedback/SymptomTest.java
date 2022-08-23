package feedback;

import feedback.common.ThreadTestBase;
import feedback.log.Log;
import feedback.parser.LogParser;
import feedback.symptom.Symptoms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static feedback.ScalaTestUtil.assertTrue;

final class SymptomTest extends ThreadTestBase {
    private static void test(final Path tempDir, final String bug) {
        final Path bugDir = tempDir.resolve(bug);
        final Log good = LogParser.parseLog(bugDir.resolve("good-run-log"));
        final Log bad = LogParser.parseLog(bugDir.resolve("bad-run-log"));
        assertTrue(Symptoms.findSymptom(good, bug).isEmpty());
        assertTrue(Symptoms.findSymptom(bad, bug).nonEmpty());
        assertTrue(Symptoms.findResultEvent(good, bug).isEmpty());
        assertTrue(Symptoms.findResultEvent(bad, bug).nonEmpty());
    }

    @Test
    void testSymptom(final @TempDir Path tempDir) throws IOException {
        DiffTest.prepareEndToEndTest(tempDir);
        final String[] bugs = new String[]{
                "zookeeper-3006",
                "zookeeper-4203",
                "zookeeper-3157",
                "zookeeper-2247",
                "hdfs-4233",
                "hdfs-12070",
                "hdfs-12248",
                "hbase-19608",
                "hbase-18137",
                "kafka-12508",
                "kafka-9374",
        };
        for (final String bug : bugs) {
            test(tempDir, bug);
        }
    }

    // TODO: add more tests for all cases
}
