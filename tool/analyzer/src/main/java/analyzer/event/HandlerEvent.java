package analyzer.event;

import analyzer.analysis.AnalysisManager;
import analyzer.analysis.ExceptionHandlingAnalysis;
import index.ProgramLocation;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.ThrowStmt;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.*;

public final class HandlerEvent extends ProgramEvent {
    final ProgramLocation location;
    final List<ProgramEvent> frontiers = new LinkedList();
    final List<InjectionPoint> injectionPoints = new LinkedList<>();
    public HandlerEvent(final ProgramLocation location) {
        this.location = location;
    }

    @Override
    public List<ProgramEvent> computeFrontiers(final AnalysisManager analysisManager) {
        final ExceptionHandlingAnalysis analysis = analysisManager.exceptionAnalysis.analyses.get(location.sootMethod);
        if (analysis.transit2throw.containsKey(location.unit)) {
            for (final Map.Entry<SootClass, Set<Unit>> entry : analysis.transit2throw.get(location.unit).entrySet()) {
                final SootClass exception = entry.getKey();
                for (final Unit unit : entry.getValue()) {
                    final ProgramLocation loc = analysisManager.analysisInput.indexManager.index
                            .get(this.location.sootClass).get(this.location.sootMethod).get(unit);
                    if (analysis.internalCalls.containsKey(unit)) {
                        final InternalInjectionEvent injectionEvent =
                                new InternalInjectionEvent(analysis.internalCalls.get(unit), exception);
                        //boolean uncaught = false;
                        for (final SootMethod virtualMethod : analysisManager.callGraphAnalysis.virtualCalls
                                .get(analysis.internalCalls.get(unit))) {
                            if (virtualMethod.hasActiveBody()) {
                                frontiers.add(new InternalInjectionEvent(virtualMethod, exception));
                                //if (analysisManager.exceptionAnalysis.analyses.get(virtualMethod).NewExceptionUncaught.contains(exception))
                                //uncaught = true;
                            }
                        }
                        //if (uncaught)
                        injectionPoints.add(analysisManager.createInjectionPoint(this, injectionEvent, loc));
                    } else if (analysis.libCalls.containsKey(unit)) {
                        final ExternalInjectionEvent injectionEvent =
                                new ExternalInjectionEvent(analysis.libCalls.get(unit), exception);
                        frontiers.add(injectionEvent);
                        injectionPoints.add(analysisManager.createInjectionPoint(this, injectionEvent, loc));
                    } else if (unit instanceof ThrowStmt) {
                        frontiers.add(new LocationEvent(loc));
                    } else {
                        System.out.println("Warning in HandlerEvent");
                    }
                }
            }
        } else {
//            System.out.println("monitor exception: " + location.dump().build().toString());
        }
        return this.frontiers;
    }

    @Override
    public JsonObjectBuilder dump(final EventManager eventManager) {
        return Json.createObjectBuilder()
                .add("id", eventManager.getId(this))
                .add("type", "handler_event")
                .add("location", location.dump());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandlerEvent that = (HandlerEvent) o;
        return Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    @Override
    public String toString(){return "Handler_event"+"  "+location.unit;}
}
