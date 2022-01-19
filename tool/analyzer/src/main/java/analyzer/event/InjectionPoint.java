package analyzer.event;

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

    public InjectionPoint(final ProgramEvent caller, final ProgramEvent callee, final ProgramLocation location, final int id) {
        this.caller = caller;
        this.callee = callee;
        this.location = location;
        this.id = id;
    }

    public void instrument() {
        final List<Value> args = new LinkedList<>();
        args.add(IntConstant.v(this.id));
        final StaticInvokeExpr injectExpr = Jimple.v().newStaticInvokeExpr(TraceInstrumentor.injectMethod.makeRef(), args);
        final InvokeStmt stmt = Jimple.v().newInvokeStmt(injectExpr);
        location.sootMethod.getActiveBody().getUnits().insertBefore(stmt, location.unit);
    }

    public JsonObjectBuilder dump(final EventManager eventManager) {
        final JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("id", this.id)
                .add("caller", eventManager.getId(this.caller))
                .add("callee", eventManager.getId(this.callee))
                .add("location", this.location.dump());
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
