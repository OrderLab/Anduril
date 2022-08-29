package analyzer.event;

import analyzer.analysis.AnalysisManager;
import soot.SootClass;
import soot.SootMethod;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.LinkedList;
import java.util.List;

public class UncaughtThrowInjectionEvent extends ExceptionInjectionEvent {
    //final SootMethod exceptionMethod;
    public UncaughtThrowInjectionEvent(final SootClass exceptionType) {
        super(exceptionType);
    }

    @Override
    public List<ProgramEvent> computeFrontiers(final AnalysisManager analysisManager) {
        return new LinkedList<>();
    }

    @Override
    public JsonObjectBuilder dump(EventManager eventManager) {
        return Json.createObjectBuilder()
                .add("id", eventManager.getId(this))
                .add("type", "internal_injection_event")
                .add("exception", exceptionType.getName());
                //.add("invocation_class", exceptionMethod.getDeclaringClass().getName())
                //.add("invocation_method", exceptionMethod.getSubSignature());
    }

    @Override
    public String toString() {
        return "Uncaught_throw_injection_event" +"  "+exceptionType.getName();
    }


}
