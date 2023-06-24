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
        assertTrue(threadSchedulingAnalysis.get2Call.get(unitIds.get(5)).size()==1);
        for (SootMethod call : threadSchedulingAnalysis.get2Call.get(unitIds.get(5))) {
            call.getSubSignature().equals("java.lang.Object call()");
        }
    }

}