package analyzer.analysis;

import soot.*;
import soot.baf.InterfaceInvokeInst;
import soot.jimple.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

public class ThreadSchedulingAnalysis {
    public final List<SootClass> classes;
    public final Set<SootClass> callableClassSet;

    public final GlobalCallGraphAnalysis globalCallGraphAnalysis;

    public final Map<Unit,Set<SootMethod>> get2Call = new HashMap<>();

    private final String call = "java.lang.Object call()";

    ThreadSchedulingAnalysis(final List<SootClass> classes, final GlobalCallGraphAnalysis globalCallGraphAnalysis) {
        this.classes = classes;
        this.globalCallGraphAnalysis = globalCallGraphAnalysis;
        this.callableClassSet = new HashSet<>(this.classes);
        for (final SootClass sootClass : this.classes) {
            for (final SootMethod method : sootClass.getMethods()) {
                if (method.hasActiveBody()) {
                    final Body body = method.getActiveBody();
                    final UnitGraph graph = new BriefUnitGraph(body);
                    for (final Unit unit : body.getUnits()) {
                        // Callable Submit Future Get
                        if (unit instanceof DefinitionStmt) {
                            final Value lhs = ((DefinitionStmt) unit).getLeftOp();
                            if (lhs instanceof Local) {
                                final Value rhs = ((DefinitionStmt) unit).getRightOp();
                                if (rhs instanceof NewExpr) {
                                    final SootClass invocationClass = ((NewExpr) rhs).getBaseType().getSootClass();
                                    if (SubTypingAnalysis.v().isCallable(invocationClass)) {
                                        for (Unit futureGet : findFutureGet(unit, (Local)lhs, graph)) {
                                            get2Call.computeIfAbsent(futureGet, k -> new HashSet<>());
                                            get2Call.get(futureGet).add(invocationClass.getMethod(call));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private String submit_subsignature = "java.util.concurrent.Future submit(java.util.concurrent.Callable)";
    private String get_subsignature = "java.lang.Object get()";


    private Set<Unit> findFutureGet(final Unit newCallable, Local v, final UnitGraph graph) {

        final Set<Value> callables = new HashSet<>();
        callables.add(v);
        final Set<Unit> visited = new HashSet<>();
        visited.add(newCallable);
        final LinkedList<Unit> q = new LinkedList<>();
        q.add(newCallable);

        final Set<Value> submits = new HashSet<>();
        final Set<Unit> gets = new HashSet<>();
        // State Transition
        while (!q.isEmpty()) {
            final Unit node = q.pollFirst();
            // Find ExecutorService.submit() to get the returned Future first
            if (node instanceof DefinitionStmt) {
                if (((DefinitionStmt) node).getRightOp() instanceof InvokeExpr) {
                    // find the ExecutorService that submit this callable in exchange of a Future
                    final SootMethod calleeMethod = ((InvokeExpr) ((DefinitionStmt) node).getRightOp()).getMethod();
                    if (SubTypingAnalysis.v().isExecutorService(calleeMethod.getDeclaringClass())) {
                        if (calleeMethod.getSubSignature().equals(submit_subsignature)) {
                            // Add the Future locations
                            for (Value param : ((InvokeExpr) ((DefinitionStmt) node).getRightOp()).getArgs()) {
                                if (callables.contains(param)) {
                                    submits.add(((DefinitionStmt) node).getLeftOp());
                                }
                            }
                        }
                    }
                }


            } else if (submits.size() > 0 && node instanceof InvokeStmt) {
                SootMethod calleeMethod = ((InvokeStmt)node).getInvokeExpr().getMethod();
                if (SubTypingAnalysis.v().isFuture(calleeMethod.getDeclaringClass())) {
                    if (calleeMethod.getSubSignature().equals(get_subsignature)) {
                        // link the callable to the future.get()
                        gets.add(node);
                    }
                }
            }

            for (final Unit succ : graph.getSuccsOf(node)) {
                if (!visited.contains(succ)) {
                    visited.add(succ);
                    boolean kill = false;
                    for (final ValueBox valueBox : succ.getDefBoxes()) {
                        if (valueBox.getValue() == v) {
                            kill = true;
                            break;
                        }
                    }
                    if (!kill) {
                        q.add(succ);
                    }
                }
            }
        }
        return gets;
    }

}
