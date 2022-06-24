package analyzer.event;

import analyzer.analysis.AnalysisManager;
import index.ProgramLocation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventGraph {

    //Use for bfs
    Node root;
    public final Map<ProgramEvent, EventGraph.Node> nodes = new HashMap<>();
    public final Map<ProgramEvent, Integer> nodeIds = new HashMap<>();
    public final int startingPointNumber;
    public final List<InjectionPoint> injectionPoints = new LinkedList<>();

    public static class Node {
        public final ProgramEvent event;
        public final int depth;
        public final List<EventGraph.Node> out = new LinkedList<>();
        public final List<EventGraph.Node> in  = new LinkedList<>();
        private Node(final ProgramEvent event, final int depth) {
            this.event = event;
            this.depth = depth;
        }
    }

    public EventGraph(final AnalysisManager analysisManager) {
        this.root = new Node(null, -1);

        final LinkedList<EventGraph.Node> queue = new LinkedList<>();
        final EventGraph.Node root = new EventGraph.Node(analysisManager.analysisInput.symptomEvent, 0);
        queue.addLast(root);
        nodeIds.put(root.event, 0);
        nodes.put(root.event, root);
        //New added
        this.root.out.add(root);
        //root.in.add(this.root);

        for (final ProgramLocation loc : analysisManager.analysisInput.logEvents) {
            final ProgramEvent event = new LocationEvent(loc);
            if (!nodes.containsKey(event)) {
                final EventGraph.Node node = new EventGraph.Node(event, 0);
                queue.addLast(node);
                nodeIds.put(event, nodeIds.size());
                nodes.put(event, node);
                //New added
                this.root.out.add(node);
                //node.in.add(this.root);
            }
        }
        this.startingPointNumber = nodes.size();
        while (!queue.isEmpty()) {
            final EventGraph.Node node = queue.pollFirst();
            // TODO: remove the constraint
            // original depth: 21
//            if (node.depth == 8) {
//                continue;
//            }
            for (final ProgramEvent event : node.event.computeFrontiers(analysisManager)) {
                EventGraph.Node child;
                if (nodes.containsKey(event)) {
                    child = nodes.get(event);
                } else {
                    child = new EventGraph.Node(event, node.depth + 1);
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
    }


}
