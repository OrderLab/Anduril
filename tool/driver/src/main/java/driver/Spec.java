package driver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

final class Spec {
    public final boolean baseline, experiment;
    public final boolean distributed;
    public final int processNumber;
    public final Path currentDir;

    public final Path experimentPath;
    public final File configFile, specFile;

    public final int trial_limit;

    Spec(final String[] args) {
        final CommandLine cmd = parseCommandLine(args);
        this.configFile = new File(cmd.getOptionValue("config"));
        if (!this.configFile.exists() || this.configFile.isDirectory()) {
            throw new RuntimeException("invalid config " + this.configFile.getPath());
        }
        this.baseline = cmd.hasOption("baseline");
        this.experiment = cmd.hasOption("experiment");
        if (this.baseline == this.experiment) {
            throw new RuntimeException("choose either baseline or experiment");
        }
        if (cmd.hasOption("nodes")) {
            this.distributed = true;
            this.processNumber = Integer.parseInt(cmd.getOptionValue("nodes"));
        } else {
            this.distributed = false;
            this.processNumber = -1;
        }
        this.currentDir = Paths.get(System.getProperty("user.dir"));
        final File experimentFile = new File(cmd.getOptionValue("path"));
        this.experimentPath = experimentFile.toPath();
        if (experimentFile.exists()) {
            if (!experimentFile.isDirectory()) {
                throw new RuntimeException(experimentFile.getPath() + " is not a directory");
            }
        } else {
            if (!experimentFile.mkdirs()) {
                throw new RuntimeException("can't create directory " + experimentFile.getPath());
            }
        }
        if (this.baseline) {
            this.specFile = null;
        } else {
            this.specFile = new File(cmd.getOptionValue("spec"));
            if (!this.specFile.exists()) {
                throw new RuntimeException("can't find injection spec json " + this.specFile.getPath());
            }
        }
        if (cmd.hasOption("trial-limit")) {
            trial_limit = Integer.parseInt(cmd.getOptionValue("trial-limit"));
        } else {
            trial_limit = 2000;
        }
    }

    private static Options getOptions() {
        final Options options = new Options();

        final Option config = new Option("c", "config", true, "experiment config file");
        config.setRequired(true);
        options.addOption(config);

        final Option baseline = new Option("b", "baseline", false, "run baseline");
        options.addOption(baseline);

        final Option experiment = new Option("e", "experiment", false,
                "run experiment instead of baseline");
        options.addOption(experiment);

        final Option nodes = new Option("n", "nodes", true, "distributed nodes");
        options.addOption(nodes);

        final Option path = new Option("p", "path", true, "dir path for trials");
        path.setRequired(true);
        options.addOption(path);

        final Option spec = new Option("s", "spec", true,
                "path of injection spec json");
        options.addOption(spec);

        return options;
    }

    private static CommandLine parseCommandLine(final String[] args) {
        final Options options = getOptions();
        try {
            return new org.apache.commons.cli.DefaultParser().parse(options, args);
        } catch (org.apache.commons.cli.ParseException e) {
            new org.apache.commons.cli.HelpFormatter().printHelp("utility-name", options);
            throw new RuntimeException("fail to parse the arguments");
        }
    }
}
