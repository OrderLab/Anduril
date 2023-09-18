package feedback;

import feedback.common.ActionMayThrow;
import feedback.common.Env;
import feedback.diff.ThreadDiff;
import feedback.log.Log;
import feedback.parser.InjectionRecordsReader;
import feedback.parser.LogParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class CommandLine {
    private final org.apache.commons.cli.CommandLine cmd;

    private CommandLine(final org.apache.commons.cli.CommandLine cmd) {
        this.cmd = cmd;
    }

    public static void main(final String[] args) throws IOException, ExecutionException, InterruptedException {
        Env.enter();
        try {
            new CommandLine(parseCommandLine(args)).run();
        } finally {
            Env.exit();
        }
    }

    private void run() throws IOException, ExecutionException, InterruptedException {
        if (cmd.hasOption("append")) {
            final File file = new File(cmd.getOptionValue("append"));
            final JsonObject json = JsonUtil.loadJson(file);
            JsonUtil.dumpJson(this.jsonHandler(json).build(), file);
        } else if (cmd.hasOption("object")) {
            try (final ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    Files.newOutputStream(Paths.get(cmd.getOptionValue("object"))))) {
                objectOutputStream.writeObject(this.objectHandler());
            }
        } else {
            if (cmd.hasOption("output")) {
                final File file = new File(cmd.getOptionValue("output"));
                // If the directory doesn't exist we need to create it
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    throw new RuntimeException("fail to create the necessaries directories");
                }
                try (final PrintStream writer = new PrintStream(file)) {
                    this.outputHandler(writer);
                }
            } else {
                this.outputHandler(System.out);
            }
        }
    }

    private JsonObjectBuilder jsonHandler(final JsonObject json)
            throws ExecutionException, InterruptedException {
        final JsonObjectBuilder result = JsonUtil.json2builder(json);
        if (cmd.hasOption("location-feedback")) {
            if (json.containsKey("feedback")) {
                throw new RuntimeException("location feedback result existed at json");
            }
            final JsonArrayBuilder array = JsonUtil.createArrayBuilder();
            this.computeLocationFeedback(array::add);
            result.add("feedback", array);
        }
        if (cmd.hasOption("time-feedback")) {
            throw new RuntimeException("not support");
//            if (json.containsKey("timeFeedback")) {
//                throw new Exception("time feedback result existed at json");
//            }
//            final JsonArrayBuilder array = JsonUtil.createArrayBuilder();
//            this.computeTimeFeedback(e -> {});
//            result.add("timeFeedback", array);
        }
        if (cmd.hasOption("diff")) {
            if (json.containsKey("diff")) {
                throw new RuntimeException("diff result existed at json");
            }
            final JsonArrayBuilder array = JsonUtil.createArrayBuilder();
            this.computeDiff(e -> array.add(e.toString()));
            result.add("diff", array);
        }
        if (cmd.hasOption("double-diff")) {
            throw new RuntimeException("not support");
        }
        if (cmd.hasOption("double-diff-set")) {
            throw new RuntimeException("not support");
        }
        return result;
    }

    private void outputHandler(final PrintStream printer)
            throws ExecutionException, InterruptedException {
        if (cmd.hasOption("location-feedback")) {
            this.computeLocationFeedback(printer::println);
        }
        if (cmd.hasOption("time-feedback")) {
            throw new RuntimeException("not support");
        }
        if (cmd.hasOption("diff")) {
            this.computeDiff(printer::println);
        }
        if (cmd.hasOption("double-diff")) {
            this.computeDoubleDiff(printer::println);
        }
        if (cmd.hasOption("double-diff-set")) {
            this.computeDoubleDiffSet(printer::println);
        }
        if (cmd.hasOption("log")) {
            LogStatistics.countLog(LogParser.parseLog(cmd.getOptionValue("log")), printer::println);
        }
        if (cmd.hasOption("unique-count")) {
            LogStatistics.countUniqueFaults(LogParser.parseLog(cmd.getOptionValue("unique-count")), printer::println);
        }
        if (cmd.hasOption("stacktrace-injection")) {
            LogStatistics.StackTraceInjection[] injectionArray = LogStatistics.collectExceptionStackTrace(LogParser.parseLog(cmd.getOptionValue("unique-count")));
            String path = "stacktrace.json";
            final JsonArrayBuilder injectionsJson = Json.createArrayBuilder();
            for (LogStatistics.StackTraceInjection injection : injectionArray) {
                final JsonArrayBuilder stackTrace = Json.createArrayBuilder();
                for (int i = 0; i < injection.stackTrace().length; i++) {
                    stackTrace.add(injection.stackTrace()[i]);
                }
                injectionsJson.add(Json.createObjectBuilder()
                        .add("exception", injection.exception())
                        .add("class", injection.className())
                        .add("method", injection.methodName())
                        .add("line", injection.lineNumber())
                        .add("stackTrace", stackTrace));
            }
            final JsonObject json = Json.createObjectBuilder()
                    .add("injections", injectionsJson).build();
            final Map<String, Object> options = new HashMap<>();
            options.put(JsonGenerator.PRETTY_PRINTING, true);
            final JsonWriterFactory writerFactory = Json.createWriterFactory(options);
            try (final FileWriter fw = new FileWriter(path);
                 final JsonWriter jsonWriter = writerFactory.createWriter(fw)) {
                jsonWriter.writeObject(json);
            } catch (final IOException ignored) { }
        }
    }

    private Serializable objectHandler()
            throws ExecutionException, InterruptedException {
        if (cmd.hasOption("time-feedback")) {
            if (cmd.hasOption("trace")) {
                return this.computeTimeFeedbackWithCSVTrace();
            } else {
                return this.computeTimeFeedback();
            }
        }
        throw new RuntimeException("nothing to produce");
    }

    // WARN: we assume cmd is thread-safe (or will not change)

    private void computeLocationFeedback(final ActionMayThrow<Integer> action)
            throws ExecutionException, InterruptedException {
        final Future<Log> good = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("good")));
        final Future<Log> bad = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("bad")));
        final Future<Log> trial = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("trial")));
        final Future<JsonObject> spec = Env.submit(() -> JsonUtil.loadJson(cmd.getOptionValue("spec")));
        Algorithms.computeLocationFeedback(good.get(), bad.get(), trial.get(), spec.get(), action);
    }

    private Serializable computeTimeFeedback()
            throws ExecutionException, InterruptedException {
        final Future<Log> good = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("good")));
        final Future<Log> bad = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("bad")));
        final Future<JsonObject> spec = Env.submit(() -> JsonUtil.loadJson(cmd.getOptionValue("spec")));
        return Algorithms.computeTimeFeedback(good.get(), bad.get(), spec.get());
    }

    private Serializable computeTimeFeedbackWithCSVTrace()
            throws ExecutionException, InterruptedException {
        final Future<Log> good = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("good")));
        final Future<Log> bad = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("bad")));
        final Future<JsonObject> spec = Env.submit(() -> JsonUtil.loadJson(cmd.getOptionValue("spec")));
        return Algorithms.computeTimeFeedbackWithTraceCSV(good.get(), bad.get(), spec.get(), InjectionRecordsReader.readRecordCSVs(cmd.getOptionValue("trace")));
    }

    private void computeDiff(final ActionMayThrow<ThreadDiff.CodeLocation> action)
            throws ExecutionException, InterruptedException {
        final Future<Log> good = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("good")));
        final Future<Log> bad = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("bad")));
        Algorithms.computeDiff(good.get(), bad.get(), action);
    }

    private void computeDoubleDiff(final ActionMayThrow<ThreadDiff.CodeLocation> action)
            throws ExecutionException, InterruptedException {
        final Future<Log> good = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("good")));
        final Future<Log> bad = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("bad")));
        final Future<Log> trial = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("trial")));
        Algorithms.computeDoubleDiff(good.get(), bad.get(), trial.get(), action);
    }

    private void computeDoubleDiffSet(final ActionMayThrow<ThreadDiff.CodeLocation> action)
            throws ExecutionException, InterruptedException {
        final Future<Log> good = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("good")));
        final Future<Log> bad = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("bad")));
        final Future<Log> trial = Env.submit(() -> LogParser.parseLog(cmd.getOptionValue("trial")));
        Algorithms.computeDoubleDiff1(good.get(), bad.get(), trial.get(), action);
    }

    private static Options getOptions() {
        final Options options = new Options();

        final Option output = new Option("o", "output", true, "output file");
        options.addOption(output);

        final Option append = new Option("a", "append", true, "append to json");
        options.addOption(append);

        final Option object = new Option("obj", "object", true, "append to binary object");
        options.addOption(object);

        final Option good = new Option("g", "good", true, "good run log");
        options.addOption(good);

        final Option bad = new Option("b", "bad", true, "bad run log");
        options.addOption(bad);

        final Option trial = new Option("t", "trial", true, "trial run log");
        options.addOption(trial);

        final Option spec = new Option("s", "spec", true,
                "the result json of the static reasoning");
        options.addOption(spec);

        final Option diff = new Option("d", "diff", false,
                "only compute the diff of the good run and the bad run");
        options.addOption(diff);

        final Option dd = new Option("dd", "double-diff", false,
                "only compute the diff of the good run and the bad run and the trial run");
        options.addOption(dd);


        final Option dds = new Option("dds", "double-diff-set", false,
                "only compute the diff of the good run and the bad run and the trial run using set minus");
        options.addOption(dds);

        final Option timeFeedback = new Option("tf", "time-feedback", false,
                "compute the time-based feedback based on the trial run, the good run, the bad run");
        options.addOption(timeFeedback);

        final Option locationFeedback = new Option("lf", "location-feedback", false,
                "compute the location-based feedback based on the trial run, the good run, the bad run");
        options.addOption(locationFeedback);

        final Option log = new Option("l", "log", true, "arbitrary log");
        options.addOption(log);

        final Option uc = new Option("u", "unique-count", true, "arbitrary log");
        options.addOption(uc);

        final Option trace = new Option("tr", "trace", true, "injection trace csv");
        options.addOption(trace);

        final Option stackTrace = new Option("st", "stacktrace-injection", false, "stacktrace-injection mode");
        options.addOption(stackTrace);

        return options;
    }

    private static org.apache.commons.cli.CommandLine parseCommandLine(final String[] args) {
        final Options options = getOptions();
        try {
            return new org.apache.commons.cli.DefaultParser().parse(options, args);
        } catch (org.apache.commons.cli.ParseException e) {
            new org.apache.commons.cli.HelpFormatter().printHelp("utility-name", options);
            throw new RuntimeException("fail to parse the arguments");
        }
    }
}
