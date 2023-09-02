package analyzer.event;

import analyzer.analysis.AnalysisManager;
import index.ProgramLocation;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.*;

public final class InvocationEvent extends ProgramEvent {
    public final SootClass locationClass;
    public final SootMethod locationMethod;
    final List<ProgramEvent> frontiers = new LinkedList();
    public InvocationEvent(SootClass locationClass, SootMethod locationMethod) {
        this.locationClass = locationClass;
        this.locationMethod = locationMethod;
    }

    @Override
    public List<ProgramEvent> computeFrontiers(final AnalysisManager analysisManager) {
        if (!analysisManager.analysisInput.classSet.contains(locationClass)) {
            return new LinkedList<>();
        }
        for (final Map.Entry<SootMethod, Set<Unit>> entry :
                analysisManager.callGraphAnalysis.backwardCallMap.get(locationMethod).entrySet()) {
            final SootMethod caller = entry.getKey();
            for (final Unit unit : entry.getValue()) {
                final ProgramLocation loc = analysisManager.analysisInput.indexManager.index
                        .get(caller.getDeclaringClass()).get(caller).get(unit);
                this.frontiers.add(new LocationEvent(loc));
            }
        }
        // Meta Handler to Calling
        if (analysisManager.threadSchedulingAnalysis.handler2Call.containsKey(locationMethod)) {
            for (SootMethod throwing : analysisManager.threadSchedulingAnalysis.handler2Call.getOrDefault(locationMethod,new HashSet<>())) {
                for (SootClass methodExceptionType : analysisManager.exceptionAnalysis.analyses.get(throwing).methodExceptions.keySet()) {
                    frontiers.add(new InternalInjectionEvent(throwing, methodExceptionType));
                }
            }
        } else if (analysisManager.threadSchedulingAnalysis.handler2Inv.containsKey(locationMethod)) {
            for (SootMethod invoking : analysisManager.threadSchedulingAnalysis.handler2Inv.getOrDefault(locationMethod,new HashSet<>())) {
                frontiers.add(new InvocationEvent(invoking.getDeclaringClass(),invoking));
            }
        }
        return this.frontiers;
    }

    @Override
    public JsonObjectBuilder dump(final EventManager eventManager) {
        return Json.createObjectBuilder()
                .add("id", eventManager.getId(this))
                .add("type", "invocation_event")
                .add("class", locationClass.getName())
                .add("method", locationMethod.getSubSignature());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvocationEvent that = (InvocationEvent) o;
        return Objects.equals(locationMethod, that.locationMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationMethod);
    }

    @Override
    public String toString() {
        return "Invocation Event" + "  "+locationClass.getName() +"  "+locationMethod.getSubSignature();
    }
}
