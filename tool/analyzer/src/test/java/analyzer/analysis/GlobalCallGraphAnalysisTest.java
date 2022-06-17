package analyzer.analysis;

import analyzer.AnalyzerTestBase;
import analyzer.cases.SocketCnxAcceptor;
import org.junit.jupiter.api.Test;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalCallGraphAnalysisTest  extends AnalyzerTestBase {

    @Test
    void testSimple() {
        System.out.println(helper.bodyMap.get(SocketCnxAcceptor.class.getName()).size());
        SootClass targetCls = Scene.v().loadClassAndSupport(SocketCnxAcceptor.class.getName());

        Body runMethod = helper.getBody(SocketCnxAcceptor.class.getName(), "void run()");
    }

    @Test
    void testDifficult() {
        System.out.println(helper.bodyMap.get(SocketCnxAcceptor.class.getName()).size());
        SootClass targetCls = Scene.v().loadClassAndSupport(SocketCnxAcceptor.class.getName());

        Body runMethod = helper.getBody(SocketCnxAcceptor.class.getName(), "void run()");
    }
}