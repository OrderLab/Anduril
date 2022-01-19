package analyzer.event;

import analyzer.analysis.AnalysisManager;
import index.ProgramLocation;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.*;

public final class ExternalInjectionEvent extends ExceptionInjectionEvent {
    final SootMethod exceptionMethod;
    public ExternalInjectionEvent(final SootMethod exceptionMethod, final SootClass exceptionType) {
        super(exceptionType);
        this.exceptionMethod = exceptionMethod;
    }

    @Override
    public List<ProgramEvent> computeFrontiers(final AnalysisManager analysisManager) {
        return new LinkedList<>();
    }

    @Override
    public JsonObjectBuilder dump(final EventManager eventManager) {
        return Json.createObjectBuilder()
                .add("id", eventManager.getId(this))
                .add("type", "external_injection_event")
                .add("exception", exceptionType.getName())
                .add("invocation_class", exceptionMethod.getDeclaringClass().getName())
                .add("invocation_method", exceptionMethod.getSubSignature());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ExternalInjectionEvent that = (ExternalInjectionEvent) o;
        return Objects.equals(exceptionMethod, that.exceptionMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), exceptionMethod);
    }
}
