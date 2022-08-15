package feedback.time;

import feedback.FeedbackTestUtil;
import feedback.parser.DistributedLog;
import feedback.parser.Log;
import feedback.parser.LogTestUtil;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

final class TimeDifferenceTest {
    private static final Logger LOG = LoggerFactory.getLogger(TimeDifferenceTest.class);

    private static abstract class BugCase {
        final String name;
        final DistributedLog good, bad, trial;
        final JsonObject spec;

        private BugCase(final String name, final Path tempDir) throws IOException {
            if (name == null) {
                this.name = FeedbackTestUtil.parseCaseDirName(this.getClass().getSimpleName());
            } else {
                this.name = name;
            }
            this.prepareTempFiles(tempDir);
            final Path dir = tempDir.resolve(this.name);
            this.good = new DistributedLog(dir.resolve("good-run-log"));
            this.bad = new DistributedLog(dir.resolve("bad-run-log"));
            this.trial = new DistributedLog(dir.resolve("trial-run-log"));
            this.spec = LogTestUtil.loadJson("record-inject/" + this.name + "/tree.json");
        }

        abstract void prepareTempFiles(final Path tempDir) throws IOException;
        abstract void print(final Consumer<String> printer);
        abstract void print(final List<Timing> timeline, final Consumer<String> printer);

        static class Timing {
            private final DateTime time;
            private final String literal;

            private Timing(final DateTime time, final String literal) {
                this.time = time;
                this.literal = literal;
            }

            @Override
            public String toString() {
                return this.literal;
            }
        }
    }

    private static class TestCase extends BugCase {
        private TestCase(final Path tempDir, final String name) throws IOException {
            super(name, tempDir);
        }

        @Override
        void prepareTempFiles(final Path tempDir) throws IOException {
            LogTestUtil.initTempFile("ground-truth/" + super.name + "/good-run-log.txt",
                    tempDir.resolve(super.name + "/good-run-log"));
            LogTestUtil.initTempFile("ground-truth/" + super.name + "/bad-run-log.txt",
                    tempDir.resolve(super.name + "/bad-run-log"));
            LogTestUtil.initTempFile("record-inject/" + super.name + "/good-run-log.txt",
                    tempDir.resolve(super.name + "/trial-run-log"));
        }

        @Override
        void print(final Consumer<String> printer) {
            final TimeDifference trial2bad = new TimeDifference(super.trial, super.bad);
            final TimeDifference good2bad = new TimeDifference(super.good, super.bad);
            final Log trial = super.trial.logs[0], good = super.good.logs[0], bad = super.bad.logs[0];
            final List<Timing> timeline = new ArrayList<>(
                    trial.injections.length + trial.entries.length + good.entries.length + bad.entries.length);
            final Map<Integer, Integer> occurrences = new TreeMap<>();
            Arrays.stream(trial.injections).forEach(injection -> timeline.add(
                    new Injection(trial2bad.good2bad(injection.datetime), injection.injection,
                            occurrences.merge(injection.injection, 1, Integer::sum))));
            Arrays.stream(trial.entries).forEach(entry -> timeline.add(
                    new LogEntry(trial2bad.good2bad(entry.datetime), LogType.TRIAL(), entry.msg)));
            Arrays.stream(good.entries).forEach(entry -> timeline.add(
                    new LogEntry(good2bad.good2bad(entry.datetime), LogType.GOOD(), entry.msg)));
            Arrays.stream(bad.entries).forEach(entry -> timeline.add(
                    new LogEntry(entry.datetime, LogType.BAD(), entry.msg)));
            timeline.sort(Comparator.comparing(timing -> timing.time)); // use stable merge sort
            this.print(timeline, printer);
        }

        @Override
        void print(final List<Timing> timeline, final Consumer<String> printer) {
            // each line:  trial log   good log   bad log
            int count = 0;
            DateTime begin = null, end = null;
            for (final Timing timing : timeline) {
                if (timing instanceof Injection) {
                    if (count == 0) {
                        begin = timing.time;
                    }
                    end = timing.time;
                    count++;
                } else {
                    if (count != 0) {
                        printer.accept(FeedbackTestUtil.getInjectionInterval(count, begin, end));
                        count = 0;
                    }
                    printer.accept(timing.toString());
                }
            }
            if (count != 0) {
                printer.accept(FeedbackTestUtil.getInjectionInterval(count, begin, end));
            }
        }

        static final class Injection extends Timing {
            Injection(final DateTime time, final int injection, final int occurrence) {
                super(time, FeedbackTestUtil.injectionTimingFormat(time, injection, occurrence));
            }
        }

        static final class LogEntry extends Timing {
            LogEntry(final DateTime time, final scala.Enumeration.Value type, final String msg) {
                super(time, FeedbackTestUtil.logEntryTimingFormat(time, type, msg));
            }
        }
    }

    @Test
    void testBugCases(final @TempDir Path tempDir) throws IOException {
        final BugCase[] cases = new BugCase[] {
                new TestCase(tempDir, "hdfs-12070"),
//                new TestCase(tempDir, "hbase-19608"),
        };
        for (final BugCase bug : cases) {
            bug.print(LOG::debug);
        }
    }
}
