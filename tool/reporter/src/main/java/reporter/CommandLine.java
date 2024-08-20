package reporter;

import feedback.log.Log;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import feedback.JsonUtil;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import reporter.check.Checker;
import reporter.parser.DistributedLogLoader;
import runtime.graph.PriorityGraph;
import runtime.time.TimePriorityTable;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;


public class CommandLine {
    private final org.apache.commons.cli.CommandLine cmd;

    private CommandLine(final org.apache.commons.cli.CommandLine cmd) {
        this.cmd = cmd;
    }

    public static void main(final String[] args) throws Exception {
        new CommandLine(parseCommandLine(args)).run();
        System.exit(1);
    }

    //By now the most simple mode
    private void run() throws Exception {
        computeFirstReproduction();
        //getRanks();
        //collectTimeStatistics();
    }

    private void collectTimeStatistics() throws IOException {
        int inject = Integer.parseInt(cmd.getOptionValue("injection"));
        int sl = Integer.parseInt(cmd.getOptionValue("symptom-log"));
        final TimePriorityTable timePriorityTable = TimePriorityTable.load(cmd.getOptionValue("time-table"));
        final JsonObject spec = JsonUtil.loadJson(cmd.getOptionValue("spec"));
        if (sl != -1) {
            timePriorityTable.injections.get(inject).forEach((k, v) -> {
                if (v.timePriorities.containsKey(sl)) {
                    System.out.printf("%d,%d,%d,%d,%d", sl, inject, k.occurrence, k.pid, v.timePriorities.get(sl));
                    System.out.println();
                }
            });
        } else {
            int startNumber = spec.getInt("start");
            for (int log = 0; log < startNumber; log++){
                int finalLog = log;
                timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) -> {
                    if (v.timePriorities.containsKey(finalLog)) {
                        System.out.printf("%d,%d,%d,%d,%d", finalLog, injection, k.occurrence, k.pid, v.timePriorities.get(finalLog));
                        System.out.println();
                    }

                }));
            }
        }
    }


    private void getRanks() throws IOException {
        int injection = Integer.parseInt(cmd.getOptionValue("injection"));
        final JsonObject spec = JsonUtil.loadJson(cmd.getOptionValue("spec"));
        final TimePriorityTable timePriorityTable = TimePriorityTable.load(cmd.getOptionValue("time-table"));;
        int start = spec.getInt("start");
        final int num_trials = Objects.requireNonNull(new File(cmd.getOptionValue("trial-directory")).listFiles((file, name)
                -> name.endsWith(".json"))).length;
        final Map<Integer, Integer> active = new TreeMap<>();
        final Set<Integer> injected = new HashSet<>();
        for (int i = -1; i < num_trials; i++) {
            if (i != -1) {
                File result = new File(cmd.getOptionValue("trial-directory") + "/injection-" + i + ".json");
                try (final InputStream inputStream = Files.newInputStream(result.toPath());
                     final JsonReader reader = Json.createReader(inputStream)) {
                    final JsonObject json = reader.readObject();
                    final int trialId = json.getInt("trial_id");
		    if (json.containsKey("id")) {
                      injected.add(json.getInt("id"));
		    } 
                    final JsonArray events = json.getJsonArray("feedback");
                    for (int j = 0; j < events.size(); j++) {
                        active.merge(events.getInt(j), -1, Integer::sum);
                    }
                    for (int j = 0; j < start; j++) {
                        active.merge(j, 1, Integer::sum);
                    }

                } catch (final IOException ignored) {
                    System.out.println("For debug purpose:" + ignored);
                }
            }
            PriorityGraph graph = new PriorityGraph("none",spec);
            for (int j = 0; j < graph.startNumber; j++) {
                graph.setStartValue(j, active.getOrDefault(j, 0));
            }
            final Set<Integer> set = new HashSet<>();
            graph.calculatePriorities((injectionId) -> {
                //if (!injected.contains(injectionId)) {
                if (timePriorityTable.injection2Log2Time.get(injectionId) != null && !injected.contains(injectionId)) {
                    set.add(injectionId);
                }
                return injectionId == injection;
            });
            System.out.printf("%d,%d",i+2,set.size());
            System.out.println();
        }
    }

    private void computeFirstReproduction() throws IOException {
        Checker checker = null;
        if (cmd.hasOption("spec")) {
            final JsonObject spec = JsonUtil.loadJson(cmd.getOptionValue("spec"));
            checker = new Checker(spec);
        } else {
            checker = new Checker(cmd.getOptionValue("case-name"));
        }
        final DistributedLogLoader loader;
        if (cmd.hasOption("distributed")) {
            loader = new DistributedLogLoader(cmd.getOptionValue("trial-directory"), true);
        } else {
            loader = new DistributedLogLoader(cmd.getOptionValue("trial-directory"), false);
        }
        //Traverse through all the trials
        int index = 0;
        while (index <= 2000) {
            //Get parsed log files and injection points id.
            final Log trial;
            try {trial = loader.getDistributedLog(index);}
            catch (RuntimeException e) {
                System.out.println(e.toString());
                System.out.println("Be not able to parse trial with id:" + index);
                index++;
                continue;
            } catch (NoSuchFileException e) {
                System.out.println(e.toString());
                System.out.println("Did not find out trial with id:" + index);
                break;
            }

            if (cmd.hasOption("baseline")) {
                if (checker.checkBaselineTrial(trial)) {
                    System.out.println("The Index of first trial that reproduce the bug: " + index);

                    DateTime start = loader.getDistributedLog(0).showtime();
                    DateTime end = loader.getDistributedLog(index).showtime();
                    Duration elapsed = new Duration(start, end);
                    System.out.println("The elapsed time to reproduce is : " + elapsed.toString());
                    break;
                }
            } else {
                final int injectionId = loader.getInjectionId(index);
                if (checker.checkTrial(trial, injectionId)) {
                    System.out.println("The Index of first trial that reproduce the bug: " + index);

                    DateTime start = loader.getDistributedLog(0).showtime();
                    DateTime end = loader.getDistributedLog(index).showtime();
                    Duration elapsed = new Duration(start, end);
                    System.out.println("The elapsed time to reproduce is : " + elapsed.toString());
                    break;
                }
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

        final Option injection = new Option("i", "injection", true,
                "target injection id for calculating ranks");
        options.addOption(injection);

        final Option time = new Option("tt", "time-table", true,
                "path for time table");
        options.addOption(time);

        final Option symptomLog = new Option("sl", "symptom-log", true,
                "symptom log");
        options.addOption(symptomLog);

        final Option baseline = new Option("b", "baseline", false,
                "whether it is SOTA evaluation");
        options.addOption(baseline);

        final Option case_name = new Option("n", "case-name", true,
                "case name for use in checker");
        options.addOption(case_name);

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
