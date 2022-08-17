package feedback;

import feedback.parser.DistributedLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.reflections.Reflections;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SymptomTest {
    private static void test(final Path tempDir, final String bug) throws IOException {
        final Path bugDir = tempDir.resolve(bug);
        final DistributedLog good = new DistributedLog(bugDir.resolve("good-run-log"));
        final DistributedLog bad = new DistributedLog(bugDir.resolve("bad-run-log"));
        assertFalse(Symptoms.checkSymptom(good, bug));
        assertTrue(Symptoms.checkSymptom(bad, bug));
    }

    @Test
    void testSymptom(final @TempDir Path tempDir) throws IOException {
        DiffTest.prepareEndToEndTest(tempDir);
        final String[] bugs = new String[]{
                "zookeeper-3006",
                "hdfs-4233",
                "hdfs-12070",
        };
        for (final String bug : bugs) {
            test(tempDir, bug);
        }
        for (final Class<? extends Object> bug : new Reflections("feedback.parser").getSubTypesOf(Object.class)) {
            System.out.println(bug.getName());
        }
    }
}
