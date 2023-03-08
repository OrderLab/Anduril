package analyzer.analysis;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalReturnAnalysis {
    public final Map<SootMethod, ExceptionReturnAnalysis> analyses = new HashMap<>();

    public GlobalReturnAnalysis(final List<SootClass> classes,
                                   final GlobalCallGraphAnalysis globalCallGraphAnalysis) {
        for (final SootClass sootClass : classes) {
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                if (sootMethod.hasActiveBody()) {
                    if (ExceptionReturnAnalysis.isWrapper(sootMethod)) {
                        final Body body = sootMethod.getActiveBody();
                        final UnitGraph graph = new BriefUnitGraph(body);
                        // Intra analysis
                        analyses.put(sootMethod, new ExceptionReturnAnalysis(sootMethod, body, graph, globalCallGraphAnalysis));

                    }
                }
            }
        }
        // Global inter analysis
        // 1. Start "Transparent" analysis + 1.5 With complete "transparent", calculate New exceptions again locally(last run)
        boolean stop1 = false;
        while (!stop1) {
            stop1 = true;
            for (ExceptionReturnAnalysis methodReturnAnalysis : analyses.values()) {
                if (methodReturnAnalysis.transparentPropagateAnalysis(analyses)) {
                    stop1 = false;
                }
            }
        }
        // 2. Integrate New Exception Info
        boolean stop2 = false;
        while(!stop2) {
            stop2 = true;
            for (ExceptionReturnAnalysis methodReturnAnalysis : analyses.values()) {
                if (methodReturnAnalysis.newExceptionPropagateAnalysis(analyses)) {
                    stop2 = false;
                }
            }
        }
    }
}
