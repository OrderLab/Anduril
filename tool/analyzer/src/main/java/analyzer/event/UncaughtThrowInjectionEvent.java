package analyzer.event;

import analyzer.analysis.AnalysisManager;
import soot.SootClass;
import soot.SootMethod;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class UncaughtThrowInjectionEvent extends ExceptionInjectionEvent {
    final SootMethod exceptionMethod;
    public UncaughtThrowInjectionEvent(final SootMethod exceptionMethod, final SootClass exceptionType) {
        super(exceptionType);
        this.exceptionMethod = exceptionMethod;

    }

    @Override
    public List<ProgramEvent> computeFrontiers(final AnalysisManager analysisManager) {
        return new LinkedList<>();
    }

    @Override
    public JsonObjectBuilder dump(EventManager eventManager) {
        return Json.createObjectBuilder()
                .add("id", eventManager.getId(this))
                .add("type", "Uncaught_throw_injection_event")
                .add("exception", exceptionType.getName())
                .add("invocation_class", exceptionMethod.getDeclaringClass().getName())
                .add("invocation_method", exceptionMethod.getSubSignature());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UncaughtThrowInjectionEvent that = (UncaughtThrowInjectionEvent) o;
        return Objects.equals(exceptionMethod, that.exceptionMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), exceptionMethod);
    }

    @Override
    public String toString() {
        return "Uncaught_throw_injection_event" +"  "+exceptionType.getName();
    }


}
