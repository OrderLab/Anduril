package analyzer.analysis;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

public class ExceptionReturnAnalysis {
    public final SootMethod method;
    public final Body body;
    public final UnitGraph graph;
    public final PatchingChain<Unit> units;

    public final Set<SootClass> newExceptions = new HashSet<>(); // Contain the possible returned new New exceptions
    public final Map<SootMethod, Set<Unit>> wrapperCalls = new HashMap<>(); // The other wrapper called by this wrapper
    public final Map<Unit, List<Unit>> passLocations = new HashMap<>(); // internal wrapper from new exceptions/parameter
    public boolean transparent = false; // means that whatever exception type is passed in is possible to be returned

    public final GlobalCallGraphAnalysis globalCallGraphAnalysis;

    public ExceptionReturnAnalysis(final SootMethod method, final Body body,
                                   final UnitGraph graph, final GlobalCallGraphAnalysis globalCallGraphAnalysis) {
        this.method = method;
        this.body = body;
        this.graph = graph;
        this.units = body.getUnits();
        this.globalCallGraphAnalysis = globalCallGraphAnalysis;
        // Intra analysis
        for (final Unit unit : units) {
            if (unit instanceof DefinitionStmt) {
                final Value lhs = ((DefinitionStmt) unit).getLeftOp();
                if (lhs instanceof Local) {
                    final Value rhs = ((DefinitionStmt) unit).getRightOp();
                    if (rhs instanceof NewExpr) {
                        // New constructor
                        final SootClass invocationClass = ((NewExpr) rhs).getBaseType().getSootClass();
                        if (SubTypingAnalysis.v().isThrowable(invocationClass)) {
                            if (searchReturnLocationAndPropagate(unit, (Local) lhs)) {
                                newExceptions.add(invocationClass);
                            }
                        }
                    } else if (rhs instanceof InvokeExpr) {
                        // Another wrapper
                        final SootMethod calleeMethod = ((InvokeExpr) rhs).getMethod();
                        if (isWrapper(calleeMethod)) {
                            if (searchReturnLocationAndPropagate(unit, (Local) lhs)) {
                                Set<Unit> set;
                                if (!wrapperCalls.containsKey(calleeMethod)) {
                                    set = new HashSet<>();
                                    wrapperCalls.put(calleeMethod, set);
                                }
                                set = wrapperCalls.get(calleeMethod);
                                set.add(unit);
                            }
                        }
                    } else if (rhs instanceof ParameterRef) {
                        // Parameter Exception
                        if ((rhs.getType()) instanceof  RefType) {
                            final SootClass invocationClass = ((RefType) rhs.getType()).getSootClass();
                            if (SubTypingAnalysis.v().isThrowable(invocationClass)) {
                                if (searchReturnLocationAndPropagate(unit, (Local) lhs)) {
                                    this.transparent = true;
                                }
                            }
                        }
                    }
                }
            }
        }



    }

    public static boolean isWrapper(SootMethod sootMethod) {
        Type returned = sootMethod.getReturnType();
        // Take care of  ArrayType of RefType
        if (returned instanceof RefType) {
            if (SubTypingAnalysis.v().isThrowable(((RefType) returned).getSootClass())) {
                boolean isWrap = false;
                // Check for args
                for (final Type arg:sootMethod.getParameterTypes()) {
                    if (arg instanceof RefType) {
                        if (SubTypingAnalysis.v().isThrowable(((RefType) arg).getSootClass())) {
                            isWrap = true;
                        }
                    }
                }
                if (isWrap) {
                    // Found one wrapper
                    return true;
                }
            }
        }
        return false;
    }



