package analyzer.phase;

import analyzer.analysis.AnalysisInput;
import analyzer.analysis.AnalysisManager;
import analyzer.analysis.SubTypingAnalysis;
import analyzer.baseline.BaselineAnalyzer;
import analyzer.event.EventManager;
import analyzer.event.InvocationEvent;
import analyzer.event.LocationEvent;
import analyzer.event.ProgramEvent;
import analyzer.fate.FateAnalyzer;
import analyzer.instrument.ThreadInstrumentor;
import analyzer.option.AnalyzerOptions;
import analyzer.stacktrace.StackTraceAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.NewExpr;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Identify all the module entry points (thread and its run methods) in a system.
 *
 */
public class FlakyTestAnalyzer extends SceneTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(FlakyTestAnalyzer.class);

    public static final PhaseInfo PHASE_INFO = new PhaseInfo("wjtp", "flaky",
            "Extract module entry points in the subject software", true, false);

    public FlakyTestAnalyzer() {
    }

    public static final boolean baseline = Boolean.getBoolean("analysis.baseline");
    public static final boolean fate = Boolean.getBoolean("analysis.fate");
    public static final boolean crashtuner = Boolean.getBoolean("analysis.crashtuner");
    public static final boolean stackTrace = Boolean.getBoolean("analysis.stackTrace");

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        if (crashtuner) {
            BaselineAnalyzer.run(AnalyzerOptions.getInstance(), true);
            return;
        }
        if (baseline) {
            BaselineAnalyzer.run(AnalyzerOptions.getInstance(), false);
            return;
        }
        if (fate) {
            FateAnalyzer.run(AnalyzerOptions.getInstance());
            return;
        }
        if (stackTrace) {
            StackTraceAnalyzer.run(AnalyzerOptions.getInstance());
            return;
        }
        // TODO: make it configurable
        final AnalysisInput analysisInput = new AnalysisInput(AnalyzerOptions.getInstance(),
                Scene.v().getApplicationClasses());
        final AnalysisManager analysisManager = new AnalysisManager(analysisInput);
        System.out.printf("\nEvent Chaining Start Time: %s\n",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
        final EventManager eventManager = new EventManager(analysisManager);
        System.out.printf("\nEvent Chaining End Time: %s\n",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
        analysisManager.instrument();
        eventManager.dump("tree.json");
        eventManager.instrumentInjections();

//        targetClass = Scene.v().getSootClass(
//                "org.apache.zookeeper.server.ZooKeeperServerListenerImpl");
//        targetMethod = targetClass.getMethod("void notifyStopping(java.lang.String,int)");
//        analysisManager.reason2(new InvocationEvent(targetClass, targetMethod));
//        analysisManager.instrument();

//        for (final SootClass sc : Scene.v().getApplicationClasses()) {
//            if (SubTypingAnalysis.isThreadOrRunnable(sc)) {
//                try {
//                    final SootMethod runMethod = sc.getMethod("void run()");
//                    if (runMethod.hasActiveBody()) {
//                        new ThreadInstrumentor(sc, runMethod).instrument();
//                    }
//                } catch (final RuntimeException ignored) { }
//            }
//        }
//
//        System.out.println(analysisManager.start.locationUnit);
//        List<ProgramEvent> result = analysisManager.start.explainsWith(analysisManager);
//        for (final ProgramEvent e : result) {
//            System.out.println(((LocationEvent) e).locationUnit);
//        }
//        result = result.get(0).explainsWith(analysisManager);
//        for (final ProgramEvent e : result) {
//            System.out.println(((LocationEvent) e).locationUnit);
//        }
//
//        final SootMethod mainMethod = Scene.v().getMainClass().getMethodUnsafe("void main(java.lang.String[])");
//        final Body body = mainMethod.retrieveActiveBody();
//        final BasicBlockAnalysis basicBlockAnalysis = new BasicBlockAnalysis(body);
//        final PatchingChain<Unit> units = body.getUnits();
//        final UnitGraph graph = new ExceptionalUnitGraph(body);
//        final DominatorAnalysis dominatorAnalysis = new DominatorAnalysis(graph, units);
//        for (final Unit unit : units) {
//            final LinkedList<Unit> doms = dominatorAnalysis.getDominate(unit);
//            if (doms != null) {
//                System.out.println(unit);
//                System.out.println("----------");
//                for (final Unit dom : doms) {
//                    System.out.println(dom);
//                }
//                System.out.println("##########");
//            }
//        }
//        final TraceInstrumentor traceInstrumentor = new TraceInstrumentor(basicBlockAnalysis);
//        traceInstrumentor.instrument();
    }
}
