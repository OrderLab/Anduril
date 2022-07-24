package feedback;

import feedback.diff.ThreadDiff;
import feedback.parser.DistributedLog;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.File;
import java.io.PrintStream;
import java.util.function.Consumer;

public final class CommandLine {
    private final org.apache.commons.cli.CommandLine cmd;

    private CommandLine(final org.apache.commons.cli.CommandLine cmd) {
        this.cmd = cmd;
    }

    public static void main(final String[] args) throws Exception {
        new CommandLine(parseCommandLine(args)).run();
    }

    private void run() throws Exception {
        if (cmd.hasOption("append")) {
            final File file = new File(cmd.getOptionValue("append"));
            final JsonObject json = JsonUtil.loadJson(file);
            JsonUtil.dumpJson(this.jsonHandler(json).build(), file);
        } else {
            if (cmd.hasOption("output")) {
                final File file = new File(cmd.getOptionValue("output"));
                file.getParentFile().mkdirs(); // If the directory doesn't exist we need to create it
                try (final PrintStream writer = new PrintStream(file)) {
                    this.outputHandler(writer);
                }
            } else {
                this.outputHandler(System.out);
            }
        }
    }

    private JsonObjectBuilder jsonHandler(final JsonObject json) throws Exception {
        final JsonObjectBuilder result = JsonUtil.json2builder(json);
        if (cmd.hasOption("location-feedback")) {
            if (json.containsKey("locationFeedback")) {
                throw new Exception("location feedback result existed at json");
            }
            final JsonArrayBuilder array = JsonUtil.createArrayBuilder();
            this.computeLocationFeedback(array::add);
            result.add("locationFeedback", array);
        }
        if (cmd.hasOption("time-feedback")) {
            if (json.containsKey("timeFeedback")) {
                throw new Exception("time feedback result existed at json");
            }
            final JsonArrayBuilder array = JsonUtil.createArrayBuilder();
            this.computeTimeFeedback();
            result.add("timeFeedback", array);
        }
        if (cmd.hasOption("diff")) {
            if (json.containsKey("diff")) {
                throw new Exception("diff result existed at json");
            }
            final JsonArrayBuilder array = JsonUtil.createArrayBuilder();
            this.computeDiff(e -> array.add(e.toString()));
            result.add("diff", array);
        }
        return result;
    }

    private void outputHandler(final PrintStream printer) throws Exception {
        if (cmd.hasOption("location-feedback")) {
            this.computeLocationFeedback(printer::println);
        }
        if (cmd.hasOption("time-feedback")) {
            this.computeTimeFeedback();
        }
        if (cmd.hasOption("diff")) {
            this.computeDiff(printer::println);
        }
    }

    private void computeLocationFeedback(final Consumer<Integer> consumer) throws Exception {
        final DistributedLog good = new DistributedLog(cmd.getOptionValue("good"));
        final DistributedLog bad = new DistributedLog(cmd.getOptionValue("bad"));
        final DistributedLog trial = new DistributedLog(cmd.getOptionValue("trial"));
        final JsonObject spec = JsonUtil.loadJson(cmd.getOptionValue("spec"));
        Algorithms.computeLocationFeedback(good, bad, trial, spec, consumer);
    }

    private void computeTimeFeedback() throws Exception {
        final DistributedLog good = new DistributedLog(cmd.getOptionValue("good"));
        final DistributedLog bad = new DistributedLog(cmd.getOptionValue("bad"));
        final DistributedLog trial = new DistributedLog(cmd.getOptionValue("trial"));
        final JsonObject spec = JsonUtil.loadJson(cmd.getOptionValue("spec"));
        Algorithms.computeTimeFeedback(good, bad, trial, spec, e -> {});
    }

    private void computeDiff(final Consumer<ThreadDiff.ThreadLogEntry> consumer) throws Exception {
        final DistributedLog good = new DistributedLog(cmd.getOptionValue("good"));
        final DistributedLog bad = new DistributedLog(cmd.getOptionValue("bad"));
        Algorithms.computeDiff(good, bad, consumer);
    }

    static Options getOptions() {
        final Options options = new Options();

        final Option output = new Option("o", "output", true, "output file");
        options.addOption(output);

        final Option append = new Option("a", "append", true, "append to json");
        options.addOption(append);

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

        final Option timeFeedback = new Option("tf", "time-feedback", false,
                "compute the time-based feedback based on the trial run, the good run, the bad run");
        options.addOption(timeFeedback);

        final Option locationFeedback = new Option("lf", "location-feedback", false,
                "compute the location-based feedback based on the trial run, the good run, the bad run");
        options.addOption(locationFeedback);

        return options;
    }

    static org.apache.commons.cli.CommandLine parseCommandLine(final String[] args) throws Exception {
        final Options options = getOptions();
        try {
            return new org.apache.commons.cli.DefaultParser().parse(options, args);
        } catch (org.apache.commons.cli.ParseException e) {
            new org.apache.commons.cli.HelpFormatter().printHelp("utility-name", options);
            throw new Exception("fail to parse the arguments");
        }
    }
}
