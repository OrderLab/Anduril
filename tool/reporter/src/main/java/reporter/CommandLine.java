package reporter;

import feedback.log.Log;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import feedback.JsonUtil;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import reporter.check.Checker;
import reporter.parser.DistributedLogLoader;

import javax.json.JsonObject;
import java.io.IOException;



public class CommandLine {
    private final org.apache.commons.cli.CommandLine cmd;

    private CommandLine(final org.apache.commons.cli.CommandLine cmd) {
        this.cmd = cmd;
    }

    public static void main(final String[] args) throws Exception {
        new CommandLine(parseCommandLine(args)).run();
    }

    //By now the most simple mode
    private void run() throws Exception {
        computeFirstReproduction();
    }

    private void computeFirstReproduction() throws IOException {
        final JsonObject spec = JsonUtil.loadJson(cmd.getOptionValue("spec"));
        final Checker checker = new Checker(spec);
        final DistributedLogLoader loader;
        if (cmd.hasOption("distributed")) {
            loader = new DistributedLogLoader(cmd.getOptionValue("trial-directory"), true);
        } else {
            loader = new DistributedLogLoader(cmd.getOptionValue("trial-directory"), false);
        }
        //Traverse through all the trials
        int index = 0;
        while (index <= 1000000) {
            //Get parsed log files and injection points id.
            Log trial = loader.getDistributedLog(index);
            int injectionId = loader.getInjectionId(index);
System.out.println(index);
            if (checker.checkTrial(trial, injectionId)) {

                System.out.println("The Index of first trial that reproduce the bug: " + index);

                //DateTime start = loader.getDistributedLog(0).logs[0].entries[0].datetime;
                //int length = trial.logs[0].entries.length;
                //DateTime end = trial.logs[0].entries[length - 1].datetime;
                //Duration elapsed = new Duration(start,end);
                //System.out.println("The elapsed time to reproduce is : " + elapsed.toString());
                continue;
            }
            index++;
        }
    }


    private static Options getOptions() {
        final Options options = new Options();

        final Option trialDirectory = new Option("t", "trial-directory", true,
                "the path of the trial directory");
        options.addOption(trialDirectory);

        final Option spec = new Option("s", "spec", true,
                "the result json of the static reasoning");
        options.addOption(spec);

        final Option dis = new Option("d", "distributed", false,
                "it is in distributed mode");
        options.addOption(dis);

        return options;
    }

    private static org.apache.commons.cli.CommandLine parseCommandLine(final String[] args) throws Exception {
        final Options options = getOptions();
        try {
            return new org.apache.commons.cli.DefaultParser().parse(options, args);
        } catch (org.apache.commons.cli.ParseException e) {
            new org.apache.commons.cli.HelpFormatter().printHelp("utility-name", options);
            throw new Exception("fail to parse the arguments");
        }
    }
}
