package feedback;

import feedback.common.ActionMayThrow;
import feedback.common.Env;
import feedback.common.JavaThreadUtil;
import feedback.common.ThreadTestBase;
import feedback.diff.LogFileDiff;
import feedback.diff.ThreadDiff;
import feedback.log.LogFile;
import feedback.log.LogTestUtil;
import feedback.parser.LogParser;
import feedback.parser.ParserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.AssertionFailedError;

import javax.json.JsonObject;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ExceptionStacktraceTest extends ThreadTestBase {
    static final class DistributedCase {
        final String name;
        final String[] files;
        DistributedCase(final String name, final String... files) {
            this.name = name;
            this.files = files;
        }

        void prepareTempFiles(final String prefix, final Path tempDir) throws IOException {
            final String resourcePrefix = prefix + this.name + "/";
            final String tempPrefix = this.name + "/";
            for (final String file : files) {
                LogTestUtil.initTempFile(resourcePrefix + file, tempDir.resolve(tempPrefix + file));
            }
        }
    }

    @Test
    void testStacktrace() throws Exception {
        LogFile bad = LogTestUtil.getLogFile("ground-truth/" + "zookeeper-3157" + "/bad-run-log.txt");
        LogStatistics.StackTraceInjection[] a = LogStatistics.collectExceptionStackTrace(bad);
        for (LogStatistics.StackTraceInjection i : a) {
            System.out.println(i);
            System.out.println(i.stackTrace()[0]);
        }
        StackTraceElement[] es = Thread.currentThread().getStackTrace();
        String[] s = new String[es.length];
        for (int i = 0; i < es.length; i++) {
            //System.out.println(es[i]);
        }
    }
}
