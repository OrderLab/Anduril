package analyzer.event;

import analyzer.analysis.AnalysisManager;
import analyzer.analysis.BasicBlockAnalysis;
import index.ProgramLocation;
import soot.*;
import soot.jimple.IdentityStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;

import java.util.*;

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

    public EventGraph(final AnalysisManager analysisManager, ProgramEvent symptomEvent, Set<ProgramLocation> logEvents) {
        this.root = new Node(null, -1);

        final LinkedList<EventGraph.Node> queue = new LinkedList<>();
        final EventGraph.Node root = new EventGraph.Node(symptomEvent, 0);
        queue.addLast(root);
        nodeIds.put(root.event, 0);
        nodes.put(root.event, root);
        //New added
        this.root.out.add(root);
        //root.in.add(this.root);

        for (final ProgramLocation loc : logEvents) {
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
        //After all, calculate the uncaughtException Event and add the injections Points
        final List<InjectionPoint> uncaughtThrowInjectionPoints = new LinkedList<>();
        for (final InjectionPoint injectionPoint : this.injectionPoints) {
            if (injectionPoint.callee instanceof InternalInjectionEvent) {
                final SootClass exception = ((InternalInjectionEvent) injectionPoint.callee).exceptionType;
                final SootMethod throwingMethod = ((InternalInjectionEvent) injectionPoint.callee).exceptionMethod;
                if (throwingMethod.hasActiveBody()) {
                    if (analysisManager.exceptionAnalysis.analyses.get(throwingMethod).NewExceptionUncaught.contains(exception)) {
                        //Add into the graph
                        final EventGraph.Node node = nodes.get(injectionPoint.callee);
                        final ProgramEvent event = new UncaughtThrowInjectionEvent(throwingMethod,exception);
                        if (nodeIds.containsKey(event)) {
                            continue;
                        }
                        final EventGraph.Node child = new EventGraph.Node(event, node.depth + 1);
                        nodeIds.put(event, nodeIds.size());
                        nodes.put(event, child);
                        node.out.add(child);
                        child.in.add(node);
                        //Construct Injection Point
                        final PatchingChain<Unit> units = throwingMethod.getActiveBody().getUnits();
                        Unit first = throwingMethod.getActiveBody().getUnits().getFirst();
                        while (BasicBlockAnalysis.isLeadingStmt(first))
                            first = units.getSuccOf(first);
                        final ProgramLocation loc = analysisManager.analysisInput.indexManager.index
                                .get(throwingMethod.getDeclaringClass()).get(throwingMethod).get(first);
                        uncaughtThrowInjectionPoints.add(analysisManager.createInjectionPoint(injectionPoint.callee, event, loc));
                    }
                }
            }
        }
        injectionPoints.addAll(uncaughtThrowInjectionPoints);
    }

    public List<Node> bfs() {
        Set<EventGraph.Node> visited = new HashSet<EventGraph.Node>();
        List<Node> bfs = new LinkedList<>();
        LinkedList<Node> queue = new LinkedList<>();
        queue.addLast(this.root);
        while (!queue.isEmpty()) {
            Node node = queue.pollFirst();
            if (node != this.root) {
                bfs.add(node);
            }
            for (Node n : node.out) {
                if (!visited.contains(n)) {
                    visited.add(n);
                    queue.addLast(n);
                }
            }
        }

        return bfs;
    }


}
