package feedback;

import feedback.common.Env;
import feedback.common.JavaThreadUtil;
import feedback.common.ThreadTestBase;
import feedback.log.LogTestUtil;
import feedback.parser.ParserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LocationFeedbackTest extends ThreadTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(LocationFeedbackTest.class);

    private static abstract class BugCase {
        private final String name;
        private final int[] instances;
        private BugCase(final String name, final int[] instances) {
            this.name = name;
            this.instances = instances;
        }
        private BugCase(final String name, final int caseNumber) {
            this.name = name;
            this.instances = new int[caseNumber];
            for (int i = 0; i < caseNumber; i++) {
                this.instances[i] = i;
            }
        }

        abstract void prepareTempFiles(final String prefix, final Path tempDir) throws IOException;

        private void test(final Path tempDir)
                throws IOException, ExecutionException, InterruptedException {
            this.prepareTempFiles("ground-truth/", tempDir);
            final String dir = tempDir + "/" + this.name + "/";
            for (int i_ = 0; i_ < this.instances.length; i_++) {
                final int i = i_;
                final List<Integer> expected = JsonUtil.toIntStream(LogTestUtil.loadJson(
                        "feedback/" + this.name + "/injection-" + this.instances[i] + ".json").getJsonArray("feedback"))
                        .sorted().collect(Collectors.toList());
                // test output
                final Future<Void> outputTest = Env.submit(() -> {
                    final String outputFile = tempDir + "/" + this.name + "/test-" + i + ".out";
                    CommandLine.main(prepareArgs(dir + "good-run-log", dir + "bad-run-log", dir + i, dir + "spec.json",
                            Arrays.asList(random.nextBoolean() ? "--output" : "-o", outputFile)));
                    assertEquals(expected, Arrays.stream(ParserUtil.getFileLines(outputFile))
                            .filter(line -> !line.isEmpty()).map(Integer::valueOf).sorted().collect(Collectors.toList()));
                });
                // test json
                final Future<Void> jsonTest = Env.submit(() -> {
                    final String jsonFile = tempDir + "/" + this.name + "/test-" + i + ".json";
                    JsonUtil.dumpJson(JsonUtil.createObjectBuilder().add("bug", this.name).build(), jsonFile);
                    CommandLine.main(prepareArgs(dir + "good-run-log", dir + "bad-run-log", dir + i, dir + "spec.json",
                            Arrays.asList(random.nextBoolean() ? "--append" : "-a", jsonFile)));
                    final JsonObject json = JsonUtil.loadJson(jsonFile);
                    assertEquals(expected, JsonUtil.toIntStream(json.getJsonArray("feedback"))
                            .sorted().collect(Collectors.toList()));
                });
                outputTest.get();
                jsonTest.get();
            }
        }
    }

    private static final class TestCase extends BugCase {
        private TestCase(final String name, final int... instances) {
            super(name, instances);
        }

        @Override
        void prepareTempFiles(final String prefix, final Path tempDir) throws IOException {
            LogTestUtil.initTempFile(prefix + super.name + "/good-run-log.txt",
                    tempDir.resolve(super.name + "/good-run-log"));
            LogTestUtil.initTempFile(prefix + super.name + "/bad-run-log.txt",
                    tempDir.resolve(super.name + "/bad-run-log"));
            LogTestUtil.initTempFile("feedback/" + super.name + "/tree.json",
                    tempDir.resolve(super.name + "/spec.json"));
            for (int i = 0; i < super.instances.length; i++) {
                LogTestUtil.initTempFile("feedback/" + super.name + "/output-" + super.instances[i] + ".txt",
                        tempDir.resolve(super.name + "/" + i));
            }
        }
    }

    private static final class DistributedCase extends BugCase {
        private final DiffTest.DistributedCase bug;
        private final String[] files;
        private DistributedCase(final DiffTest.DistributedCase bug, final int caseNumber, final String... files) {
            super(bug.name, caseNumber);
            this.bug = bug;
            this.files = files;
        }

        @Override
        void prepareTempFiles(final String prefix, final Path tempDir) throws IOException {
            this.bug.prepareTempFiles(prefix, tempDir);
            LogTestUtil.initTempFile("feedback/" + super.name + "/tree.json",
                    tempDir.resolve(super.name + "/spec.json"));
            for (int i = 0; i < super.instances.length; i++) {
                final String filePrefix = "feedback/" + super.name + "/" + super.instances[i] + "/";
                final Path tempPrefix = tempDir.resolve(super.name + "/" + i);
                for (final String file : files) {
                    LogTestUtil.initTempFile(filePrefix + file, tempPrefix.resolve(file));
                }
            }
        }
    }

    private static final BugCase[] cases = new BugCase[] {
            new TestCase("zookeeper-3006",
                    2695,1004,2696,558,2694,1001,554,2872,274,0,1000,281,2766),
            new DistributedCase(DiffTest.hdfs_4233, 108,
                    "logs-0/hadoop-haoze-secondarynamenode.pid",
                    "logs-0/hadoop-haoze-secondarynamenode-razor7.log",
                    "logs-0/hadoop-haoze-secondarynamenode-razor7.out",
                    "logs-0/SecurityAuth-haoze.audit",
                    "logs-1/hadoop-haoze-namenode.pid",
                    "logs-1/hadoop-haoze-namenode-razor7.log",
                    "logs-1/hadoop-haoze-namenode-razor7.out",
                    "logs-1/SecurityAuth-haoze.audit",
                    "logs-2/hadoop-haoze-datanode.pid",
                    "logs-2/hadoop-haoze-datanode-razor7.log",
                    "logs-2/hadoop-haoze-datanode-razor7.out",
                    "logs-2/SecurityAuth-haoze.audit",
                    "logs-3/hadoop-haoze-datanode.pid",
                    "logs-3/hadoop-haoze-datanode-razor7.log",
                    "logs-3/hadoop-haoze-datanode-razor7.out",
                    "logs-3/SecurityAuth-haoze.audit"),
    };

    private static final Random random = new Random(System.currentTimeMillis());

    private static String[] prepareArgs(final String good, final String bad, final String trial, final String spec,
                                        final List<String> option) {
        final List<List<String>> cmd = Arrays.asList(
                Collections.singletonList(random.nextBoolean() ? "--location-feedback" : "-lf"),
                Arrays.asList(random.nextBoolean()? "--good" : "-g", good),
                Arrays.asList(random.nextBoolean()? "--bad" : "-b", bad),
                Arrays.asList(random.nextBoolean()? "--trial" : "-t", trial),
                Arrays.asList(random.nextBoolean()? "--spec" : "-s", spec),
                option);
        Collections.shuffle(cmd);
        final List<String> result = new ArrayList<>();
        cmd.forEach(result::addAll);
        return result.toArray(new String[0]);
    }

    @Test
    void testEnd2EndLocationFeedback(final @TempDir Path tempDir) throws Exception {
        LOG.info("testEnd2EndLocationFeedback is expected to run for {} seconds", 40);
        JavaThreadUtil.parallel(cases, bug -> {
            bug.test(tempDir);
        }).get();
    }
}
