package feedback.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

final class TimeDifferenceTest {
    private static final Logger LOG = LoggerFactory.getLogger(TimeDifferenceTest.class);

    @Test
    void testBugCases(final @TempDir Path tempDir) throws IOException {
        final BugCase[] cases = new BugCase[] {
                new TestCase("hdfs-12070", tempDir),
        };
        for (final BugCase bug : cases) {
            bug.print(LOG::debug);
        }
    }
}
