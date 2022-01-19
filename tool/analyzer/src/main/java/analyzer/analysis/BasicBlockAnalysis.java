package analyzer.analysis;

import soot.*;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;

import java.util.HashMap;

public final class BasicBlockAnalysis {
    public final Body body;
    public final HashMap<Unit, Unit> basicBlocks; // basic block head -> instrumentation point
    public final HashMap<Unit, Unit> heads;
    public int counterStart, counterEnd;

    public BasicBlockAnalysis(final Body body) {
        this.body = body;
        basicBlocks = new HashMap<>();
        final PatchingChain<Unit> units = body.getUnits();
        Unit head = units.getFirst();
        while (isLeadingStmt(head)) {
            head = units.getSuccOf(head);
        }
        basicBlocks.put(units.getFirst(), head);
        for (final Unit unit : units) {
            if (unit instanceof IfStmt) {
                final Unit thenBranch = ((IfStmt) unit).getTargetBox().getUnit();
                basicBlocks.put(thenBranch, thenBranch);
                final Unit elseBranch = units.getSuccOf(unit);
                basicBlocks.put(elseBranch, elseBranch);
            }
        }
        for (final Trap trap : body.getTraps()) {
            final Unit handler = trap.getHandlerUnit();
            basicBlocks.put(handler, units.getSuccOf(handler));
        }
        head = null;
        heads = new HashMap<>();
        for (final Unit unit : units) {
            if (basicBlocks.containsKey(unit)) {
                head = unit;
            }
            if (head != null) {
                heads.put(unit, head);
            }
        }
    }

    public static boolean isLeadingStmt(final Unit unit) {
        if (!(unit instanceof IdentityStmt))
            return false;
        final Value value = ((IdentityStmt) unit).getRightOp();
        return (value instanceof ThisRef) || (value instanceof ParameterRef);
    }
}
