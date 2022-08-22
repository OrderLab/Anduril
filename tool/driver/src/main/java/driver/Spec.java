package driver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.Scanner;

final class Spec {
    public final static int TRIAL_LIMIT = 1_000_000;

    public final int start, end;
    public final boolean baseline, experiment;
    public final boolean distributed;
    public final int processNumber;
    public final String currentDir;

    public final File experimentPath, specPath;
    public final File configPath;

    Spec(final String[] args) throws Exception {
        final CommandLine cmd = parseCommandLine(args);
        this.configPath = new File(cmd.getOptionValue("config"));
        if (!this.configPath.exists() || this.configPath.isDirectory()) {
            throw new Exception("invalid config " + this.configPath.getPath());
        }
        if (cmd.hasOption("start")) {
            this.start = Integer.parseInt(cmd.getOptionValue("start"));
        } else {
            this.start = 0;
        }
        if (this.start < 0) {
            throw new Exception("invalid start id: " + this.start);
        }
        if (cmd.hasOption("end")) {
            this.end = Integer.parseInt(cmd.getOptionValue("end"));
            if (this.end > TRIAL_LIMIT) {
                throw new Exception(this.end + " exceeds trials end id limit");
            }
        } else {
            this.end = TRIAL_LIMIT;
        }
        this.baseline = cmd.hasOption("baseline");
        this.experiment = cmd.hasOption("experiment");
        if (this.baseline == this.experiment) {
            throw new Exception("choose either baseline or experiment");
        }
        if (cmd.hasOption("nodes")) {
            this.distributed = true;
            this.processNumber = Integer.parseInt(cmd.getOptionValue("nodes"));
        } else {
            this.distributed = false;
            this.processNumber = -1;
        }
        this.currentDir = System.getProperty("user.dir");
        experimentPath = new File(cmd.getOptionValue("path"));
        if (experimentPath.exists()) {
            if (experimentPath.isDirectory()) {
                if (experimentPath.listFiles().length > 0) {
                    if (!cmd.hasOption("yes") &&
                            !getYes("Found existing files in " + experimentPath.getPath())) {
                        throw new Exception("Found existing files in " + experimentPath.getPath());
                    }
                }
            } else {
                throw new Exception(experimentPath.getPath() + " is not a directory");
            }
        } else {
            if (!experimentPath.mkdirs()) {
                throw new Exception("can't create directory " + experimentPath.getPath());
            }
        }
        if (this.baseline) {
            this.specPath = null;
        } else {
            this.specPath = new File(cmd.getOptionValue("spec"));
            if (!this.specPath.exists()) {
                throw new Exception("can't find injection spec json " + this.specPath.getPath());
            }
        }
    }

    private static Options getOptions() {
        final Options options = new Options();

        final Option start = new Option("s", "start", true,
                "the first trial id (inclusive)");
        options.addOption(start);

        final Option end = new Option("e", "end", true, "the end trial id (exclusive)");
        options.addOption(end);

        final Option config = new Option("c", "config", true, "experiment config file");
        config.setRequired(true);
        options.addOption(config);

        final Option baseline = new Option("b", "baseline", false, "run baseline");
        options.addOption(baseline);

        final Option experiment = new Option("ex", "experiment", false, "run experiment");
        options.addOption(experiment);

        final Option nodes = new Option("n", "nodes", true, "distributed nodes");
        options.addOption(nodes);

        final Option path = new Option("p", "path", true, "dir path for trials");
        path.setRequired(true);
        options.addOption(path);

        final Option yes = new Option("y", "yes", false, "say yes to warning prompt");
        options.addOption(yes);

        final Option spec = new Option("spec", "injection-spec", true,
                "path of injection spec json");
        options.addOption(spec);

        return options;
    }

    private static CommandLine parseCommandLine(final String[] args) throws Exception {
        final Options options = getOptions();
        try {
            return new org.apache.commons.cli.DefaultParser().parse(options, args);
        } catch (org.apache.commons.cli.ParseException e) {
            new org.apache.commons.cli.HelpFormatter().printHelp("utility-name", options);
            throw new Exception("fail to parse the arguments");
        }
    }

    private boolean getYes(final String prompt) {
        final Scanner kbd = new Scanner (System.in);
        while (true) {
            System.out.println(prompt);
            System.out.print("Do you want to continue? type yes or no: ");
            final String decision = kbd.nextLine();
            switch(decision) {
                case "yes": return true;
                case "no": return false;
                default: System.out.println("please enter again");
            }
        }
    }
}
