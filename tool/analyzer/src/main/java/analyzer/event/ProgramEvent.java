package analyzer.event;

import analyzer.analysis.AnalysisManager;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.List;

public abstract class ProgramEvent {
    abstract public List<ProgramEvent> computeFrontiers(final AnalysisManager analysisManager);
    abstract public JsonObjectBuilder dump(final EventManager eventManager);
//    abstract public void parse(final JsonObject json);
}
