package index;

import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.spark.ondemand.pautil.SootUtil;
import soot.tagkit.LineNumberTag;

import javax.json.JsonObject;
import java.util.*;

public class IndexManager {
    public Map<String, SootClass> classes = new TreeMap<>();
    public Map<SootClass, Map<SootMethod, Map<Unit, ProgramLocation>>> index = new HashMap<>();
    public final Map<SootMethod, Map<Integer, Unit>> methodUnitIds = new HashMap<>();
    public final String prefix;

    public final Map<LogEntry, ProgramLocation> logEntries = new HashMap<>();

    //Used for test
    public IndexManager(Map<SootClass, Map<SootMethod, Map<Unit, ProgramLocation>>> index, Map<String, SootClass> classes) {
        this.index = index;
        this.classes = classes;
        prefix = null;
    }

    public IndexManager(final Collection<SootClass> classes, final String prefix) {
        this.prefix = prefix;
        for (final SootClass sootClass : classes) {
            if (sootClass.getName().startsWith(prefix)) {
                this.classes.put(sootClass.getName(), sootClass);
                final Map<SootMethod, Map<Unit, ProgramLocation>> maps = new HashMap<>();
                index.put(sootClass, maps);
                final String shortClassName = sootClass.getName().substring(sootClass.getName().lastIndexOf('.') + 1);
                for (final SootMethod sootMethod : new ArrayList<SootMethod>(sootClass.getMethods())) {
                    if (sootMethod.hasActiveBody()) {
                        final Map<Unit, ProgramLocation> locations = new HashMap<>();
                        maps.put(sootMethod, locations);
                        final Map<Integer, Unit> unitIds = new HashMap<>();
                        methodUnitIds.put(sootMethod, unitIds);
                        int id = 0;
                        for (final Unit unit : sootMethod.getActiveBody().getUnits()) {
                            final ProgramLocation loc = new ProgramLocation(sootClass, sootMethod, unit, id);
                            locations.put(unit, loc);
                            unitIds.put(id, unit);
                            id++;
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
                                                logEntries.put(new LogEntry(shortClassName, getLine(unit)), loc);
                                            default : break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static int getLine(Unit unit) {
        final LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
        if (tag != null) {
            return tag.getLineNumber();
        }
        return -1;
    }

    public static final class LogEntry {
        public final String name;
        public final int line;

        public LogEntry(String name, int line) {
            this.name = name;
            this.line = line;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LogEntry logEntry = (LogEntry) o;
            return line == logEntry.line && Objects.equals(name, logEntry.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, line);
        }
    }

    int ttt = 0;
    public ProgramLocation parse(final JsonObject json) {
//        if (++ttt < 10) System.out.println(json); else return null;
        try {
            final SootClass sootClass = this.classes.get(json.getString("class"));
            final SootMethod sootMethod = sootClass.getMethod(json.getString("method"));
            final Unit unit = this.methodUnitIds.get(sootMethod).get(json.getInt("instruction_id"));
            return this.index.get(sootClass).get(sootMethod).get(unit);
        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println(json);
            return null;
        }
    }
}
