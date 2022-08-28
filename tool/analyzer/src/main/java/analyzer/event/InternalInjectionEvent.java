package analyzer.event;

import analyzer.analysis.AnalysisManager;
import analyzer.analysis.ExceptionHandlingAnalysis;
import index.ProgramLocation;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.*;

public final class InternalInjectionEvent extends ExceptionInjectionEvent {
    final SootMethod exceptionMethod;
    final List<ProgramEvent> frontiers = new LinkedList<>();
    final List<InjectionPoint> injectionPoints = new LinkedList<>();
    public InternalInjectionEvent(final SootMethod exceptionMethod, final SootClass exceptionType) {
        super(exceptionType);
        this.exceptionMethod = exceptionMethod;
    }

    private static final Set<Unit> emptySet = new HashSet<>();

    @Override
    public List<ProgramEvent> computeFrontiers(final AnalysisManager analysisManager) {
        if (!analysisManager.analysisInput.classSet.contains(exceptionMethod.getDeclaringClass())) {
            return new LinkedList<>();
        }
        final ExceptionHandlingAnalysis analysis = analysisManager.exceptionAnalysis.analyses.get(exceptionMethod);
        for (final Unit unit : analysis.methodExceptions.getOrDefault(this.exceptionType, emptySet)) {
            final ProgramLocation loc = analysisManager.analysisInput.indexManager.index
                    .get(exceptionMethod.getDeclaringClass()).get(exceptionMethod).get(unit);
            if (analysis.libCalls.containsKey(unit)) {
                final ExternalInjectionEvent injectionEvent =
                        new ExternalInjectionEvent(analysis.libCalls.get(unit), this.exceptionType);
                this.frontiers.add(injectionEvent);
                injectionPoints.add(analysisManager.createInjectionPoint(this, injectionEvent, loc));
            } else if (analysis.internalCalls.containsKey(unit)) {
                final InternalInjectionEvent injectionEvent =
                        new InternalInjectionEvent(analysis.internalCalls.get(unit), this.exceptionType);
                for (final SootMethod virtualMethod : analysisManager.callGraphAnalysis.virtualCalls
                        .get(analysis.internalCalls.get(unit))) {
                    if (virtualMethod.hasActiveBody()) {
                        frontiers.add(new InternalInjectionEvent(virtualMethod, this.exceptionType));
                    }
                }
                if (analysis.NewExceptionUncaught.contains(this.exceptionType))
                    injectionPoints.add(analysisManager.createInjectionPoint(this, injectionEvent, loc));
            } else {
                this.frontiers.add(new LocationEvent(loc));
            }
        }
        return this.frontiers;
    }

    @Override
    public JsonObjectBuilder dump(final EventManager eventManager) {
        return Json.createObjectBuilder()
                .add("id", eventManager.getId(this))
                .add("type", "internal_injection_event")
                .add("exception", exceptionType.getName())
                .add("invocation_class", exceptionMethod.getDeclaringClass().getName())
                .add("invocation_method", exceptionMethod.getSubSignature());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InternalInjectionEvent that = (InternalInjectionEvent) o;
        return Objects.equals(exceptionMethod, that.exceptionMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), exceptionMethod);
    }

    @Override
    public String toString() {return "internal_injection_event"+"  "+exceptionType.getName();}
}
