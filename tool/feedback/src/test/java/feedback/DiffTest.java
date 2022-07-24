package feedback;

import feedback.diff.DistributedLogDiff;
import feedback.diff.LogDiff;
import feedback.diff.ThreadDiff;
import feedback.parser.DistributedLog;
import feedback.parser.LogTestUtil;
import feedback.parser.ParserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DiffTest {
    private static final class DistributedCase {
        final String name;
        final String[] files;
        DistributedCase(final String name, final String... files) {
            this.name = name;
            this.files = Arrays.stream(files).toArray(String[]::new);
        }

        void prepareTempFiles(final Path tempDir) throws IOException {
            final String resourcePrefix = "ground-truth/" + this.name + "/";
            final String tempPrefix = this.name + "/";
            for (final String file : files) {
                LogTestUtil.initTempFile(resourcePrefix + file, tempDir.resolve(tempPrefix + file));
            }
        }
    }

    private static final DistributedCase[] distributedCases = {
            new DistributedCase("hdfs-4233",
                    "good-run-log/logs-0/hadoop-haoze-secondarynamenode-razor2.log",
                    "good-run-log/logs-1/hadoop-haoze-namenode-razor2.log",
                    "good-run-log/logs-2/hadoop-haoze-datanode-razor2.log",
                    "good-run-log/logs-3/hadoop-haoze-datanode-razor2.log",
                    "bad-run-log/logs-0/hadoop-haoze-secondarynamenode-razor2.log",
                    "bad-run-log/logs-1/hadoop-haoze-namenode-razor2.log",
                    "bad-run-log/logs-2/hadoop-haoze-datanode-razor2.log",
                    "bad-run-log/logs-3/hadoop-haoze-datanode-razor2.log"),
    };

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

    private static List<String> collectDiff(final Consumer<Consumer<ThreadDiff.ThreadLogEntry>> dumpBadDiff) {
        final List<String> result = new ArrayList<>();
        dumpBadDiff.accept(e -> result.add(e.toString()));
        Collections.sort(result);
        return result;
    }

    private static List<String> collectDiff(final String[] text) {
        return Arrays.stream(text).filter(line -> !line.isEmpty()).sorted().collect(Collectors.toList());
    }

    @Test
    void testLogDiff() throws IOException {
        for (final String bug : testCases) {
            final List<String> expected = collectDiff(LogTestUtil.getFileLines("ground-truth/" + bug + "/diff_log.txt")),
                    actual = collectDiff(new LogDiff(LogTestUtil.getLog("ground-truth/" + bug + "/good-run-log.txt"),
                            LogTestUtil.getLog("ground-truth/" + bug + "/bad-run-log.txt"))::dumpBadDiff);
            assertEquals(expected, actual);
        }
    }

    @Test
    void testDistributedLogDiff(final @TempDir Path tempDir) throws Exception {
        for (final DistributedCase bug : distributedCases) {
            bug.prepareTempFiles(tempDir);
            final List<String> expected = collectDiff(LogTestUtil.getFileLines("ground-truth/" + bug.name + "/diff_log.txt")),
                    actual = collectDiff(new DistributedLogDiff(
                            new DistributedLog(tempDir.resolve(bug.name + "/good-run-log")),
                            new DistributedLog(tempDir.resolve(bug.name + "/bad-run-log")))::dumpBadDiff);
            assertEquals(expected, actual);
        }
    }

    private final Random random = new Random();

    private String[] prepareArgs(final String bugDir, final List<String> option) {
        final List<List<String>> cmd = Arrays.asList(
                Collections.singletonList(this.random.nextBoolean() ? "--diff" : "-d"),
                Arrays.asList(this.random.nextBoolean()? "--good" : "-g", bugDir + "/good-run-log"),
                Arrays.asList(this.random.nextBoolean()? "--bad" : "-b", bugDir + "/bad-run-log"),
                option);
        Collections.shuffle(cmd);
        final List<String> result = new ArrayList<>();
        cmd.forEach(result::addAll);
        return result.toArray(new String[0]);
    }

    private List<String> prepareEndToEndTest(final Path tempDir) throws IOException {
        final List<String> cases = new ArrayList<>();
        for (final String bug : testCases) {
            LogTestUtil.initTempFile("ground-truth/" + bug + "/good-run-log.txt",
                    tempDir.resolve(bug + "/good-run-log"));
            LogTestUtil.initTempFile("ground-truth/" + bug + "/bad-run-log.txt",
                    tempDir.resolve(bug + "/bad-run-log"));
            cases.add(bug);
        }
        for (final DistributedCase bug : distributedCases) {
            bug.prepareTempFiles(tempDir);
            cases.add(bug.name);
        }
        Collections.shuffle(cases);
        return cases;
    }

    private void testEndToEndDiff(final String rootDir, final String bug) throws Exception {
        final String bugDir = rootDir + bug;
        final String outputFile = bugDir + ".txt";
        final String jsonFile = bugDir + ".json";
        final List<String> diff = collectDiff(LogTestUtil.getFileLines("ground-truth/" + bug + "/diff_log.txt"));
        // test output
        CommandLine.main(prepareArgs(bugDir, Arrays.asList(random.nextBoolean()? "--output" : "-o", outputFile)));
        assertEquals(diff, Arrays.stream(ParserUtil.getFileLines(outputFile)).sorted().collect(Collectors.toList()));
        // test json
        JsonUtil.dumpJson(JsonUtil.createObjectBuilder().add("bug", bug).build(), jsonFile);
        CommandLine.main(prepareArgs(bugDir, Arrays.asList(this.random.nextBoolean()? "--append" : "-a", jsonFile)));
        final JsonObject json = JsonUtil.loadJson(jsonFile);
        assertEquals(diff, json.getJsonArray("diff").stream()
                .map(v -> ((JsonString)v).getString()).sorted().collect(Collectors.toList()));
        assertEquals(bug, json.getString("bug"));
    }

    @Test
    void testEndToEndDiff(final @TempDir Path tempDir) throws Exception {
        final List<String> cases = prepareEndToEndTest(tempDir);
        final String rootDir = tempDir + "/";
        for (final String bug : cases) {
            testEndToEndDiff(rootDir, bug);
        }
    }
}
