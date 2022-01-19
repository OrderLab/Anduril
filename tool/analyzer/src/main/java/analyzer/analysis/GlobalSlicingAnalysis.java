package analyzer.analysis;

import soot.*;
import soot.jimple.FieldRef;

import java.util.*;

public final class GlobalSlicingAnalysis {
    public static final class Location {
        public final SootMethod method;
        public final Unit unit;

        public Location(final SootMethod method, final Unit unit) {
            this.method = method;
            this.unit = unit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Location location = (Location) o;
            return Objects.equals(method, location.method) && Objects.equals(unit, location.unit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method, unit);
        }
    }
    public final Map<SootField, Set<Location>> dataWrite = new HashMap<>();
    public GlobalSlicingAnalysis(final AnalysisInput analysisInput) {
        for (final SootClass sootClass : analysisInput.classes) {
            for (final SootField f : sootClass.getFields()) {
                dataWrite.put(f, new HashSet<>());
            }
        }
        // TODO: parameter ref
        for (final SootClass sootClass : analysisInput.classes) {
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                if (sootMethod.hasActiveBody()) {
                    final Body body = sootMethod.getActiveBody();
                    for (final Unit unit : body.getUnits()) {
                        for (final ValueBox valueBox : unit.getDefBoxes()) {
                            final Value value = valueBox.getValue();
                            if (value instanceof FieldRef) {
                                try {
                                    dataWrite.get(((FieldRef) value).getField()).add(new Location(sootMethod, unit));
                                } catch (NullPointerException e) {
                                    // ===
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
