package analyzer.event;

import analyzer.analysis.AnalysisInput;
import analyzer.analysis.AnalysisManager;
import index.ProgramLocation;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public final class EventManager {
//    private final JsonBuilderFactory jsonBuilderFactory;
//    public JsonObjectBuilder createObjectBuilder() {
//        return jsonBuilderFactory.createObjectBuilder();
//    }
//    public JsonArrayBuilder createArrayBuilder() {
//        return jsonBuilderFactory.createArrayBuilder();
//    }

    public final int getId(final ProgramEvent e) {
        return this.eventGraph.nodeIds.getOrDefault(e, -1);
    }

    public final EventGraph eventGraph;

    public final AnalysisManager analysisManager;

    public EventManager(final AnalysisManager analysisManager) {
        this.analysisManager = analysisManager;
//        final Map<String, Object> options = new HashMap<>();
//        options.put(JsonGenerator.PRETTY_PRINTING, true);
//        this.jsonBuilderFactory = Json.createBuilderFactory(options);

        this.eventGraph = new EventGraph(analysisManager,analysisManager.analysisInput.symptomEvent,analysisManager.analysisInput.logEvents);

        int internalInjections = 0, externalInjections = 0;
        for (final InjectionPoint injectionPoint : this.eventGraph.injectionPoints) {
            if (injectionPoint.callee instanceof InternalInjectionEvent) {
                internalInjections++;
            }
            if (injectionPoint.callee instanceof ExternalInjectionEvent) {
                externalInjections++;
            }
        }
        System.out.println("injections: " + this.eventGraph.injectionPoints.size());
        System.out.println("internal injections: " + internalInjections);
        System.out.println("external injections: " + externalInjections);
    }

    public void dump(final String path) {
        final JsonArrayBuilder nodesJson = Json.createArrayBuilder();
        final JsonArrayBuilder treeJson = Json.createArrayBuilder();
        for (final EventGraph.Node node : this.eventGraph.nodes.values()) {
            nodesJson.add(node.event.dump(this));
            final JsonArrayBuilder childrenJson = Json.createArrayBuilder();
            for (final EventGraph.Node child : node.out) {
                childrenJson.add(getId(child.event));
            }
            treeJson.add(Json.createObjectBuilder()
                    .add("id", getId(node.event))
                    .add("children", childrenJson));
        }
        final JsonArrayBuilder injectionJson = Json.createArrayBuilder();
        for (final InjectionPoint injectionPoint : this.eventGraph.injectionPoints) {
            if (injectionPoint.callee instanceof ExternalInjectionEvent) {
                injectionJson.add(injectionPoint.dump(this));
            }
        }
        final JsonObject json = Json.createObjectBuilder()
                .add("nodes", nodesJson)
                .add("start", this.eventGraph.startingPointNumber)
                .add("tree", treeJson)
                .add("injections", injectionJson)
                .build();
        final Map<String, Object> options = new HashMap<>();
        options.put(JsonGenerator.PRETTY_PRINTING, true);
        final JsonWriterFactory writerFactory = Json.createWriterFactory(options);
        try (final FileWriter fw = new FileWriter(path);
             final JsonWriter jsonWriter = writerFactory.createWriter(fw)) {
            jsonWriter.writeObject(json);
        } catch (final IOException ignored) { }
    }

    public void instrumentInjections() {
        for (final InjectionPoint injectionPoint : this.eventGraph.injectionPoints) {
            if (injectionPoint.callee instanceof ExternalInjectionEvent) {
                injectionPoint.instrument();
            }
        }
    }
}
