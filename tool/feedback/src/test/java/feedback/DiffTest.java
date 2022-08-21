package feedback;

import feedback.diff.LogFileDiff;
import feedback.diff.ThreadDiff;
import feedback.log.LogTestUtil;
import feedback.parser.LogParser;
import feedback.parser.ParserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.AssertionFailedError;

import javax.json.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class DiffTest {
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

    static final DistributedCase hdfs_4233 = new DistributedCase("hdfs-4233",
            "good-run-log/logs-0/hadoop-haoze-secondarynamenode-razor2.log",
            "good-run-log/logs-1/hadoop-haoze-namenode-razor2.log",
            "good-run-log/logs-2/hadoop-haoze-datanode-razor2.log",
            "good-run-log/logs-3/hadoop-haoze-datanode-razor2.log",
            "bad-run-log/logs-0/hadoop-haoze-secondarynamenode-razor2.log",
            "bad-run-log/logs-1/hadoop-haoze-namenode-razor2.log",
            "bad-run-log/logs-2/hadoop-haoze-datanode-razor2.log",
            "bad-run-log/logs-3/hadoop-haoze-datanode-razor2.log");

    private static final DistributedCase[] distributedCases = {
            hdfs_4233,
    };

    private static final String[] testCases = {
            "hbase-15252",
            "hbase-19608",
//            "hbase-25905",
            "hbase-18137",
            "hbase-20492",
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

    private static List<String> collectDiff(final Consumer<Consumer<ThreadDiff.CodeLocation>> dumpBadDiff) {
        final List<String> result = new ArrayList<>();
        dumpBadDiff.accept(e -> result.add(e.toString()));
        Collections.sort(result);
        return result;
    }

    private static List<String> collectDiff(final String[] text) {
        return Arrays.stream(text).filter(line -> !line.isEmpty()).sorted().collect(Collectors.toList());
    }

    @Test
    void testLogFileDiff() throws Exception {
        // test the logs without filtering redundant element
        ScalaUtil.runTasks(testCases, bug -> {
            try {
                final List<String> expected = collectDiff(LogTestUtil.getFileLines("ground-truth/" + bug + "/diff_log.txt")),
                        actual = collectDiff(new LogFileDiff(LogTestUtil.getLogFile("ground-truth/" + bug + "/good-run-log.txt"),
                                LogTestUtil.getLogFile("ground-truth/" + bug + "/bad-run-log.txt"))::dumpBadDiff);
                assertEquals(expected, actual);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testDistributedLogDiff(final @TempDir Path tempDir) throws Exception {
        ScalaUtil.runTasks(distributedCases, bug -> {
            try {
                bug.prepareTempFiles("ground-truth/", tempDir);
                final List<String> expected = collectDiff(LogTestUtil.getDistinctFileLines("ground-truth/" + bug.name + "/diff_log.txt"));
                final List<String> actual = new ArrayList<>();
                Algorithms.computeDiff(LogParser.parseLog(tempDir.resolve(bug.name + "/good-run-log")),
                        LogParser.parseLog(tempDir.resolve(bug.name + "/bad-run-log")),
                        entry -> actual.add(entry.toString()));
                Collections.sort(actual);
                assertEquals(expected, actual);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static final Random random = new Random(System.currentTimeMillis());

    private static String[] prepareArgs(final String good, final String bad, final List<String> option) {
        final List<List<String>> cmd = Arrays.asList(
                Collections.singletonList(random.nextBoolean() ? "--diff" : "-d"),
                Arrays.asList(random.nextBoolean()? "--good" : "-g", good),
                Arrays.asList(random.nextBoolean()? "--bad" : "-b", bad),
                option);
        Collections.shuffle(cmd);
        final List<String> result = new ArrayList<>();
        cmd.forEach(result::addAll);
        return result.toArray(new String[0]);
    }

    static ArrayList<String> prepareEndToEndTest(final String prefix, final Path tempDir) throws IOException {
        final ArrayList<String> cases = new ArrayList<>();
        for (final String bug : testCases) {
            LogTestUtil.initTempFile(prefix + bug + "/good-run-log.txt",
                    tempDir.resolve(bug + "/good-run-log"));
            LogTestUtil.initTempFile(prefix + bug + "/bad-run-log.txt",
                    tempDir.resolve(bug + "/bad-run-log"));
            cases.add(bug);
        }
        for (final DistributedCase bug : distributedCases) {
            bug.prepareTempFiles(prefix, tempDir);
            cases.add(bug.name);
        }
        Collections.shuffle(cases);
        return cases;
    }

    private void testEndToEndDiff(final Path tempDir, final String bug, final String good, final String bad)
            throws Exception {
        final String bugDir = tempDir + "/" + bug;
        final String outputFile = bugDir + ".txt";
        final String jsonFile = bugDir + ".json";
        final String goodRun = tempDir + "/" + good;
        final String badRun = tempDir + "/" + bad;
        final List<String> diff = collectDiff(LogTestUtil.getDistinctFileLines("ground-truth/" + bug + "/diff_log.txt"));
        // test output
        CommandLine.main(prepareArgs(goodRun, badRun, Arrays.asList(random.nextBoolean()? "--output" : "-o", outputFile)));
        assertEquals(diff, Arrays.stream(ParserUtil.getFileLines(outputFile)).sorted().collect(Collectors.toList()));
        // test json
        JsonUtil.dumpJson(JsonUtil.createObjectBuilder().add("bug", bug).build(), jsonFile);
        CommandLine.main(prepareArgs(goodRun, badRun, Arrays.asList(this.random.nextBoolean()? "--append" : "-a", jsonFile)));
        final JsonObject json = JsonUtil.loadJson(jsonFile);
        assertEquals(diff, JsonUtil.toStringStream(json.getJsonArray("diff")).sorted().collect(Collectors.toList()));
        assertEquals(bug, json.getString("bug"));
    }

    @Test
    void testEndToEndDiff(final @TempDir Path tempDir) throws Exception {
        ScalaUtil.runTasks(prepareEndToEndTest("ground-truth/", tempDir), bug -> {
            try {
                testEndToEndDiff(tempDir, bug, bug + "/good-run-log", bug + "/bad-run-log");
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testEndToEndDiffShuffleError(final @TempDir Path tempDir) throws Exception {
        final ArrayList<String> cases = prepareEndToEndTest("ground-truth/", tempDir);
        final ArrayList<String> shuffle = (ArrayList<String>) cases.clone();
        Collections.shuffle(shuffle);
        ScalaUtil.runTasks(0, cases.size(), i -> {
            try {
                final String expected = cases.get(i);
                final String actual = shuffle.get(i);
                if (expected.equals(actual)) {
                    testEndToEndDiff(tempDir, expected, expected + "/good-run-log", expected + "/bad-run-log");
                } else {
                    assertThrows(AssertionFailedError.class, () ->
                            testEndToEndDiff(tempDir, expected, actual + "/good-run-log", actual + "/bad-run-log"));
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testEndToEndDiffSwitchError(final @TempDir Path tempDir) throws Exception {
        ScalaUtil.runTasks(prepareEndToEndTest("ground-truth/", tempDir), bug -> {
            try {
                if (random.nextBoolean()) {
                    testEndToEndDiff(tempDir, bug, bug + "/good-run-log", bug + "/bad-run-log");
                } else {
                    assertThrows(AssertionFailedError.class, () ->
                            testEndToEndDiff(tempDir, bug, bug + "/bad-run-log", bug + "/good-run-log"));
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
