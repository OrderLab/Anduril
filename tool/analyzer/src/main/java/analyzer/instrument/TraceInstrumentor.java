package analyzer.instrument;

import analyzer.analysis.BasicBlockAnalysis;
import runtime.TraceAgent;
import soot.*;
import soot.jimple.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

public class TraceInstrumentor {
    private final BasicBlockAnalysis basicBlockAnalysis;

    public static final SootClass agentClass;
    public static final SootMethod traceMethod;
    public static final SootMethod recordMethod;
    public static final SootMethod injectMethod;
    public static final SootMethod initMethod;
    static {
        agentClass = Scene.v().loadClassAndSupport(TraceAgent.class.getCanonicalName());
        traceMethod = agentClass.getMethodByName("trace");
        recordMethod = agentClass.getMethodByName("threadRecord");
        injectMethod = agentClass.getMethodByName("inject");
        initMethod = agentClass.getMethodByName("initStub");
    }

    public TraceInstrumentor(final BasicBlockAnalysis basicBlockAnalysis) {
        this.basicBlockAnalysis = basicBlockAnalysis;
    }

    private static int counter = 0;

    public void instrument() {
        final PatchingChain<Unit> units = basicBlockAnalysis.body.getUnits();
        basicBlockAnalysis.counterStart = counter;
        for (final Map.Entry<Unit, Unit> entry : basicBlockAnalysis.basicBlocks.entrySet()) {
            basicBlockAnalysis.ids.put(entry.getKey(), counter);
            LinkedList<Value> args = new LinkedList<>();
            args.add(IntConstant.v(counter));
            counter++;
//            final StaticInvokeExpr traceExpr = Jimple.v().newStaticInvokeExpr(traceMethod.makeRef(), args);
//            final InvokeStmt traceStmt = Jimple.v().newInvokeStmt(traceExpr);
//            units.insertBefore(traceStmt, entry.getValue());
        }
        basicBlockAnalysis.counterEnd = counter;
    }
}
