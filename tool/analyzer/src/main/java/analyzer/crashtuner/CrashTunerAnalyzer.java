package analyzer.crashtuner;

import soot.*;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;

import java.util.*;

public final class CrashTunerAnalyzer {
    static public Set<Location> analyze(final Set<SootClass> classes, final String exampleLog) {
        final Set<Location> result = new HashSet<>();
        final Set<SootField> keyFields = new HashSet<>();
        for (final SootClass sootClass : classes) {
            final Collection<SootMethod> methods = sootClass.getMethods();
            for (final SootMethod sootMethod : methods) {
                if (sootMethod.hasActiveBody()) {
                    final PatchingChain<Unit> units = sootMethod.retrieveActiveBody().getUnits();
                    for (final Unit unit : units) {
                        for (final ValueBox valueBox : unit.getUseBoxes()) {
                            final Value value = valueBox.getValue();
                            if (value instanceof InvokeExpr) {
                                final SootMethod log = ((InvokeExpr) value).getMethod();
                                final String name = log.getDeclaringClass().getName();
                                if (name.equals("org.apache.commons.logging.Log") ||
                                        name.equals("org.slf4j.Logger")) {
                                    switch (log.getName()) {
                                        case "error":
                                        case "info" :
                                        case "warn" :
                                        case "debug":
                                            for (final Value arg: ((InvokeExpr) value).getArgs()) {
                                                if (arg instanceof FieldRef) {
                                                    keyFields.add(((FieldRef) arg).getField());
                                                }
                                            }
                                        default : break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (final SootClass sootClass : classes) {
            final Collection<SootMethod> methods = sootClass.getMethods();
            for (final SootMethod sootMethod : methods) {
                if (sootMethod.hasActiveBody()) {
                    final PatchingChain<Unit> units = sootMethod.retrieveActiveBody().getUnits();
                    for (final Unit unit : units) {
                        for (final ValueBox valueBox : unit.getUseBoxes()) {
                            final Value value = valueBox.getValue();
                            if (value instanceof FieldRef) {
                                final SootField field = ((FieldRef) value).getField();
                                if (keyFields.contains(field)) {
                                    result.add(new Location(sootMethod, unit));
                                    break;
                                }
                            }
                        }
                        for (final ValueBox valueBox : unit.getDefBoxes()) {
                            final Value value = valueBox.getValue();
                            if (value instanceof FieldRef) {
                                final SootField field = ((FieldRef) value).getField();
                                if (keyFields.contains(field)) {
                                    result.add(new Location(sootMethod, unit));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static final class Location {
        public final SootMethod method;
        public final Unit unit;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Location)) return false;
            Location location = (Location) o;
            return method.equals(location.method) && unit.equals(location.unit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method, unit);
        }

        public Location(final SootMethod method, final Unit unit) {
            this.method = method;
            this.unit = unit;
        }
    }
}
