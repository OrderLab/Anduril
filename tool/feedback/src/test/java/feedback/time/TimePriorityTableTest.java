package feedback.time;

import feedback.CommandLine;
import feedback.log.LogTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import runtime.time.TimePriorityTable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

class TimePriorityTableTest {
    @Test
    void testLoadingTimePriorityTable(final @TempDir Path tempDir)
            throws IOException, ExecutionException, InterruptedException {
        LogTestUtil.initTempFile("record-inject/kafka-12508/good-run-log.txt",
                tempDir.resolve("good-run-log.txt"));
        LogTestUtil.initTempFile("record-inject/kafka-12508/bad-run-log.txt",
                tempDir.resolve("bad-run-log.txt"));
        LogTestUtil.initTempFile("record-inject/kafka-12508/tree.json",
                tempDir.resolve("tree.json"));
        CommandLine.main(new String[]{"-tf",
                "-g", tempDir.resolve("good-run-log.txt").toString(),
                "-b", tempDir.resolve("bad-run-log.txt").toString(),
                "-s", tempDir.resolve("tree.json").toString(),
                "-obj", tempDir.resolve("time.bin").toString()});
        TimePriorityTable.load(tempDir.resolve("time.bin").toString());
    }
}
