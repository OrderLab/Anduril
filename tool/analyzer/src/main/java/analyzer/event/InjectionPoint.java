package analyzer.event;

import analyzer.analysis.BasicBlockAnalysis;
import analyzer.instrument.TraceInstrumentor;
import index.ProgramLocation;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.StaticInvokeExpr;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.LinkedList;
import java.util.List;

public final class InjectionPoint {
    public final ProgramEvent caller;
    public final ProgramEvent callee;
    public final ProgramLocation location;
    public final int id;
    public int blockId = -1;

    public InjectionPoint(final ProgramEvent caller, final ProgramEvent callee, final ProgramLocation location, final int id) {
        this.caller = caller;
        this.callee = callee;
        this.location = location;
        this.id = id;
    }

    public void instrument() {
        final List<Value> args = new LinkedList<>();
        args.add(IntConstant.v(this.id));
        args.add(IntConstant.v(this.blockId));
        final StaticInvokeExpr injectExpr = Jimple.v().newStaticInvokeExpr(TraceInstrumentor.injectMethod.makeRef(), args);
        final InvokeStmt stmt = Jimple.v().newInvokeStmt(injectExpr);
        location.sootMethod.getActiveBody().getUnits().insertBefore(stmt, location.unit);
    }

    public JsonObjectBuilder dump(final EventManager eventManager) {
        final BasicBlockAnalysis basicBlockAnalysis =
                eventManager.analysisManager.globalIntraProceduralAnalysis.getAnalysis(location.sootClass, location.sootMethod).basicBlockAnalysis;
        this.blockId = basicBlockAnalysis.ids.get(basicBlockAnalysis.heads.get(location.unit));
        final JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("id", this.id)
                .add("caller", eventManager.getId(this.caller))
                .add("callee", eventManager.getId(this.callee))
                .add("location", this.location.dump())
                .add("block", this.blockId);
        if (this.callee instanceof InternalInjectionEvent) {
            final InternalInjectionEvent trans = (InternalInjectionEvent) callee;
            builder.add("exception", trans.exceptionType.getName());
            builder.add("invocation", trans.exceptionMethod.getSubSignature());
        }
        if (this.callee instanceof ExternalInjectionEvent) {
            final ExternalInjectionEvent trans = (ExternalInjectionEvent) callee;
            builder.add("exception", trans.exceptionType.getName());
            builder.add("invocation", trans.exceptionMethod.getSubSignature());
        }
        return builder;
    }
}
