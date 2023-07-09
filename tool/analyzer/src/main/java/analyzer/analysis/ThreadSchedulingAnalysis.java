package analyzer.analysis;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

public class ThreadSchedulingAnalysis {
    public final List<SootClass> classes;
    public final Set<SootClass> callableClassSet;

    public final GlobalCallGraphAnalysis globalCallGraphAnalysis;

    public final Map<Unit,Set<SootMethod>> get2Call = new HashMap<>();

    // What about the case of multiple?
    public final Map<Unit,SootMethod> wrapper2Call = new HashMap<>();

    public final Map<SootMethod,Set<SootMethod>> handler2Call = new HashMap<>();

    private final String call = "java.lang.Object call()";
    private final String overriden_call = "void call()";

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
                                // stack = new Callable
                                if (rhs instanceof NewExpr) {
                                    final SootClass invocationClass = ((NewExpr) rhs).getBaseType().getSootClass();
                                    if (SubTypingAnalysis.v().isCallable(invocationClass)) {
                                        for (Unit futureGet : findFutureGet(unit, (Local)lhs, graph)) {
                                            get2Call.computeIfAbsent(futureGet, k -> new HashSet<>());
                                            for (SootMethod candidate : invocationClass.getMethods()) {
                                                if (candidate.getSubSignature().equals(call)
                                                        || candidate.getSubSignature().equals(overriden_call)) {
                                                    get2Call.get(futureGet).add(candidate);
                                                }
                                            }
                                        }
                                    }
                                } else if (rhs instanceof DynamicInvokeExpr) {
                                    SootMethod constructor = ((DynamicInvokeExpr) rhs).getMethod();
                                    Type returned = constructor.getReturnType();
                                    if (returned instanceof RefType) {
                                        // Lambda Expression converted to callable case
                                        if (SubTypingAnalysis.v().isCallable(((RefType) returned).getSootClass())) {
                                            // Locate the found lambda expression
                                            SootMethodRef dynamicCall = ((DynamicInvokeExpr) rhs).getBootstrapMethodRef();
                                            for (final Value arg : ((DynamicInvokeExpr) rhs).getBootstrapArgs()) {
                                                if (arg instanceof MethodHandle) {
                                                    SootMethod lambda = ((MethodHandle) arg).getMethodRef().resolve();
                                                    for (Unit futureGet : findFutureGet(unit, (Local)lhs, graph)) {
                                                        get2Call.computeIfAbsent(futureGet, k -> new HashSet<>());
                                                        get2Call.get(futureGet).add(lambda);
                                                    }

                                                }
                                            }
                                        } else if (SubTypingAnalysis.v().isThreadOrRunnable(((RefType) returned).getSootClass())) {
                                            // Lambda Expression converted to runnable case
                                            SootMethodRef dynamicCall = ((DynamicInvokeExpr) rhs).getBootstrapMethodRef();
                                            for (final Value arg : ((DynamicInvokeExpr) rhs).getBootstrapArgs()) {
                                                if (arg instanceof MethodHandle) {
                                                    SootMethod lambda = ((MethodHandle) arg).getMethodRef().resolve();
                                                    for (Unit ranLambda : findRunnableUnCaughtRun(unit, (Local)lhs, graph)) {
                                                        wrapper2Call.computeIfAbsent(ranLambda, k -> lambda);
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
            }
        }
        costumeHandlerCall();
    }

    private static final String[] handler_classes = {
            "org.apache.cassandra.service.CassandraDaemon$2",
    };

    private static final String[] handler_methods = {
            "void uncaughtException(java.lang.Thread, java.lang.Throwable)",
    };

    private static final String[] calling_classes = {
            "org.apache.cassandra.net.MessageDeliveryTask",
    };

    private static final String[] calling_methods = {
            "void run()",
    };

    private void costumeHandlerCall () {
        for (int i = 0; i < handler_classes.length; i++) {
            try {
                SootClass handlerClass = Scene.v().getSootClass(handler_classes[i]);
                SootMethod handlerMethod = handlerClass.getMethod(handler_methods[i]);
                SootClass callingClass = Scene.v().getSootClass(calling_classes[i]);
                SootMethod callingMethod = callingClass.getMethod(calling_methods[i]);
                handler2Call.computeIfAbsent(handlerMethod, k -> new HashSet<>());
                handler2Call.get(handlerMethod).add(callingMethod);
                System.out.println("Added Handler:"+handlerMethod+"--Caller:"+callingMethod );
            } catch (Exception ignored) {

            }
        }
    }


    private final String submit_subsignature = "java.util.concurrent.Future submit(java.util.concurrent.Callable)";
    public final String get_subsignature = "java.lang.Object get()";


    private static final String[] kafka = {
            "maybeMeasureLatency",
    };

    // Deal with runnable
    private Set<Unit> findRunnableUnCaughtRun(final Unit newRunnable, Local v, final UnitGraph graph) {
        final Set<Value> runnables = new HashSet<>();
        runnables.add(v);
        final Set<Unit> visited = new HashSet<>();
        visited.add(newRunnable);
        final LinkedList<Unit> q = new LinkedList<>();
        q.add(newRunnable);
        final Set<Unit> calledRun = new HashSet<>();

        while (!q.isEmpty()) {
            final Unit node = q.pollFirst();
            if (node instanceof DefinitionStmt) {
                if (runnables.contains(((DefinitionStmt) node).getRightOp())) {
                    // alias analysis
                    runnables.add(((DefinitionStmt) node).getLeftOp());
                }
            } else if (node instanceof InvokeStmt) {
                SootMethod calleeMethod = ((InvokeStmt)node).getInvokeExpr().getMethod();
                InvokeExpr ie = ((InvokeStmt) node).getInvokeExpr();
                for (Value param : ie.getArgs()) {
                    if (runnables.contains(param)) {
                        if (ie instanceof StaticInvokeExpr) {
                            for (String s : kafka) {
                                if (s.equals(calleeMethod.getName())) {
                                    calledRun.add(node);
                                }
                            }
                        }
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
        return calledRun;
    }


    // Deal with callable
    private Set<Unit> findFutureGet(final Unit newCallable, Local v, final UnitGraph graph) {

        final Set<Value> callables = new HashSet<>();
        callables.add(v);
        final Set<Unit> visited = new HashSet<>();
        visited.add(newCallable);
        final LinkedList<Unit> q = new LinkedList<>();
        q.add(newCallable);

        final Set<Value> submits = new HashSet<>();
        final Set<Unit> gets = new HashSet<>();

        while (!q.isEmpty()) {
            final Unit node = q.pollFirst();
            // Find ExecutorService.submit() to get the returned Future first
            if (node instanceof DefinitionStmt) {
                if (callables.contains(((DefinitionStmt) node).getRightOp())) {
                    // alias analysis

                    callables.add(((DefinitionStmt) node).getLeftOp());
                } else if (submits.contains(((DefinitionStmt) node).getRightOp())) {
                    // alias and array analysis
                    Value left = ((DefinitionStmt) node).getLeftOp();
                    if (left instanceof ArrayRef) {
                        submits.add(((ArrayRef) left).getBase());
                    } else {
                        submits.add(left);
                    }
                } else if (((DefinitionStmt) node).getRightOp() instanceof ArrayRef) {
                    if (submits.contains(((ArrayRef)((DefinitionStmt) node).getRightOp()).getBase())) {
                        Value left = ((DefinitionStmt) node).getLeftOp();
                        if (left instanceof ArrayRef) {
                            submits.add(((ArrayRef) left).getBase());
                        } else {
                            submits.add(left);
                        }
                    }
                } else if (((DefinitionStmt) node).getRightOp() instanceof CastExpr) {
                    // Cast case
                    if (submits.contains(((CastExpr) ((DefinitionStmt) node).getRightOp()).getOp())) {
                        Value left = ((DefinitionStmt) node).getLeftOp();
                        if (left instanceof ArrayRef) {
                            submits.add(((ArrayRef) left).getBase());
                        } else {
                            submits.add(left);
                        }
                    }
                } else if (((DefinitionStmt) node).getRightOp() instanceof InvokeExpr) {
                    // find the ExecutorService that submit this callable in exchange of a Future
                    final SootMethod calleeMethod = ((InvokeExpr) ((DefinitionStmt) node).getRightOp()).getMethod();
                    InvokeExpr ie = (InvokeExpr) ((DefinitionStmt) node).getRightOp();
                    if (SubTypingAnalysis.v().isExecutorService(calleeMethod.getDeclaringClass())) {
                        if (calleeMethod.getSubSignature().equals(submit_subsignature)) {
                            // Add the Future locations
                            for (Value param : ie.getArgs()) {
                                if (callables.contains(param)) {
                                    submits.add(((DefinitionStmt) node).getLeftOp());
                                }
                            }
                        }
                    } else if (submits.size() > 0 && SubTypingAnalysis.v().isFuture(calleeMethod.getDeclaringClass())) {
                        if (calleeMethod.getSubSignature().equals(get_subsignature)) {
                            // link the callable to the future.get()
                            if (submits.contains(((InterfaceInvokeExpr)ie).getBase())) {
                                gets.add(node);
                            }
                        }
                    }  else {
                        // Deal with future kicked out of a container
                        if (ie instanceof InterfaceInvokeExpr) {
                            Value container = ((InterfaceInvokeExpr) ie).getBase();
                            if (submits.contains(container)) {
                                //System.out.println("ArrayList output:" + ((DefinitionStmt) node).getLeftOp());
                                submits.add(((DefinitionStmt) node).getLeftOp());
                            }
                        }
                    }

                }
            } else if (node instanceof InvokeStmt) {
                SootMethod calleeMethod = ((InvokeStmt)node).getInvokeExpr().getMethod();
                InvokeExpr ie = ((InvokeStmt) node).getInvokeExpr();
                if (submits.size() > 0) {
                    if (SubTypingAnalysis.v().isFuture(calleeMethod.getDeclaringClass())) {
                        if (calleeMethod.getSubSignature().equals(get_subsignature)) {
                            // link the callable to the future.get()
                            if (submits.contains(((InterfaceInvokeExpr)ie).getBase())) {
                                gets.add(node);
                            }
                        }
                    }
                }
                // Deal with future stored into a container
                if (ie instanceof InterfaceInvokeExpr) {
                    for (Value param : ie.getArgs()) {
                        if (submits.contains(param)) {
                            //System.out.println("ArrayList input:" + ((InterfaceInvokeExpr) ie).getBase());
                            submits.add(((InterfaceInvokeExpr) ie).getBase());
                        }
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
