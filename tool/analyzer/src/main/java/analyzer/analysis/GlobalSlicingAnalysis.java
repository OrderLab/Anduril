package analyzer.analysis;

import soot.*;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;

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
    public GlobalCallGraphAnalysis globalCallGraphAnalysis;
    public AnalysisManager analysisManager;

    public GlobalSlicingAnalysis(final AnalysisInput analysisInput,
                                 final GlobalCallGraphAnalysis globalCallGraphAnalysis,
                                 final AnalysisManager analysisManager) {
        this.globalCallGraphAnalysis = globalCallGraphAnalysis;
        this.analysisManager = analysisManager;
        for (final SootClass sootClass : analysisInput.classes) {
            for (final SootField f : sootClass.getFields()) {
                dataWrite.put(f, new HashSet<>());
            }
            for (final SootMethod m : sootClass.getMethods()) {
                retValues.put(m, new HashSet<>());
            }
        }
        // TODO: parameter ref
        for (final SootClass sootClass : analysisInput.classes) {
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                if (sootMethod.hasActiveBody()) {
                    final Body body = sootMethod.getActiveBody();
                    for (final Unit unit : body.getUnits()) {
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

    private Value search(final Value value, final SootClass sootClass, final SootMethod sootMethod, final Unit u) {
        final IntraProceduralAnalysis analysis = this.analysisManager.getAnalysis(sootClass, sootMethod);
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
