package analyzer.analysis;

import soot.*;
import soot.jimple.InvokeExpr;

import java.util.*;

public final class GlobalCallGraphAnalysis {
    public final Map<SootMethod, Set<SootMethod>> virtualCalls = new HashMap<>();
    public final Map<SootMethod, Set<SootMethod>> forwardCallMap = new HashMap<>();
    public final Map<SootMethod, Map<SootMethod, Set<Unit>>> backwardCallMap = new HashMap<>();
    public final AnalysisInput analysisInput;
    private void check(final String sig, final SootMethod method, final SootClass sc) {
        if (method.isStatic() || method.isPrivate() || !analysisInput.classSet.contains(sc)) {
            return;
        }
        try {
            final SootMethod overrideMethod = sc.getMethod(sig);
            virtualCalls.get(overrideMethod).add(method);
        } catch (final RuntimeException ignored) { }
        final SootClass superC = sc.getSuperclass();
        if (superC != null) {
            check(sig, method, superC);
        }
        for (final SootClass itf : sc.getInterfaces()) {
            check(sig, method, itf);
        }
    }
    private void addBackwardCall(final SootMethod caller, final SootMethod callee, final Unit unit) {
        Map<SootMethod, Set<Unit>> backwardEdges;
        if (backwardCallMap.containsKey(callee)) {
            backwardEdges = backwardCallMap.get(callee);
        } else {
            backwardEdges = new HashMap<>();
            backwardCallMap.put(callee, backwardEdges);
        }
        if (backwardEdges.containsKey(caller)) {
            backwardEdges.get(caller).add(unit);
        } else {
            backwardEdges.put(caller, new HashSet<>(Collections.singletonList(unit)));
        }
    }
    public GlobalCallGraphAnalysis(final AnalysisInput analysisInput) {
        this.analysisInput = analysisInput;
        for (final SootClass sootClass : analysisInput.classes) {
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                final Set<SootMethod> calls = new HashSet<>();
                virtualCalls.put(sootMethod, calls);
                if (sootMethod.hasActiveBody()) {
                    calls.add(sootMethod);
                }
            }
        }
        for (final SootClass sootClass : analysisInput.classes) {
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                if (sootMethod.hasActiveBody() && !sootMethod.isStatic() && !sootMethod.isPrivate() &&
                        !sootMethod.isConstructor()) {
                    check(sootMethod.getSubSignature(), sootMethod, sootClass.getSuperclass());
                    for (final SootClass itf : sootClass.getInterfaces()) {
                        check(sootMethod.getSubSignature(), sootMethod, itf);
                    }
                }
            }
        }
        for (final SootClass sootClass : analysisInput.classes) {
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                if (sootMethod.hasActiveBody()) {
                    backwardCallMap.put(sootMethod, new HashMap<>());
                }
            }
        }
        for (final SootClass sootClass : analysisInput.classes) {
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                if (sootMethod.hasActiveBody()) {
                    final Body body = sootMethod.getActiveBody();
                    final Set<SootMethod> edges = new HashSet<>();
                    forwardCallMap.put(sootMethod, edges);
                    for (final Unit unit : body.getUnits()) {
                        for (final ValueBox valueBox : unit.getUseBoxes()) {
                            final Value value = valueBox.getValue();
                            if (value instanceof InvokeExpr) {
                                final SootMethod method = ((InvokeExpr) value).getMethod();
                                final SootClass sc = method.getDeclaringClass();
                                if (analysisInput.classSet.contains(sc)) {
                                    if (method.isStatic() || method.isConstructor() || method.isPrivate()) {
                                        addBackwardCall(sootMethod, method, unit);
                                    } else {
                                        for (final SootMethod callee : virtualCalls.get(method)) {
                                            addBackwardCall(sootMethod, callee, unit);
                                        }
                                    }
                                    if (SubTypingAnalysis.v().isThreadOrRunnable(sc) && method.isConstructor()) {
                                        // what about going to superclass?
                                        try {
                                            final SootMethod runMethod = sc.getMethod("void run()");
                                            final Set<SootMethod> calls = virtualCalls.get(runMethod);
//                                            if (sc.getName().equals(
//                                                    "org.apache.zookeeper.server.quorum.Leader$LearnerCnxAcceptor$LearnerCnxAcceptorHandler")) {
//                                                System.err.println(sootClass.getName() + " ## " + sootMethod.getSubSignature());
//                                            }
                                            if (calls != null) {
                                                edges.addAll(calls);
                                            }
                                        } catch (final RuntimeException ignored) { }
                                    }
                                    final Set<SootMethod> calls = virtualCalls.get(method);
                                    if (calls != null) {
                                        edges.addAll(calls);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public Set<SootMethod> computeReachable(final SootMethod start) {
        final Set<SootMethod> visited = new HashSet<>();
        visited.add(start);
        final LinkedList<SootMethod> q = new LinkedList<>();
        q.add(start);
        while (!q.isEmpty()) {
            final SootMethod node = q.pollFirst();
            for (final SootMethod method : forwardCallMap.get(node)) {
                if (!visited.contains(method)) {
                    visited.add(method);
                    q.addLast(method);
                }
            }
        }
        return visited;
    }
}
