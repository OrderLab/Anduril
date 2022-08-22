package analyzer.instrument;

import analyzer.analysis.BasicBlockAnalysis;
import soot.*;
import soot.jimple.*;

import java.util.LinkedList;
import java.util.List;

public final class ThreadInstrumentor {
    public final SootClass sootClass;
    public final SootMethod runMethod;
    public final Body body;
    public ThreadInstrumentor(final SootClass sootClass, final SootMethod runMethod) {
        this.sootClass = sootClass;
        this.runMethod = runMethod;
        this.body = runMethod.getActiveBody();
    }
   /*
    public void instrument() {
        final String name = sootClass.getName();
        final PatchingChain<Unit> units = body.getUnits();
        final List<Unit> retStmts = new LinkedList<>();
        for (final Unit unit : units) {
            if (unit instanceof ReturnStmt || unit instanceof ReturnVoidStmt) {
                retStmts.add(unit);
            }
        }
        Unit head = units.getFirst();
        while (BasicBlockAnalysis.isLeadingStmt(head)) head = units.getSuccOf(head);
        head = units.getPredOf(head);
        units.insertAfter(getThreadRecordStmt(name, 1), head);
        for (final Unit unit : retStmts) {
            units.insertBefore(getThreadRecordStmt(name, -1), unit);
        }
    }

    private InvokeStmt getThreadRecordStmt(final String name, final int d) {
            LinkedList<Value> args = new LinkedList<>();
            args.add(StringConstant.v(name));
            args.add(IntConstant.v(d));
            final StaticInvokeExpr traceExpr = Jimple.v().newStaticInvokeExpr(TraceInstrumentor.recordMethod.makeRef(), args);
            return Jimple.v().newInvokeStmt(traceExpr);
    }
    */
}
