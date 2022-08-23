package feedback.parser;

import feedback.common.ThreadTestBase;
import feedback.common.Env;
import feedback.log.LogTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class LogParserTest extends ThreadTestBase {
    private static final int pass = 2;
    private static final int fail = 1;

    @Test
    void testPass(final @TempDir Path tempDir) throws Exception {
        Env.parallel(0, pass, bug -> {
            final Path log = tempDir.resolve("pass").resolve(bug + ".log");
            LogTestUtil.initTempFile("parser-check/pass/" + bug + ".log", log);
            LogParser.parseLog(log);
        }).get();
    }

    @Test
    void testFail(final @TempDir Path tempDir) throws Exception {
        Env.parallel(0, fail, bug -> {
            assertThrows(Exception.class, () -> {
                final Path log = tempDir.resolve("fail").resolve(bug + ".log");
                LogTestUtil.initTempFile("parser-check/fail/" + bug + ".log", log);
                LogParser.parseLog(log);
            }, "Found unit test log without test result");
        }).get();
    }
}
