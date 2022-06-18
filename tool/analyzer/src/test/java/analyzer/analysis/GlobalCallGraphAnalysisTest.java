package analyzer.analysis;

import analyzer.AnalyzerTestBase;
import analyzer.cases.SocketCnxAcceptor;
import index.IndexManager;
import index.ProgramLocation;
import org.junit.jupiter.api.Test;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.tagkit.LineNumberTag;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class GlobalCallGraphAnalysisTest  extends AnalyzerTestBase {



    @Test
    void testMakingIndex() {
        //System.out.println(helper.bodyMap.get(SocketCnxAcceptor.class.getName()).size());
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