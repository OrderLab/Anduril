package feedback;

import feedback.log.LogTestUtil;
import feedback.parser.TextParser;
import org.junit.jupiter.api.Test;
import runtime.FeedbackManager;
import runtime.LocalInjectionManager;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LocationFeedbackExperimentTest {
    private static final BugCase[] cases = new BugCase[]{
//            new BugCase("hdfs-12248", 3173),  // cost ~2 min
            new BugCase("hdfs-4233", 109),
            new BugCase("zookeeper-2247", 215),
            new BugCase("zookeeper-3006", 15),
            new BugCase("zookeeper-3157", 2222),
            new BugCase("zookeeper-4203", 198),
    };

    private static final class FeedbackTestHelper extends FeedbackManager {
        private FeedbackTestHelper(final JsonObject json) {
            super(json);
        }

        private int weightBound;
        private int index;

        private void test(final int windowSize, final int[] ids) {
            if (super.injections.size() <= windowSize) {
                assertEquals(super.injections.size(), ids.length);
                return;
            }
            this.allowSet.clear();
            for (int i = 0; i < super.graph.startNumber; i++) {
                super.graph.setStartValue(i, super.active.getOrDefault(i, 0));
            }
            this.weightBound = GraphTest.INF;
            this.index = 0;
            final int[] expected = new int[ids.length];
            final int[] expectedIds = new int[ids.length];
            super.graph.calculatePriorities(injectionId -> {
                if (this.allowSet.add(injectionId) && allowSet.size() <= windowSize) {
                    expected[this.index] = this.graph.w.get(injectionId);
                    expectedIds[this.index] = injectionId;
                    index++;
                }
                if (allowSet.size() >= windowSize) {
                    final int bound = super.graph.w.get(injectionId);
                    if (bound > this.weightBound) {
                        return true;
                    }
                    this.weightBound = bound;
                }
                return false;
            });
            assertEquals(this.index, ids.length);
            final int[] actual = new int[ids.length];
            for (int i = 0; i < actual.length; i++) {
                actual[i] = this.graph.w.get(ids[i]);
            }
            Arrays.sort(actual);
            Arrays.sort(expectedIds);
            for (int i = 0; i < actual.length; i++) {
                assertEquals(expected[i], actual[i]);  // check weight
                assertEquals(expectedIds[i], ids[i]);  // check concrete order
            }
        }
    }

    private static final class BugCase {
        private final String name;
        private final int n;

        private BugCase(final String name, final int n) {
            this.name = name;
            this.n = n;
        }

        private void test() throws IOException {
            final JsonObject spec = LogTestUtil.loadJson("location-feedback-experiment/" + this.name + "/tree.json");
            int nextWindowSize = 10;
            final FeedbackTestHelper feedback = new FeedbackTestHelper(spec);
            for (int i = 0; i < this.n; i++) {
                final JsonObject injection = LogTestUtil.loadJson(
                        "location-feedback-experiment/" + this.name + "/trials/injection-" + i + ".json");
                final int windowSize = injection.getInt("window");
                assertEquals(windowSize, nextWindowSize);
                nextWindowSize = injection.containsKey("id")?
                        windowSize : Math.min(LocalInjectionManager.INF, windowSize * 2);
                final String[] lines = LogTestUtil.getFileLines(
                        "location-feedback-experiment/" + this.name + "/trials/output-" + i + ".txt");
                assertEquals(1, lines.length);
                feedback.test(windowSize, TextParser.parseLogSet(lines[0]));
                // the last trial is generally flawed
                // a trial without injection should not contribute to the feedback
                if (i != this.n - 1 && injection.containsKey("id")) {
                    final JsonArray events = injection.getJsonArray("feedback");
                    for (int j = 0; j < events.size(); j++) {
                        feedback.activate(events.getInt(j));
                    }
                }
            }
        }
    }

    @Test
    void testLocationFeedbackExperiments() throws Exception {
        ScalaUtil.runTasks(cases, bug -> {
            try {
                bug.test();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
