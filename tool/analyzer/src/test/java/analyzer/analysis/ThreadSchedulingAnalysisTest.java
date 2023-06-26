package analyzer.analysis;

import analyzer.AnalyzerTestBase;
import analyzer.cases.threadSchedulingAnalysis.CallableExample;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThreadSchedulingAnalysisTest extends AnalyzerTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalCallGraphAnalysisTest.class);

    public static GlobalCallGraphAnalysis callGraphAnalysis;

    public static ThreadSchedulingAnalysis threadSchedulingAnalysis;

    private static List<SootClass> classList;


    @BeforeAll
    public static void makingCallGraphAnalysis() {
        LOG.info("ClassGraphAnalysis.....");
        classList = new LinkedList<>(classes.values());
        classList.sort(Comparator.comparing(SootClass::getName));
        callGraphAnalysis = new GlobalCallGraphAnalysis(classList);
        LOG.info("ThreadSchedulingAnalysis.....");
        threadSchedulingAnalysis = new ThreadSchedulingAnalysis(classList,callGraphAnalysis);
    }

    @Test
    void simpleLocalCase() {
        SootClass target = classes.get(CallableExample.class.getName());
        SootMethod targetMethod = target.getMethod("void submitThenGetSimple()");
        Map<Integer, Unit> unitIds = methodUnitIds.get(targetMethod);
        //for (Integer i :  unitIds.keySet()) {
        //  System.out.println(i);
        //  System.out.println(unitIds.get(i).toString());
        //}
        assertTrue(threadSchedulingAnalysis.get2Call.containsKey(unitIds.get(5)));
        assertEquals(1, threadSchedulingAnalysis.get2Call.get(unitIds.get(5)).size());
        for (SootMethod call : threadSchedulingAnalysis.get2Call.get(unitIds.get(5))) {
            assertEquals("java.lang.Object call()", call.getSubSignature());
        }
    }

    @Test
    void simpleLocalLambdaCase() {
        SootClass target = classes.get(CallableExample.class.getName());
        SootMethod targetMethod = target.getMethod("void submitThenGetLamda()");
        Map<Integer, Unit> unitIds = methodUnitIds.get(targetMethod);
        assertTrue(threadSchedulingAnalysis.get2Call.containsKey(unitIds.get(4)));
        assertEquals(1, threadSchedulingAnalysis.get2Call.get(unitIds.get(4)).size());
        for (SootMethod call : threadSchedulingAnalysis.get2Call.get(unitIds.get(4))) {
            assertEquals("java.lang.Integer lambda$submitThenGetLamda$0(int)", call.getSubSignature());
        }
    }

    @Test
    void arrayInBetween() {
        SootClass target = classes.get(CallableExample.class.getName());
        SootMethod targetMethod = target.getMethod("void arrayInBetween()");
        Map<Integer, Unit> unitIds = methodUnitIds.get(targetMethod);
        //for (Integer i :  unitIds.keySet()) {
        //  System.out.println(i);
        //  System.out.println(unitIds.get(i).toString());
        //}
        assertTrue(threadSchedulingAnalysis.get2Call.containsKey(unitIds.get(8)));
        assertEquals(1, threadSchedulingAnalysis.get2Call.get(unitIds.get(8)).size());
        for (SootMethod call : threadSchedulingAnalysis.get2Call.get(unitIds.get(8))) {
            assertEquals("java.lang.Object call()", call.getSubSignature());
        }
    }

    @Test
    void arrayListInBetween() {
        SootClass target = classes.get(CallableExample.class.getName());
        SootMethod targetMethod = target.getMethod("void arrayListInBetween()");
        Map<Integer, Unit> unitIds = methodUnitIds.get(targetMethod);
        assertTrue(threadSchedulingAnalysis.get2Call.containsKey(unitIds.get(10)));
        assertEquals(1, threadSchedulingAnalysis.get2Call.get(unitIds.get(10)).size());
        for (SootMethod call : threadSchedulingAnalysis.get2Call.get(unitIds.get(10))) {
            assertEquals("java.lang.Object call()", call.getSubSignature());
        }
    }

}