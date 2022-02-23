package analyzer.option;

import analyzer.AnalyzerMain;
import analyzer.phase.PhaseInfo;
import analyzer.phase.PhaseManager;
import analyzer.util.StringUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A parser for the command line arguments (main code snippets are from AutoWatchdog project).
 */
public class OptionParser {

    private static final Logger LOG = LoggerFactory.getLogger(OptionParser.class);

    private Option analysisList = Option.builder("a").longOpt("analysis").hasArgs()
            .argName("name name ...")
            .desc("list of analysis names to run on the subject software").build();

    private Option phaseOptions = Option.builder("p").longOpt("phase_option").hasArgs()
            .argName("phase key:val,key:val, ...").desc("list of phase option to be passed to Soot")
            .build();

    private final Option flakyCase = Option.builder("fc").longOpt("flaky_case").hasArg()
            .desc("flaky test jira issue name")
            .build();

    private final Option failureLogDiffLocations = Option.builder("fld").longOpt("failure_log_diff_locations")
            .hasArg().desc("file of the failure log diff")
            .build();

    private Option inputDirectoryList = Option.builder("i").longOpt("indir").hasArgs()
            .argName("directory directory ...")
            .desc("List of input directories that contain the class files of a subject software")
            .build();

    private Option inputJarList = Option.builder("j").longOpt("jar").hasArg()
            .argName("file file ...")
            .desc("List of input jar files of the subject software to be analyzed").build();

    private Option classPathList = Option.builder("x").longOpt("extra_classpath").hasArgs()
            .argName("classpath classpath ...")
            .desc("List of additional classpaths (directory or jar)").build();

    private Option classList = Option.builder("c").longOpt("class").hasArgs()
            .argName("class class ...")
            .desc("List of classes to be analyzed").build();

    private Option mainClass = Option.builder("m").longOpt("main").hasArg()
            .desc("Main class of package to be analyzed").build();

    private Option secondaryMainClassList = Option.builder("sm").longOpt("secmain").hasArgs()
            .argName("class class ...")
            .desc("List of secondary main classes").build();

    private Option outputDirectory = Option.builder("o").longOpt("outdir").hasArg()
            .argName("directory")
            .desc("Directory to output the instrumented programs").build();

    private Option genExecutable = Option.builder("e").longOpt("executable")
            .desc("Generate executable class files instead of Soot IRs").build();

    private Option noOutput = Option.builder("n").longOpt("no_output")
            .desc("Do not generate output for parsing").build();

    private Option wholeProgram = Option.builder("w").longOpt("whole")
            .desc("Whole program analysis").build();

    private Option noDebugInfo = Option.builder().longOpt("no_debug")
            .desc("Do not keep debug information in analysis").build();

    private Option listAnalysis = Option.builder().longOpt("list")
            .desc("List analysis available").build();

    private Option overrideProperties = Option.builder().longOpt("config").hasArgs()
            .argName("key:value key:value ...")
            .desc("List of key value configs, which will override the settings in the config file").build();

    private Option help = Option.builder("h").longOpt("help").desc("Print this help message")
            .build();

    private Option helpWithSootHelp = Option.builder("H").longOpt("Help")
            .desc("Print this help message along " + "with Soot help message").build();

    private Options mOptions;

    public OptionParser() {
        mOptions = new Options();

        // accept a list of analysis names
        mOptions.addOption(analysisList);
        mOptions.addOption(phaseOptions);
        mOptions.addOption(flakyCase);
        mOptions.addOption(failureLogDiffLocations);
        mOptions.addOption(inputDirectoryList);
        mOptions.addOption(inputJarList);
        mOptions.addOption(classList);
        mOptions.addOption(secondaryMainClassList);
        mOptions.addOption(mainClass);
        mOptions.addOption(outputDirectory);
        mOptions.addOption(classPathList);
        mOptions.addOption(noOutput);
        mOptions.addOption(genExecutable);
        mOptions.addOption(wholeProgram);
        mOptions.addOption(noDebugInfo);
        mOptions.addOption(listAnalysis);
        mOptions.addOption(overrideProperties);
        mOptions.addOption(help);
        mOptions.addOption(helpWithSootHelp);
    }

