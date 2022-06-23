package analyzer.event;

import analyzer.analysis.AnalysisManager;
import analyzer.analysis.GlobalSlicingAnalysis;
import analyzer.analysis.IntraProceduralAnalysis;
import index.ProgramLocation;
import soot.*;
import soot.jimple.*;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class ConditionEvent extends LocationEvent {
    public final boolean isThenBranch;
    public final List<ProgramEvent> frontiers = new LinkedList<>();
    public final List<GlobalSlicingAnalysis.Location> preconditionLocations = new LinkedList<>();
    public final Value pred;

    public ConditionEvent(final ProgramLocation location, final boolean isThenBranch, final Value pred) {
        super(location);
        this.isThenBranch = isThenBranch;
        this.pred = pred;
    }

    private Value search(final Value value, final AnalysisManager analysisManager) {
        final IntraProceduralAnalysis analysis = analysisManager.globalIntraProceduralAnalysis.getAnalysis(location.sootClass, location.sootMethod);
        final PatchingChain<Unit> units = location.sootMethod.getActiveBody().getUnits();
        for (Unit unit = analysis.basicBlockAnalysis.heads.get(location.unit); unit != location.unit; unit = units.getSuccOf(unit)) {
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

    @Override
    public List<ProgramEvent> computeFrontiers(final AnalysisManager analysisManager) {
        frontiers.addAll(super.computeFrontiers(analysisManager));
//        System.out.println(pred);
        boolean eq = isThenBranch;
        Value lhs = null, rhs = null;
        if (pred instanceof EqExpr) {
            lhs = ((EqExpr) pred).getOp1();
            rhs = ((EqExpr) pred).getOp2();
        } else if (pred instanceof NeExpr) {
            eq = !eq;
            lhs = ((NeExpr) pred).getOp1();
            rhs = ((NeExpr) pred).getOp2();
        }
//        System.out.println(lhs);
//        System.out.println(rhs);
        lhs = search(lhs, analysisManager);
        rhs = search(rhs, analysisManager);
//        System.out.println(lhs);
//        System.out.println(rhs);
        if (lhs instanceof FieldRef) {
            final Collection<GlobalSlicingAnalysis.Location> writeLocations =
                    analysisManager.slicingAnalysis.dataWrite.get(((FieldRef) lhs).getField());
            // if the variable comes from external library (parent class), the locations are not initialized here
            if (writeLocations != null) {
                preconditionLocations.addAll(writeLocations);
            }
        }
//        if (lhs instanceof InvokeExpr) {
//            final SootMethod calleeMethod = ((InvokeExpr) lhs).getMethod();
//            final SootClass calleeClass = calleeMethod.getDeclaringClass();
//            if (analysisManager.analysisInput.classSet.contains(calleeClass)) {
//                calleeMethod
//            }
//        }
        for (final GlobalSlicingAnalysis.Location loc : preconditionLocations) {
            frontiers.add(new LocationEvent(analysisManager.analysisInput.indexManager.index
                    .get(loc.method.getDeclaringClass()).get(loc.method).get(loc.unit)));
        }
        return frontiers;
    }

    @Override
    public JsonObjectBuilder dump(final EventManager eventManager) {
        return Json.createObjectBuilder()
                .add("id", eventManager.getId(this))
                .add("type", "condition_event")
                .add("location", location.dump())
                .add("domination", eventManager.getId(dominationLocation))
                .add("isThenBranch", isThenBranch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConditionEvent that = (ConditionEvent) o;
        return isThenBranch == that.isThenBranch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isThenBranch);
    }
}