    private boolean searchReturnLocationAndPropagate(final Unit unit, final Local v) {
        final Set<Value> vs = new HashSet<>();
        vs.add(v);
        final Set<Unit> visited = new HashSet<>();
        visited.add(unit);
        final LinkedList<Unit> q = new LinkedList<>();
        q.add(unit);
        boolean toBeReturn = false;
        while (!q.isEmpty()) {
            final Unit node = q.pollFirst();
            if (node instanceof DefinitionStmt) {
                //System.out.println(node);
                //System.out.println(((DefinitionStmt) node).getRightOp());
                if (vs.contains(((DefinitionStmt) node).getRightOp())) {
                    // alias analysis

                    vs.add(((DefinitionStmt) node).getLeftOp());
                } else if (((DefinitionStmt) node).getRightOp() instanceof CastExpr) {
                    // Cast case
                    if (vs.contains(((CastExpr) ((DefinitionStmt) node).getRightOp()).getOp())) {
                        vs.add(((DefinitionStmt) node).getLeftOp());
                    }
                } else if (((DefinitionStmt) node).getRightOp() instanceof InvokeExpr) {
                    // find the wrapper call that take this variable as a argument
                    final SootMethod calleeMethod = ((InvokeExpr) ((DefinitionStmt) node).getRightOp()).getMethod();
                    if (isWrapper(calleeMethod)) {
                        for (Value param:((InvokeExpr) ((DefinitionStmt) node).getRightOp()).getArgs()) {
                            //System.out.println(param);
                            //System.out.println(vs);
                            if (vs.contains(param)) {
                                final List<Unit> locations;
                                if (!passLocations.containsKey(node)) {
                                    locations = new LinkedList<>();
                                    passLocations.put(node,locations);
                                } else {
                                    locations = passLocations.get(node);
                                }
                                locations.add(unit);
                            }
                        }
                    }
                }
            }
            if (node instanceof ReturnStmt) {
                if (vs.contains(((ReturnStmt) node).getOp())) {
                    toBeReturn = true;
                }
                continue;
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
        return toBeReturn;
    }

    public boolean newExceptionPropagateAnalysis(Map<SootMethod, ExceptionReturnAnalysis> analyses) {
        boolean flag = false;
        for (SootMethod callee: this.wrapperCalls.keySet()) {
            for (SootMethod realCallee: globalCallGraphAnalysis.virtualCalls.get(callee)) {
                if (analyses.containsKey(realCallee)) {
                    if (!this.newExceptions.containsAll(analyses.get(realCallee).newExceptions)) {
                        this.newExceptions.addAll(analyses.get(realCallee).newExceptions);
                        flag = true;
                    }
                }
            }
        }
        return flag;
    }


    public boolean transparentPropagateAnalysis(Map<SootMethod, ExceptionReturnAnalysis> analyses) {

        boolean flag = false;
        for (SootMethod targetMethod: wrapperCalls.keySet()) {
            for (Unit targetUnit: wrapperCalls.get(targetMethod)) {
                if (searchParam(targetUnit, analyses)) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    // Breadth-first search of a wrapper call site to Exception Parameter
    private boolean searchParam(Unit start, Map<SootMethod, ExceptionReturnAnalysis> analyses) {
        // One complete tour is made sure with no early return

        final LinkedList<Unit> q = new LinkedList<>();
        Value rhs = ((DefinitionStmt) start).getRightOp();
        SootMethod calleeMethod = ((InvokeExpr) rhs).getMethod();
        if (this.globalCallGraphAnalysis.virtualCalls.containsKey(calleeMethod)) {
            for (SootMethod realCallee : this.globalCallGraphAnalysis.virtualCalls.get(calleeMethod)) {
                if (analyses.containsKey(realCallee) && analyses.get(realCallee).transparent) {
                    q.add(start);
                }
            }
        }

        final Set<Unit> vs = new HashSet<>();
        vs.add(start);

        boolean flag = false;
        while (!q.isEmpty()) {
            Unit node = q.pollFirst();
            if (passLocations.containsKey(node)) {
                for (Unit next: passLocations.get(node)) {
                    if (!vs.contains(next)) {
                        rhs = ((DefinitionStmt) next).getRightOp();
                        if (rhs instanceof ParameterRef) {
                            // toggle?
                            if (!this.transparent) {
                                this.transparent = true;
                                flag = true;
                            }
                        } else if (rhs instanceof NewExpr) {
                            final SootClass newException = ((NewExpr) rhs).getBaseType().getSootClass();
                            this.newExceptions.add(newException);
                        }else if (rhs instanceof InvokeExpr) {
                            calleeMethod = ((InvokeExpr) rhs).getMethod();
                            for (SootMethod realCallee: this.globalCallGraphAnalysis.virtualCalls.get(calleeMethod)) {
                                if (analyses.containsKey(realCallee) && analyses.get(realCallee).transparent) {
                                    vs.add(next);
                                    q.add(next);
                                }
                            }

                        }
                    }
                }
            }
        }
        return flag;
    }
}
