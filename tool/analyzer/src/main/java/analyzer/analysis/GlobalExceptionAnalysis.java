package analyzer.analysis;

import fj.test.Bool;
import soot.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import javax.lang.model.type.PrimitiveType;
import java.util.*;

public final class GlobalExceptionAnalysis {
    public final Map<SootMethod, ExceptionHandlingAnalysis> analyses = new HashMap<>();
    public final Map<SootMethod, Set<SootMethod>> usage = new HashMap<>(); // method -> methods using this method
    public static final Set<SootMethod> emptySet = new HashSet<>();

    public GlobalExceptionAnalysis(final List<SootClass> classes,
                                   final GlobalCallGraphAnalysis globalCallGraphAnalysis) {
        // prepare the methods
        for (final SootClass sootClass : classes) {
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                if (sootMethod.hasActiveBody()) {
                    final Body body = sootMethod.getActiveBody();
                    final UnitGraph graph = new BriefUnitGraph(body);
                    final ExceptionHandlingAnalysis analysis =
                            new ExceptionHandlingAnalysis(classes, sootMethod, body, graph, globalCallGraphAnalysis);
                    analyses.put(sootMethod, analysis);
                    for (final SootMethod method : analysis.methodOccurrences.keySet()) {
//                        if (method.getName().equals("commit") &&
//                                method.getDeclaringClass().getName().equals("org.apache.zookeeper.server.persistence.FileTxnLog")) {
//                            System.out.println(sootMethod.getName() + " calls " + method.getName());
//                        }
                        updateUsage(method, sootMethod);
                    }
                }
            }
        }
//        for (final SootMethod method : usage.keySet()) {
//            if (method.getName().equals("commit") &&
//                    method.getDeclaringClass().getName().equals("org.apache.zookeeper.server.persistence.FileTxnLog")) {
//                for (final SootMethod m : usage.get(method)) {
//                    System.out.println(m.getName() + " calls " + method.getName());
//                }
//            }
//        }
        doAnalysis();
//        for (final ExceptionHandlingAnalysis a : analyses.values()) {
//            if (a.method.getName().equals("commit") &&
//                    a.method.getDeclaringClass().getName().equals("org.apache.zookeeper.server.persistence.FileTxnSnapLog")) {
//                System.out.println("asdf");
//                for (final SootClass c : a.methodExceptions.keySet()) {
//                    System.out.println(c.getName());
//                    for (final Unit unit : a.methodExceptions.get(c)) {
//                        System.out.println(unit);
//                    }
//                }
//            }
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
//                    if (exceptionAnalysis.method.getName().equals("commit") &&
//                            exceptionAnalysis.method.getDeclaringClass().getName().equals("org.apache.zookeeper.server.persistence.FileTxnLog")) {
//                        System.out.println(exceptionAnalysis.method.getName() + " encounter");
//                        for (final SootClass c : exceptionAnalysis.methodExceptions.keySet()) {
//                            System.out.println(c.getName());
//                            for (final Unit unit : exceptionAnalysis.methodExceptions.get(c)) {
//                                System.out.println(unit);
//                            }
//                        }
//                    }
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
