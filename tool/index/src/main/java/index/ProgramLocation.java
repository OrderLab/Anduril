package index;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.tagkit.LineNumberTag;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Objects;

public class ProgramLocation {
    public final SootClass sootClass;
    public final SootMethod sootMethod;
    public final Unit unit;
    public final int unitId;
    public final int lineNumber;
    public ProgramLocation(final SootClass sootClass, final SootMethod sootMethod, final Unit unit, final int unitId) {
        this.sootClass = sootClass;
        this.sootMethod = sootMethod;
        this.unit = unit;
        this.unitId = unitId;
        final LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
        if (tag == null) {
            this.lineNumber = -1;
        } else {
            this.lineNumber = tag.getLineNumber();
        }
    }
    public JsonObjectBuilder dump() {
        return Json.createObjectBuilder()
                .add("class", sootClass.getName())
                .add("method", sootMethod.getSubSignature())
                .add("instruction", unit.toString())
                .add("instruction_id", unitId)
                .add("line_number", lineNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgramLocation location = (ProgramLocation) o;
        return Objects.equals(unit, location.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }
}
