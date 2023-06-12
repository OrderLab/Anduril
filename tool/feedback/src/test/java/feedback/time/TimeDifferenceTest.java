package feedback.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

final class TimeDifferenceTest {
    private static final Logger LOG = LoggerFactory.getLogger(TimeDifferenceTest.class);

    @Test
    void testBugCases(final @TempDir Path tempDir) {
        final BugCase[] cases = new BugCase[] {
                new TestCase("kafka-12508", tempDir),
        };
        for (final BugCase bug : cases) {
            bug.printAlign(LOG::debug);
        }
    }

    @Test
    void testBugCasesWithCSV(final @TempDir Path tempDir) {
        final BugCaseCSV[] cases = new BugCaseCSV[] {
                new TestCaseCSV("kafka-12508-csv", tempDir),
        };
        for (final BugCaseCSV bug : cases) {
            bug.printAlign(LOG::debug);
        }
    }
}
