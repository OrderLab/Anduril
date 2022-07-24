package feedback;

import feedback.parser.LogTestUtil;
import feedback.parser.ParserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LocationFeedbackTest {
    private static final class TestCase {
        final String name;
        final int[] instances;
        TestCase(final String name, final int... instances) {
            this.name = name;
            this.instances = instances;
        }

        void prepareTempFiles(final Path tempDir) throws IOException {
            LogTestUtil.initTempFile("ground-truth/" + this.name + "/good-run-log.txt",
                    tempDir.resolve(this.name + "/good-run-log"));
            LogTestUtil.initTempFile("ground-truth/" + this.name + "/bad-run-log.txt",
                    tempDir.resolve(this.name + "/bad-run-log"));
            LogTestUtil.initTempFile("feedback/" + this.name + "/tree.json",
                    tempDir.resolve(this.name + "/spec.json"));
            for (int i = 0; i < this.instances.length; i++) {
                LogTestUtil.initTempFile("feedback/" + this.name + "/output-" + this.instances[i] + ".txt",
                        tempDir.resolve(this.name + "/output-" + i));
            }
        }

        void test(final Path tempDir) throws Exception {
            this.prepareTempFiles(tempDir);
            final String dir = tempDir + "/" + this.name + "/";
            for (int i = 0; i < this.instances.length; i++) {
                final List<Integer> expected =
                        LogTestUtil.loadJson("feedback/" + this.name + "/injection-" + this.instances[i] + ".json")
                                .getJsonArray("feedback").stream()
                                .map(v -> ((JsonNumber)v).intValue()).sorted().collect(Collectors.toList());
                // test output
                final String outputFile = tempDir + "/" + this.name + "/test-" + i + ".out";
                CommandLine.main(prepareArgs(dir + "good-run-log", dir + "bad-run-log",
                        dir + "output-" + i, dir + "spec.json",
                        Arrays.asList(random.nextBoolean()? "--output" : "-o", outputFile)));
                assertEquals(expected, Arrays.stream(ParserUtil.getFileLines(outputFile))
                        .filter(line -> !line.isEmpty()).map(Integer::valueOf).sorted().collect(Collectors.toList()));
                // test json
                final String jsonFile = tempDir + "/" + this.name + "/test-" + i + ".json";
                JsonUtil.dumpJson(JsonUtil.createObjectBuilder().add("bug", this.name).build(), jsonFile);
                CommandLine.main(prepareArgs(dir + "good-run-log", dir + "bad-run-log",
                        dir + "output-" + i, dir + "spec.json",
                        Arrays.asList(random.nextBoolean()? "--append" : "-a", jsonFile)));
                final JsonObject json = JsonUtil.loadJson(jsonFile);
                assertEquals(expected, json.getJsonArray("locationFeedback").stream()
                        .map(v -> ((JsonNumber)v).intValue()).sorted().collect(Collectors.toList()));
            }
        }
    }

    private static TestCase[] cases = new TestCase[] {
            new TestCase("zookeeper-3006",
                    2695,1004,2696,558,2694,1001,554,2872,274,0,1000,281,2766),
    };

    private static final Random random = new Random();

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
    void testEnd2EndFeedback(final @TempDir Path tempDir) throws Exception {
        for (final TestCase test : cases) {
            test.test(tempDir);
        }
    }
}
