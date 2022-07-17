package parser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import parser.diff.LogDiff;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ParserCommand {
    public static void main(final String[] args) throws Exception {
        final CommandLine cmd = parseCommandLine(args);
        final Log good = getLogFromFilePath(cmd.getOptionValue("good"));
        final Log bad = getLogFromFilePath(cmd.getOptionValue("good"));
        if (cmd.hasOption("diff")) {
            if (cmd.hasOption("output")) {
                final File file = new File(cmd.getOptionValue("output"));
                file.getParentFile().mkdirs(); // If the directory doesn't exist we need to create it
                try (final PrintStream writer = new PrintStream(file)) {
                    diffToOutput(good, bad, writer);
                }
            } else {
                diffToOutput(good, bad, System.out);
            }
        } else if (cmd.hasOption("feedback")) {
            final Log trial = getLogFromFilePath(cmd.getOptionValue("trial"));
            // TODO: finish the algorithm
        } else {
            throw new Exception("You must enter a command!");
        }
    }

    public static void diffToOutput(final Log good, final Log bad, final PrintStream printer) {
        new LogDiff(good, bad).dumpBadDiff(printer::println);
    }

    static Log getLogFromFilePath(String path) throws IOException {
        final List<String> text = new ArrayList<>();
        try (final Stream<String> stream = Files.lines(Paths.get(path))) {
            stream.forEach(text::add);
        }
        return Parser.parseLog(text.toArray(new String[0]));
    }

    static CommandLine parseCommandLine(final String[] args) throws Exception {
        final Options options = new Options();

        final Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);

        final Option good = new Option("g", "good", true, "good run log");
        good.setRequired(true);
        options.addOption(good);

        final Option bad = new Option("b", "bad", true, "bad run log");
        bad.setRequired(true);
        options.addOption(bad);

        final Option trial = new Option("t", "trial", true, "trial run log");
        options.addOption(trial);

        final Option diff = new Option("d", "diff", false,
                "only compute the diff of the good run and the bad run");
        options.addOption(diff);

        final Option feedback = new Option("f", "feedback", false,
                "only compute the feedback based on the trial run, the good run and the bad run");
        options.addOption(feedback);

        try {
            return new org.apache.commons.cli.DefaultParser().parse(options, args);
        } catch (org.apache.commons.cli.ParseException e) {
            new org.apache.commons.cli.HelpFormatter().printHelp("utility-name", options);
            throw new Exception("fail to parse the arguments");
        }
    }
}
