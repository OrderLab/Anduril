package analyzer.analysis;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

public final class GlobalSlicingAnalysis {
    public static final class Location {
        public final SootMethod method;
        public final Unit unit;

        public Location(final SootMethod method, final Unit unit) {
            this.method = method;
            this.unit = unit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Location location = (Location) o;
            return Objects.equals(method, location.method) && Objects.equals(unit, location.unit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method, unit);
        }
    }

    public final Map<SootField, Set<Location>> dataWrite = new HashMap<>();
    public final Map<SootMethod, Set<Value>> retValues = new HashMap<>();
    public final Map<SootMethod, Set<Unit>> retTrue = new HashMap<>();
    public final Map<SootMethod, Set<Unit>> retFalse = new HashMap<>();
    public GlobalCallGraphAnalysis globalCallGraphAnalysis;
    public GlobalIntraProceduralAnalysis globalIntraProceduralAnalysis;

    public GlobalSlicingAnalysis(List<SootClass> classes,
                                 final GlobalCallGraphAnalysis globalCallGraphAnalysis,
                                 final GlobalIntraProceduralAnalysis globalIntraProceduralAnalysis) {
        this.globalCallGraphAnalysis = globalCallGraphAnalysis;
        this.globalIntraProceduralAnalysis = globalIntraProceduralAnalysis;
        for (final SootClass sootClass : classes) {
            for (final SootField f : sootClass.getFields()) {
                dataWrite.put(f, new HashSet<>());
            }
            for (final SootMethod m : sootClass.getMethods()) {
                //retValues.put(m, new HashSet<>());
                retTrue.put(m, new HashSet<>());
                retFalse.put(m, new HashSet<>());
            }
        }
        // TODO: parameter ref
        for (final SootClass sootClass : classes) {
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                // Return type is boolean and no parameters
                boolean flag = isCheck(sootMethod);
                if (sootMethod.hasActiveBody()) {
                    final Body body = sootMethod.getActiveBody();
                    final UnitGraph graph = new BriefUnitGraph(body);
                    for (final Unit unit : body.getUnits()) {
                        if (flag) {
                            if (unit instanceof ReturnStmt) {
                                final Value value = ((ReturnStmt) unit).getOp();
                                if (value instanceof IntConstant) {
                                    if (((IntConstant) value).value != 0) {
                                        retTrue.get(sootMethod).add(unit);
                                    } else {
                                        retFalse.get(sootMethod).add(unit);
                                    }
                                }
                            } else if (unit instanceof DefinitionStmt) {
                                final Value x = ((DefinitionStmt) unit).getLeftOp();
                                final Value y = ((DefinitionStmt) unit).getRightOp();
                                if (y instanceof IntConstant && searchReturn(unit, x, graph)) {
                                    if (((IntConstant)y).value != 0) {
                                        retTrue.get(sootMethod).add(unit);
                                    } else {
                                        retFalse.get(sootMethod).add(unit);
                                    }
                                }
                            }
                        }
//                        if (unit instanceof ReturnStmt) {
//                            final Value value = ((ReturnStmt) unit).getOp();
//                            if (value instanceof FieldRef) {
//                                retValues.get(sootMethod).add(value);
//                            }
//                            if (value instanceof InvokeExpr) {
//                                retValues.get(sootMethod).add(value);
//                            }
//                            if (value instanceof Local) {
//                                final Value origin = search(value, sootClass, sootMethod, unit);
//                                if (origin instanceof FieldRef) {
//                                    retValues.get(sootMethod).add(origin);
//                                }
//                                if (origin instanceof InvokeExpr) {
//                                    retValues.get(sootMethod).add(origin);
//                                }
//                            }
//                        }
                        for (final ValueBox valueBox : unit.getDefBoxes()) {
                            final Value value = valueBox.getValue();
                            if (value instanceof FieldRef) {
                                try {
                                    dataWrite.get(((FieldRef) value).getField()).add(new Location(sootMethod, unit));
                                } catch (NullPointerException e) {
                                    // ===
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean isCheck(SootMethod sootMethod) {
        if  (!sootMethod.hasActiveBody()) {
            return false;
        }
        Type returned = sootMethod.getReturnType();

        return (returned instanceof BooleanType) && (sootMethod.getParameterCount() == 0);
    }

    private boolean searchReturn(final Unit start, final Value v, UnitGraph graph) {
        final Set<Unit> visited = new HashSet<>();
        visited.add(start);
        final LinkedList<Unit> q = new LinkedList<>();
        q.add(start);
        while (!q.isEmpty()) {
            final Unit node = q.pollFirst();
            if (node instanceof ReturnStmt) {
                if (v == ((ReturnStmt) node).getOp()) {
                    return true;
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
        return false;
    }

    private Value search(final Value value, final SootClass sootClass, final SootMethod sootMethod, final Unit u) {
        final IntraProceduralAnalysis analysis = this.globalIntraProceduralAnalysis.getAnalysis(sootClass, sootMethod);
        final PatchingChain<Unit> units = sootMethod.getActiveBody().getUnits();
        for (Unit unit = analysis.basicBlockAnalysis.heads.get(u); unit != u; unit = units.getSuccOf(unit)) {
            if (unit instanceof DefinitionStmt) {
                final Value x = ((DefinitionStmt) unit).getLeftOp();
                final Value y = ((DefinitionStmt) unit).getRightOp();
                if (x == value) {
                    return y;
                }
            }
        }
        return value;
    }
}
