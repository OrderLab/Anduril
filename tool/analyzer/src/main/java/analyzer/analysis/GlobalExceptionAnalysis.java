package analyzer.analysis;

import soot.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

public final class GlobalExceptionAnalysis {
    public final Map<SootMethod, ExceptionHandlingAnalysis> analyses = new HashMap<>();
    public final Map<SootMethod, Set<SootMethod>> usage = new HashMap<>(); // method -> methods using this method
    public static final Set<SootMethod> emptySet = new HashSet<>();

    public GlobalExceptionAnalysis(final AnalysisInput analysisInput) {
        // prepare the methods
        for (final SootClass sootClass : analysisInput.classes) {
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                if (sootMethod.hasActiveBody()) {
                    final Body body = sootMethod.getActiveBody();
                    final UnitGraph graph = new BriefUnitGraph(body);
                    final ExceptionHandlingAnalysis analysis =
                            new ExceptionHandlingAnalysis(analysisInput, sootMethod, body, graph);
                    analyses.put(sootMethod, analysis);
                    for (final SootMethod method : analysis.methodOccurrences.keySet()) {
                        updateUsage(method, sootMethod);
                    }
                }
            }
        }
        doAnalysis();
//        for (final ExceptionHandlingAnalysis a : analyses.values()) {
//            a.ttt();
//        }
    }

    private void updateUsage(final SootMethod updateMethod, final SootMethod method) {
        final Set<SootMethod> set;
        if (usage.containsKey(updateMethod)) {
            set = usage.get(updateMethod);
        } else {
            set = new HashSet<>();
            usage.put(updateMethod, set);
        }
        set.add(method);
    }

    private void doAnalysis() {
        boolean shouldContinue = true;
        final Collection<ExceptionHandlingAnalysis> exceptionAnalyses = analyses.values();
        while (shouldContinue) {
            shouldContinue = false;
            final List<ExceptionHandlingAnalysis> active = new LinkedList<>();
            for (final ExceptionHandlingAnalysis exceptionAnalysis : exceptionAnalyses) {
//                if (exceptionAnalysis.method.getSubSignature().equals("void acceptConnections()")) {
//                    System.out.println(exceptionAnalysis.methodExceptions.keySet());
//                }
                if (exceptionAnalysis.update) {
                    exceptionAnalysis.update = false;
                    active.add(exceptionAnalysis);
                }
            }
            for (final ExceptionHandlingAnalysis exceptionAnalysis : active) {
                final SootMethod method = exceptionAnalysis.method;
                for (final SootMethod updateMethod : usage.getOrDefault(method, emptySet)) {
                    if (analyses.containsKey(updateMethod)) {
                        if (analyses.get(updateMethod).updateWith(method, exceptionAnalysis.methodExceptions.keySet())) {
                            shouldContinue = true;
                        }
                    }
                }
            }
        }
    }
}
