package feedback.parser;

import feedback.FeedbackTestBase;
import feedback.ScalaUtil;
import feedback.log.LogTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class LogParserTest extends FeedbackTestBase {
    private final int pass = 1;
    private final int fail = 1;

    @Test
    void testPass(final @TempDir Path tempDir) throws Exception {
        ScalaUtil.runTasks(0, pass, bug -> {
            try {
                final Path log = tempDir.resolve("pass").resolve(bug + ".log");
                LogTestUtil.initTempFile("parser-check/pass/" + bug + ".log", log);
                LogParser.parseLog(log);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testFail(final @TempDir Path tempDir) throws Exception {
        ScalaUtil.runTasks(0, fail, bug ->
            assertThrows(Exception.class, () -> {
                final Path log = tempDir.resolve("fail").resolve(bug + ".log");
                LogTestUtil.initTempFile("parser-check/fail/" + bug + ".log", log);
                LogParser.parseLog(log);
            }, "Found unit test log without test result")
        );
    }
}
