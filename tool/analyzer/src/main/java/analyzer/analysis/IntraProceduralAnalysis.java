package analyzer.analysis;

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public final class IntraProceduralAnalysis {
    public final DominatorAnalysis dominatorAnalysis;
    public final BasicBlockAnalysis basicBlockAnalysis;
//    public final ExceptionHandlingAnalysis exceptionHandlingAnalysis;
    public final UnitGraph graph;
    public final Body body;

    public IntraProceduralAnalysis(final SootMethod method, final Body body) {
        this.body = body;
        final BasicBlockAnalysis basicBlockAnalysis = new BasicBlockAnalysis(body);
        this.graph = new ExceptionalUnitGraph(body);
        this.dominatorAnalysis = new DominatorAnalysis(graph, body.getUnits());
        this.basicBlockAnalysis = new BasicBlockAnalysis(body);
//        this.exceptionHandlingAnalysis = new ExceptionHandlingAnalysis(body, graph, prefix);
    }
}
