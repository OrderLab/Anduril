package analyzer.analysis;

import soot.Body;
import soot.PatchingChain;
import soot.Trap;
import soot.Unit;
import soot.jimple.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.*;

/**
 * Analyze control dependency of each program statement
 */
//public final class DominatorAnalysis {
//    public final Map<Unit, Unit> dominators = new HashMap<>();
//    final Map<Unit, Set<Unit>> domSets = new HashMap<>();
//    final Map<Unit, Set<Unit>> outs = new HashMap<>();
//
//    public DominatorAnalysis(final DirectedGraph<Unit> graph, final Body body) {
//        final PatchingChain<Unit> units = body.getUnits();
//        for (final Unit unit : units) {
//            final Set<Unit> out = new HashSet<>();
//            outs.put(unit, out);
//            if (unit instanceof IfStmt) {
//                out.add(units.getSuccOf(unit));
//                out.add(((IfStmt) unit).getTarget());
//            } else if (unit instanceof RetStmt || unit instanceof ReturnStmt || unit instanceof ReturnVoidStmt) {
//            } else if (unit instanceof GotoStmt) {
//                out.add(((GotoStmt) unit).getTarget());
//            } else if (unit instanceof LookupSwitchStmt) {
//                out.addAll(((LookupSwitchStmt) unit).getTargets());
//            } else if (unit instanceof TableSwitchStmt) {
//                out.addAll(((TableSwitchStmt) unit).getTargets());
//            } else {
//                out.add(units.getSuccOf(unit));
//            }
//            out.remove(null);
//        }
//        final Set<Unit> visited = new HashSet<>();
//        final LinkedList<Unit> queue = new LinkedList<>();
//        visited.add(units.getFirst());
//        queue.add(units.getFirst());
//        domSets.put(units.getFirst(), new HashSet<>(Collections.singletonList(units.getFirst())));
//        for (final Trap trap : body.getTraps()) {
//            visited.add(trap.getHandlerUnit());
//            queue.add(trap.getHandlerUnit());
//            final Set<Unit> set = new HashSet<>(Collections.singletonList(trap.getHandlerUnit()));
//            domSets.put(trap.getHandlerUnit(), set);
//            Unit u = trap.getBeginUnit();
//            while (u != trap.getEndUnit()) {
//                set.add(u);
//                u = units.getSuccOf(u);
//            }
//        }
//        while (!queue.isEmpty()) {
//            final Unit dom = queue.pollFirst();
//            for (final Unit unit : outs.get(dom)) {
//                if (visited.add(unit)) {
//                    queue.add(unit);
//                    final Set<Unit> set = new HashSet<>(Collections.singletonList(unit));
//                    set.addAll(domSets.get(dom));
//                    domSets.put(unit, set);
//                }
//            }
//        }
//        queue.addAll(visited);
//        while (!queue.isEmpty()) {
//            final Unit dom = queue.pollFirst();
//            final Set<Unit> domDomSet = domSets.get(dom);
//            visited.remove(dom);
//            for (final Unit unit : outs.get(dom)) {
//                if (unit != dom) {
//                    final Set<Unit> domSet = domSets.get(unit);
//                    final List<Unit> del = new LinkedList<>();
//                    for (final Unit e : domSet) {
//                        if (unit != e) {
//                            if (!domDomSet.contains(e)) {
//                                del.add(e);
//                            }
//                        }
//                    }
//                    if (!del.isEmpty()) {
//                        for (final Unit e : del) {
//                            domSet.remove(e);
//                        }
//                        if (visited.add(unit)) {
//                            queue.add(unit);
//                        }
//                    }
//                }
//            }
//        }
//        for (final Unit unit: units) {
//            final Set<Unit> doms = domSets.get(unit);
//            if (doms == null) {
//                continue;
//            }
//            int size = doms.size();
//            for (final Unit dom: doms) {
//                if (domSets.get(dom).size() + 1 == size) {
//                    dominators.put(unit, dom);
//                    break;
//                }
//            }
//        }
//    }
//}

public final class DominatorAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<Unit>> {
    private final FlowSet<Unit> fullSet;

    public final Map<Unit, Unit> dominators;

    public DominatorAnalysis(final DirectedGraph<Unit> graph, final PatchingChain<Unit> units) {
        super(graph);
        fullSet = new ArraySparseSet<>();
        for (final Unit unit: units)
            fullSet.add(unit);
        doAnalysis();
        dominators = new HashMap<>();
        for (final Unit unit: units) {
            FlowSet<Unit> doms = getFlowAfter(unit);
            int size = doms.size();
            for (final Unit dom: doms) {
                if (getFlowAfter(dom).size() + 1 == size) {
                    dominators.put(unit, dom);
                    break;
                }
            }
        }
    }

    @Override
    protected FlowSet<Unit> newInitialFlow() {
        return fullSet.clone();
    }
    @Override
    protected FlowSet<Unit> entryInitialFlow() {
        return new ArraySparseSet<>();
    }
    @Override
    protected void merge(FlowSet<Unit> in1, FlowSet<Unit> in2, FlowSet<Unit> out) {
        in1.intersection(in2, out);
    }
    @Override
    protected void copy(FlowSet<Unit> src, FlowSet<Unit> dst) {
        src.copy(dst);
    }
    @Override
    protected void flowThrough(FlowSet<Unit> in, Unit node, FlowSet<Unit> out) {
        in.copy(out);
        out.add(node);
    }
}
