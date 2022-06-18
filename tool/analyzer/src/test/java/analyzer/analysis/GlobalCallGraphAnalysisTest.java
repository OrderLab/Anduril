package analyzer.analysis;

import analyzer.AnalyzerTestBase;
import analyzer.cases.SocketCnxAcceptor;
import index.IndexManager;
import index.ProgramLocation;
import org.junit.jupiter.api.Test;
import soot.*;
import soot.jimple.InvokeExpr;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class GlobalCallGraphAnalysisTest  extends AnalyzerTestBase {

    @Test
    void testMakingIndex() {
        System.out.println(helper.bodyMap.get(SocketCnxAcceptor.class.getName()).size());
        //SootClass targetCls = Scene.v().loadClassAndSupport(SocketCnxAcceptor.class.getName());

        //Body runMethod = helper.getBody(SocketCnxAcceptor.class.getName(), "void run()");
        //for (final Unit unit : runMethod.getUnits()) {
            //System.out.println(unit.toString());
        //}
        Map<String, SootClass> classes = new TreeMap<>();
        Map<SootClass, Map<SootMethod, Map<Unit, ProgramLocation>>> index = new HashMap<>();
        Map<SootMethod, Map<Integer, Unit>> methodUnitIds = new HashMap<>();
        String prefix = "analyzer.cases.";
        for (final SootClass sootClass : Scene.v().getApplicationClasses()) {
            //System.out.println(sootClass.toString());
            if (sootClass.getName().startsWith(prefix)) {
                classes.put(sootClass.getName(), sootClass);
                final Map<SootMethod, Map<Unit, ProgramLocation>> maps = new HashMap<>();
                index.put(sootClass, maps);
                final String shortClassName = sootClass.getName().substring(sootClass.getName().lastIndexOf('.') + 1);
                for (final SootMethod sootMethod : sootClass.getMethods()) {
                    //System.out.println(sootMethod.toString());
                    final Map<Unit, ProgramLocation> locations = new HashMap<>();
                    maps.put(sootMethod, locations);
                    final Map<Integer, Unit> unitIds = new HashMap<>();
                    methodUnitIds.put(sootMethod, unitIds);
                    int id = 0;
                    for (final Unit unit :
                            helper.getBody(sootClass.getName(), sootMethod.getSubSignature()).getUnits()) {
                        //System.out.println(unit.toString());
                        final ProgramLocation loc = new ProgramLocation(sootClass, sootMethod, unit, id);
                        locations.put(unit, loc);
                        unitIds.put(id, unit);
                        id++;
                    }
                }
            }
        }
        for (SootClass sootClass:index.keySet()) {
            for (SootMethod sootMethod:index.get(sootClass).keySet()) {
                for (Unit unit:index.get(sootClass).get(sootMethod).keySet()) {
                    ProgramLocation loc = index.get(sootClass).get(sootMethod).get(unit);
                    System.out.println(loc.sootMethod.toString()+"  "+loc.lineNumber+"  "+loc.unitId);
                }
            }
        }

    }

    @Test
    void testDifficult() {
        System.out.println(helper.bodyMap.get(SocketCnxAcceptor.class.getName()).size());
        SootClass targetCls = Scene.v().loadClassAndSupport(SocketCnxAcceptor.class.getName());

        Body runMethod = helper.getBody(SocketCnxAcceptor.class.getName(), "void run()");
    }
}