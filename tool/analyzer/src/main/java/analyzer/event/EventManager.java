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
    private static class Node {
        private final ProgramEvent event;
        private final int depth;
        private final List<Node> out = new LinkedList<>();
        private final List<Node> in  = new LinkedList<>();
        private Node(final ProgramEvent event, final int depth) {
            this.event = event;
            this.depth = depth;
        }
    }

    private final Map<ProgramEvent, Node> nodes = new HashMap<>();
    private final Map<ProgramEvent, Integer> nodeIds = new HashMap<>();
    public final int startingPointNumber;

    public final int getId(final ProgramEvent e) {
        return nodeIds.getOrDefault(e, -1);
    }

    private final List<InjectionPoint> injectionPoints = new LinkedList<>();
    public final AnalysisManager analysisManager;

    public EventManager(final AnalysisManager analysisManager) {
        this.analysisManager = analysisManager;
//        final Map<String, Object> options = new HashMap<>();
//        options.put(JsonGenerator.PRETTY_PRINTING, true);
//        this.jsonBuilderFactory = Json.createBuilderFactory(options);

        final LinkedList<Node> queue = new LinkedList<>();
        final Node root = new Node(analysisManager.analysisInput.symptomEvent, 0);
        queue.addLast(root);
        nodeIds.put(root.event, 0);
        nodes.put(root.event, root);

        for (final ProgramLocation loc : analysisManager.analysisInput.logEvents) {
            final ProgramEvent event = new LocationEvent(loc);
            if (!nodes.containsKey(event)) {
                final Node node = new Node(event, 0);
                queue.addLast(node);
                nodeIds.put(event, nodeIds.size());
                nodes.put(event, node);
            }
        }
        this.startingPointNumber = nodes.size();
        while (!queue.isEmpty()) {
            final Node node = queue.pollFirst();
            // TODO: remove the constraint
            // original depth: 21
//            if (node.depth == 8) {
//                continue;
//            }
            for (final ProgramEvent event : node.event.computeFrontiers(analysisManager)) {
                Node child;
                if (nodes.containsKey(event)) {
                    child = nodes.get(event);
                } else {
                    child = new Node(event, node.depth + 1);
                    nodeIds.put(event, nodeIds.size());
                    nodes.put(event, child);
                    queue.addLast(child);
                }
                node.out.add(child);
                child.in.add(node);
            }
            if (node.event instanceof HandlerEvent) {
                this.injectionPoints.addAll(((HandlerEvent) node.event).injectionPoints);
            }
            if (node.event instanceof InternalInjectionEvent) {
                this.injectionPoints.addAll(((InternalInjectionEvent) node.event).injectionPoints);
            }
        }
        int internalInjections = 0, externalInjections = 0;
        for (final InjectionPoint injectionPoint : injectionPoints) {
            if (injectionPoint.callee instanceof InternalInjectionEvent) {
                internalInjections++;
            }
            if (injectionPoint.callee instanceof ExternalInjectionEvent) {
                externalInjections++;
            }
        }
        System.out.println("injections: " + injectionPoints.size());
        System.out.println("internal injections: " + internalInjections);
        System.out.println("external injections: " + externalInjections);
    }

    public void dump(final String path) {
        final JsonArrayBuilder nodesJson = Json.createArrayBuilder();
        final JsonArrayBuilder treeJson = Json.createArrayBuilder();
        for (final Node node : nodes.values()) {
            nodesJson.add(node.event.dump(this));
            final JsonArrayBuilder childrenJson = Json.createArrayBuilder();
            for (final Node child : node.out) {
                childrenJson.add(getId(child.event));
            }
            treeJson.add(Json.createObjectBuilder()
                    .add("id", getId(node.event))
                    .add("children", childrenJson));
        }
        final JsonArrayBuilder injectionJson = Json.createArrayBuilder();
        for (final InjectionPoint injectionPoint : this.injectionPoints) {
            if (injectionPoint.callee instanceof ExternalInjectionEvent) {
                injectionJson.add(injectionPoint.dump(this));
            }
        }
        final JsonObject json = Json.createObjectBuilder()
                .add("nodes", nodesJson)
                .add("start", this.startingPointNumber)
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
        for (final InjectionPoint injectionPoint : this.injectionPoints) {
            if (injectionPoint.callee instanceof ExternalInjectionEvent) {
                injectionPoint.instrument();
            }
        }
    }
}
