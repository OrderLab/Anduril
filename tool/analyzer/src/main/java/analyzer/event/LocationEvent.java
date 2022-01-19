package analyzer.event;

import analyzer.analysis.AnalysisManager;
import analyzer.analysis.IntraProceduralAnalysis;
import index.ProgramLocation;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.IfStmt;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class LocationEvent extends ProgramEvent {
    public final ProgramLocation location;
    public ProgramEvent dominationLocation = null;

    public LocationEvent(final ProgramLocation location) {
        this.location = location;
    }

    @Override
    public List<ProgramEvent> computeFrontiers(final AnalysisManager analysisManager) {
        final IntraProceduralAnalysis analysis = analysisManager.getAnalysis(this.location.sootClass, this.location.sootMethod);
        final Unit blockHead = analysis.basicBlockAnalysis.heads.get(this.location.unit);
        if (analysis.basicBlockAnalysis.basicBlocks.get(blockHead) == blockHead) {
            final Unit dominator = analysis.dominatorAnalysis.dominators.get(blockHead);
            if (dominator instanceof IfStmt) {
                final ProgramLocation location = analysisManager.analysisInput.indexManager.index
                        .get(this.location.sootClass).get(this.location.sootMethod).get(dominator);
                dominationLocation = new ConditionEvent(location,
                        ((IfStmt) dominator).getTarget() == blockHead, ((IfStmt) dominator).getCondition());
            } else {
                // switch stmt
            }
        } else {
            if (analysis.basicBlockAnalysis.body.getUnits().getFirst() == blockHead) {
                dominationLocation = new InvocationEvent(this.location.sootClass, this.location.sootMethod);
            } else {
                final ProgramLocation location = analysisManager.analysisInput.indexManager.index
                        .get(this.location.sootClass).get(this.location.sootMethod).get(blockHead);
                dominationLocation = new HandlerEvent(location);
            }
        }
        if (dominationLocation == null) {
            return new LinkedList<>();
        }
        return Arrays.asList(dominationLocation);
    }

    @Override
    public JsonObjectBuilder dump(final EventManager eventManager) {
        return Json.createObjectBuilder()
                .add("id", eventManager.getId(this))
                .add("type", "location_event")
                .add("location", location.dump())
                .add("domination", eventManager.getId(dominationLocation));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationEvent that = (LocationEvent) o;
        return Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }
}