    public AnalyzerOptions parse(String[] args) throws OptionError {
        try {
            CommandLine cmd = new DefaultParser().parse(mOptions, args);
            AnalyzerOptions options = AnalyzerOptions.getInstance();

            /* Parsing input and output options */
            String[] jars = cmd.getOptionValues(inputJarList.getOpt());
            String[] indirs = cmd.getOptionValues(inputDirectoryList.getOpt());
            List<String> inputList = new ArrayList<String>();
            boolean outputJar = false;
            if (jars != null && jars.length > 0) {
                if (indirs != null && indirs.length > 0) {
                    throw new OptionError("Can only accept either a jar input or a directory input.");
                }
                for (String jar : jars) {
                    if (!new File(jar).isFile()) {
                        throw new OptionError(jar + " does not exist or is not a file");
                    }
                    inputList.add(jar);
                }
                // when the input contains a jar, also output a jar
                outputJar = true;
            } else {
                if (indirs != null && indirs.length > 0) {
                    for (String indir : indirs) {
                        if (!new File(indir).isDirectory()) {
                            throw new OptionError(indir + " does not exist or is not a directory");
                        }
                        inputList.add(indir);
                    }
                }
            }
            options.setInputList(inputList);
            String[] classes = cmd.getOptionValues(classList.getLongOpt());
            if (classes != null) {
                if (!inputList.isEmpty()) {
                    throw new OptionError("When classes are specified, must pass input jar or dir as argument to -x");
                }
            }
            options.setClasses(classes);
            options.setMainClass(cmd.getOptionValue(mainClass.getLongOpt()));
            String[] sec_main_classes = cmd.getOptionValues(secondaryMainClassList.getLongOpt());
            List<String> secMainClassList = new ArrayList<String>();
            if (sec_main_classes != null) {
                for (String str : sec_main_classes) {
                    secMainClassList.add(str);
                }
            }
            options.setSecondaryMainClassList(secMainClassList);

            options.setFlakyCase(cmd.getOptionValue(this.flakyCase.getLongOpt()));
            options.setDiffPath(cmd.getOptionValue(this.failureLogDiffLocations.getLongOpt()));

            boolean no_output = cmd.hasOption(noOutput.getLongOpt());
            boolean gen_executable = cmd.hasOption(genExecutable.getLongOpt());
            String outdir = cmd.getOptionValue(outputDirectory.getLongOpt());
            if (no_output) {
                if (gen_executable) {
                    throw new OptionError("Specified both no output and gen executable");
                }
            }
            options.setOutputJar(outputJar);
            options.setOutputDir(outdir);
            options.setGenExecutable(gen_executable);
            options.setNoOutput(no_output);

            /* Parsing and setting up class path */
            String java_home = System.getProperty("java.home");
            if (java_home == null || java_home.isEmpty()) {
                throw new OptionError("JAVA_HOME environment variable is not set. Make sure you set it.");
            }
            Path rt = Paths.get(java_home, "lib", "rt.jar");
            Path jce = Paths.get(java_home, "lib", "jce.jar");
            if (!Files.exists(rt) || !Files.exists(jce)) {
                throw new OptionError("rt.jar or jce.jar not found within " + java_home);
            }
            String[] extrac_cps = cmd.getOptionValues(classPathList.getLongOpt());
            String cp = "";
            if (extrac_cps != null) {
                cp = StringUtils.join(":", extrac_cps);
            }
            // Part of the tool, recipes and context manager factories will be converted to Soot
            // classes, so we need to concatenate the current running jar to the class path.
            String path = AnalyzerMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            //LOG.debug("Append " + path + " to Soot class path");
            cp = path + ":" + cp;
            if (!inputList.isEmpty()) {
                options.setClassPath(
                        String.format("%s:%s:%s:%s", cp, StringUtils.join(":", inputList),
                                rt.toString(), jce.toString()));
            } else {
                options.setClassPath(String.format("%s:%s:%s", cp, rt.toString(), jce.toString()));
            }

            /* Setup analyses */
            String[] analyses = cmd.getOptionValues(analysisList.getLongOpt());
            boolean whole = cmd.hasOption(wholeProgram.getLongOpt());
            if (analyses != null) {
                for (String analysis : analyses) {
                    PhaseInfo phase_info = PhaseManager.getInstance().getPhaseInfo(analysis);
                    if (phase_info == null) {
                        throw new OptionError(analysis + " is not a recognized analysis");
                    }
                    if (phase_info.isWholeProgram()) {
                        whole = true; // is a whole program analysis
                    }
                }
            }
            options.setIsWholeProgram(whole);
            options.setAnalyses(analyses);
            String[] phase_option_strs = cmd.getOptionValues(phaseOptions.getLongOpt());
            Map<String, List<String>> all_phase_options = new HashMap<>();
            if (phase_option_strs != null) {
                String phase_name = null;
                boolean last_is_option = false;
                for (String option_str : phase_option_strs) {
                    // Each phase option should be either a phase name or
                    // key:value,key:value,key:value,...
                    // And they must be in this order
                    String[] components = option_str.split(",");
                    for (String component : components) {
                        String[] parts = component.split(":");
                        if (parts.length == 1) {
                            if (phase_name != null && !phase_name.isEmpty() && !last_is_option) {
                                throw new OptionError("Missing phase option for " + phase_name);
                            }
                            phase_name = component;
                            last_is_option = false;
                        } else if (parts.length == 2) {
                            if (phase_name == null || phase_name.isEmpty()) {
                                throw new OptionError(
                                        "Missing phase name for phase option " + component);
                            }
                            List<String> option_list = all_phase_options.get(phase_name);
                            if (option_list == null) {
                                option_list = new ArrayList<>();
                                all_phase_options.put(phase_name, option_list);
                            }
                            option_list.add(component);
                            last_is_option = true;
                        }
                    }
                }
                if (phase_name != null && !phase_name.isEmpty() && !last_is_option) {
                    throw new OptionError("Missing phase option for " + phase_name);
                }
            }
            options.setPhaseOptions(all_phase_options);

            /* Parse configs that are specified as the command line arguments */
            String[] new_properties = cmd.getOptionValues(overrideProperties.getLongOpt());
            if (new_properties != null) {
                Properties prop = new Properties();
                for (String property : new_properties) {
                    String[] parts = property.split(":");
                    if (parts.length != 2) {
                        throw new OptionError("Invalid property format: " + property);
                    } else {
                        // put the key value pair into properties
                        prop.setProperty(parts[0], parts[1]);
                    }
                }
                options.setOverrideProperties(prop);
            }

            /* Setup other option */
            options.setListAnalysis(cmd.hasOption(listAnalysis.getLongOpt()));
            options.setKeepDebug(!cmd.hasOption(noDebugInfo.getLongOpt()));
            options.setIsHelp(cmd.hasOption(help.getLongOpt()));
            options.setIsSootHelp(cmd.hasOption(helpWithSootHelp.getLongOpt())); // soot help

            /* Extract the non-positional arguments */
            options.setArgs(cmd.getArgs());
            return options;
        } catch (ParseException e) {
            throw new OptionError("Failed to parse command line arguments: " + e.getMessage() + "\n");
        }
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(80, "analyzer.jar [OPTIONS] -- [SOOT OPTIONS]",
                null, mOptions, null, false);
        String example = "\nExample:\n"
                + "\tanalyzer.jar -a wjtp.modloc -i target/test-classes\n";
        System.out.println(example);
    }

    public void listAnalyses() {
        System.out.println("Available analyses:");
        Iterator<PhaseInfo> infoIterator = PhaseManager.getInstance().phaseInfoIterator();
        while (infoIterator.hasNext()) {
            PhaseInfo info = infoIterator.next();
            System.out.println("\t" + info.getFullName() + " - " + info.getHelp());
        }
    }

    /**
     * Exception in parsing command line options
     */
    public static class OptionError extends Exception {

        private static final long serialVersionUID = 4614793168350802013L;

        public OptionError(String message) {
            super(message);
        }
    }
}
