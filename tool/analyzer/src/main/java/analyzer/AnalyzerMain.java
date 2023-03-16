package analyzer;

import analyzer.option.AnalyzerOptions;
import analyzer.option.OptionParser;
import analyzer.option.OptionParser.OptionError;
import analyzer.phase.FlakyTestAnalyzer;
import analyzer.phase.PhaseInfo;
import analyzer.phase.PhaseManager;
import analyzer.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.G;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.Timers;
import soot.Transform;
import soot.options.Options;

public class AnalyzerMain {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyzerMain.class);
    
    // Arguments passed through the command line
    static public AnalyzerOptions options;
    private boolean initialized;

    static public Date analyzeFinishTime=null;

    public AnalyzerMain(AnalyzerOptions options) {
        this.options = options;
        initialized = false;
    }

    /**
     * Invoke Soot with our customized options and additional arguments.
     */
    protected boolean run() {
        if (!initialized) {
            System.err.println("Analyzer is not initialized");
            return false;
        }
        if (!Options.v().parse(options.getArgs())) {
            System.err.println("Error in parsing Soot options");
            return false;
        }
        Options.v().warnNonexistentPhase();
        if (Options.v().phase_list()) {
            System.out.println(Options.v().getPhaseList());
            return true;
        }
        if (!Options.v().phase_help().isEmpty()) {
            for (String phase : Options.v().phase_help()) {
                System.out.println(Options.v().getPhaseHelp(phase));
            }
            return true;
        }
        if (PhaseManager.getInstance().enabledAnalyses().size() == 0) {
            System.err.println("No analysis is specified.");
            System.err.println("Run with --list to see the list of analyses available");
            return false;
        }
        if(Options.v().on_the_fly()) {
            Options.v().set_whole_program(true);
            PhaseOptions.v().setPhaseOption("cg", "off");
        }
        // Invoke Soot's pack manager to run the packs
        try {
            Date start = new Date();
            //LOG.info("Analyzer started on " + start);
            Timers.v().totalTimer.start();
            Scene.v().loadNecessaryClasses();
            PackManager.v().runPacks();
            if (!Options.v().oaat()) {
                PackManager.v().writeOutput();
            }
            Timers.v().totalTimer.end();
            // Print out time stats.
            if (Options.v().time())
                Timers.v().printProfilingInformation();
            Date finish = new Date();
            //LOG.info("Analyzer finished on " + finish);
            long runtime = finish.getTime() - start.getTime();

            //analyzeFinishTime could be null if running countMethod scripts
            //TODO: refine the logic of setting analyzeFinishTime
            if(analyzeFinishTime!=null) {
                long analysisTime = analyzeFinishTime.getTime() - start.getTime();
                long generationTime = finish.getTime() - analyzeFinishTime.getTime();
                //LOG.info("Analysis Time: " + (analysisTime / 60000) + " min. "
                //        + ((analysisTime % 60000) / 1000) + " sec. " + (analysisTime % 1000)
                //        + " ms.");
                //LOG.info("Generation Time: " + (generationTime / 60000) + " min. "
                //        + ((generationTime % 60000) / 1000) + " sec. " + (generationTime % 1000)
                //        + " ms.");
            }
            //LOG.info("Analyzer has run for " + (runtime / 60000) + " min. "
            //        + ((runtime % 60000) / 1000) + " sec. " + (runtime % 1000) + " ms.");
        } catch (StackOverflowError e ) {
            LOG.error( "Analyzer has run out of stack memory." );
            throw e;
        } catch (OutOfMemoryError e) {
            LOG.error( "Soot has run out of the memory allocated to it by the Java VM." );
            throw e;
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return true;
    }

    /**
     * Register the analyses to run with Soot pack manager
     */
    @VisibleForTesting
    protected void registerAnalyses() {
        PhaseManager.getInstance().registerAnalysis(new FlakyTestAnalyzer(),
                FlakyTestAnalyzer.PHASE_INFO);
    }

    /**
     * Load basic classes to Soot
     */
    private void loadClasses() {
        // add input classes
        String[] classes = options.getClasses();
        if (classes != null) {
            for (String cls : classes) {
                Options.v().classes().add(cls); // all to Soot class to be loaded
            }
        }

        // add basic classes
        Class<?>[] basicClasses = {java.io.PrintStream.class, java.lang.System.class, java.lang.Thread.class};
        for (Class<?> cls : basicClasses) {
            //LOG.debug("Adding basic class " + cls.getCanonicalName() + " to Soot");
            Scene.v().addBasicClass(cls.getCanonicalName(), SootClass.SIGNATURES);
            for (Class<?> innerCls : cls.getDeclaredClasses()) {
                // Must use getName instead of getCanonicalName for inner class
                //LOG.debug("- inner class " + innerCls.getName() + " added");
                Scene.v().addBasicClass(innerCls.getName(), SootClass.SIGNATURES);
            }
        }
    }

    /**
     * Prepare environment to run auto-watchdog: set options, register analyses,
     * load classes, initialize Soot, etc.
     *
     * @return true if the initialization is successful; false otherwise
     */
    public boolean initialize() {
        //G.reset(); // reset Soot
        registerAnalyses(); // register analyses with Soot

        /* Setup Soot options */
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(options.getClassPath());
        if (options.noOutput()) {
            Options.v().set_output_format(Options.output_format_none);
        } else {
            if (!options.genExecutable() && !options.isOutputJar()) {
                // If the output format is not a jar or .class files,
                // we output Jimple by default
                Options.v().set_output_format(Options.output_format_J);
            }
            Options.v().set_output_jar(options.isOutputJar());
        }
        // Well, the truth is even if we specify output format as none
        // AutoWatchdog still relies on the output dir option to decide
        // where to write its intermediate results... :|
        if (options.getOutputDir() != null) {
            Options.v().set_output_dir(options.getOutputDir());
        }
        Options.v().set_keep_line_number(options.keepDebug());
        Options.v().set_main_class(options.getMainClass());
        Options.v().set_whole_program(options.isWholeProgram());
        if (!options.isInputListEmpty()) {
            Options.v().set_process_dir(options.getInputList());
        }
        String[] analyses = options.getAnalyses();
        if (analyses != null) {
            boolean need_call_graph = false;
            for (String analysis : analyses) {
                // Enable the analysis in the manager
                PhaseManager.getInstance().enableAnalysis(analysis);
                PhaseInfo phaseInfo = PhaseManager.getInstance().getPhaseInfo(analysis);
                // if any phase needs call graph, we should enable it
                if (phaseInfo.needCallGraph())
                    need_call_graph = true;
            }
            if (options.isWholeProgram() && !need_call_graph) {
                // if it is whole program analysis and we don't need call graph analysis
                // we should explicitly disable it
                PhaseOptions.v().setPhaseOption("cg", "off");
            }
        }

        //We enable spark to get on-the-fly callgraph
        Options.v().setPhaseOption("cg.spark","enabled:true");
        //We enable context-sensitive points-to analysis to better achieve
        Options.v().setPhaseOption("cg.spark","cs-demand:true");
        Options.v().setPhaseOption("cg.spark","apponly:true");
        //Options.v().setPhaseOption("cg.spark","geom-pta:true");
        //Options.v().setPhaseOption("cg.paddle","enabled:true");

        Map<String, List<String>> all_phase_options = options.getPhaseOptions();
        Set<String> analysis_with_options = new HashSet<>();
        if (all_phase_options != null) {
            for (Map.Entry<String, List<String>> entry : all_phase_options.entrySet()) {
                String phase = entry.getKey();
                String option_str = StringUtils.join(",", entry.getValue());
                // If the the option from command line is for an AutoWatchdog analysis
                // We must both enable it and add the custom option str
                if (PhaseManager.getInstance().isAnalysiEnabled(phase)) {
                    analysis_with_options.add(phase);
                    option_str = "enabled:true," + option_str;
                }
                // Otherwise, the option from command line is for a standard Soot phase
                // e.g., -p jb use-original-names:true, we will just pass it along
                // to Soot
                PhaseOptions.v().setPhaseOption(phase, option_str);
            }
        }
        if (analyses != null) {
            for (String analysis : analyses) {
                // For any specified analysis that does not have an option from command line
                // We must at least enable it in Soot
                if (!analysis_with_options.contains(analysis)) {
                    Options.v().setPhaseOption(analysis, "on");
                }
            }
        }
        String[] args = options.getArgs();
        if (args == null) {
            args = new String[]{};
        }
        if (args.length == 0) {
            Options.v().set_unfriendly_mode(true); // allow no arguments to be specified for Soot
        }

        // load classes
        loadClasses();

        initialized = true;
        //LOG.info("Analyzer initialization finished");
        return true;
    }

    public static void main(String[] args) {
        System.out.printf("\nStatic Analyzer Start Time: %s\n",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
        addScalaDependencies();
        addJunitDependencies();
        addextraDependencies();
        OptionParser parser = new OptionParser();
        AnalyzerOptions options = null;
        try {
            options = parser.parse(args);
        } catch (OptionError optionError) {
            System.err.println("Error in parsing options: " + optionError.getMessage() + "\n");
            parser.printHelp();
            System.exit(1);
        }
        if (options.isSootHelp()) {
            parser.printHelp();
            System.out.println("\n*********************************************");
            System.out.println("Soot OPTIONS:\n");
            System.out.println(Options.v().getUsage());
            System.exit(0);
        } else if (options.isHelp()) {
            parser.printHelp();
            System.exit(0);
        }
        if (options.listAnalysis()) {
            parser.listAnalyses();
            System.exit(0);
        }
        if (options.isInputListEmpty() && options.getClasses() == null) {
            System.err.println("Must set either a jar file/input directory or a list of classes as input.");
            parser.printHelp();
            System.exit(1);
        }
        //LOG.debug("Parsed options: " + options);
        // Create AutoWatchdog now with the parsed options
        AnalyzerMain main = new AnalyzerMain(options);
        if (!main.initialize() || !main.run()) {
            System.exit(1);
        }
        System.out.printf("\nStatic Analyzer End Time: %s\n",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
    }
    // in alphabetical order
    private static final String[] scalaDependencies = new String[]{
            "scala.runtime.java8.JFunction0$mcB$sp",
            "scala.runtime.java8.JFunction0$mcD$sp",
            "scala.runtime.java8.JFunction1$mcDD$sp",
            "scala.runtime.java8.JFunction1$mcDJ$sp",
            "scala.runtime.java8.JFunction0$mcI$sp",
            "scala.runtime.java8.JFunction0$mcJ$sp",
            "scala.runtime.java8.JFunction0$mcV$sp",
            "scala.runtime.java8.JFunction0$mcZ$sp",
            "scala.runtime.java8.JFunction1$mcID$sp",
            "scala.runtime.java8.JFunction1$mcII$sp",
            "scala.runtime.java8.JFunction1$mcJI$sp",
            "scala.runtime.java8.JFunction1$mcJJ$sp",
            "scala.runtime.java8.JFunction2$mcJJJ$sp",
            "scala.runtime.java8.JFunction0$mcS$sp",
            "scala.runtime.java8.JFunction1$mcVD$sp",
            "scala.runtime.java8.JFunction1$mcVI$sp",
            "scala.runtime.java8.JFunction2$mcVII$sp",
            "scala.runtime.java8.JFunction1$mcVJ$sp",
            "scala.runtime.java8.JFunction2$mcVJI$sp",
            "scala.runtime.java8.JFunction1$mcZD$sp",
            "scala.runtime.java8.JFunction1$mcZI$sp",
            "scala.runtime.java8.JFunction1$mcZJ$sp",
            "scala.runtime.java8.JFunction2$mcIII$sp",
            "scala.runtime.java8.JFunction2$mcZII$sp",
    };

    private static void addScalaDependencies() {
        for (final String d : scalaDependencies) {
            Scene.v().addBasicClass(d, SootClass.HIERARCHY);
        }
    }

    private static final String[] junitDependencies = new String[] {
            "org.junit.jupiter.api.extension.AfterTestExecutionCallback",
            "org.junit.jupiter.api.extension.BeforeTestExecutionCallback",  

    };

    private static void addJunitDependencies() {
        for (final String d : junitDependencies) {
            Scene.v().addBasicClass(d, SootClass.HIERARCHY);
        }    
    }

    private static final String[] extraDependencies = new String[] {
            "io.netty.channel.ChannelFutureListener",
    };

    private static void addextraDependencies() {
        for (final String d : extraDependencies) {
            Scene.v().addBasicClass(d, SootClass.HIERARCHY);
        }    
    }

}
