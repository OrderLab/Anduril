package analyzer.analysis;

import analyzer.AnalyzerTestBase;
import analyzer.cases.exceptionHandlingAnalysis.ExceptionExample;
import analyzer.cases.intraProceduralAnalysis.DominatorExample;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalIntraProceduralAnalysisTest extends AnalyzerTestBase {

    public static GlobalIntraProceduralAnalysis globalIntraProceduralAnalysis;

    @BeforeAll
    public static void makingIntraProceduralAnalysis() {
        List<SootClass> classList = new LinkedList<>(classes.values());
        classList.sort(Comparator.comparing(SootClass::getName));
        globalIntraProceduralAnalysis = new GlobalIntraProceduralAnalysis(classList);
    }

    @Test
    void testDominatorFlowControl() {
        SootClass target = classes.get(DominatorExample.class.getName());
        SootMethod targetMethod = target.getMethod("void controlFlow()");
        IntraProceduralAnalysis p = globalIntraProceduralAnalysis.getAnalysis(target, targetMethod);
        Map<Integer, Unit> unitIds = methodUnitIds.get(targetMethod);
        //for (Integer i :  unitIds.keySet()) {
          //System.out.println(i);
          //System.out.println(unitIds.get(i).toString());
        //}
        for (int i=1;i<=7;i++) {
            assertTrue(p.dominatorAnalysis.dominators.get(unitIds.get(i))==unitIds.get(i-1));
        }
        assertTrue(p.dominatorAnalysis.dominators.get(unitIds.get(8))==unitIds.get(5));
        assertTrue(p.dominatorAnalysis.dominators.get(unitIds.get(9))==unitIds.get(5));
        for (int i=10;i<=11;i++) {
            assertTrue(p.dominatorAnalysis.dominators.get(unitIds.get(i))==unitIds.get(i-1));
        }
    }

    @Test
    void testDominatorExceptionThrow() {
        SootClass target = classes.get(DominatorExample.class.getName());
        SootMethod targetMethod = target.getMethod("void exceptionThrow()");
        IntraProceduralAnalysis p = globalIntraProceduralAnalysis.getAnalysis(target, targetMethod);
        Map<Integer, Unit> unitIds = methodUnitIds.get(targetMethod);
        //for (Integer i :  unitIds.keySet()) {
          //  System.out.println(i);
          //  System.out.println(unitIds.get(i).toString());
        //}
        for (int i=1;i<=6;i++) {
            assertTrue(p.dominatorAnalysis.dominators.get(unitIds.get(i))==unitIds.get(i-1));
        }
        assertTrue(p.dominatorAnalysis.dominators.get(unitIds.get(7))==unitIds.get(3));
        assertTrue(p.dominatorAnalysis.dominators.get(unitIds.get(8))==unitIds.get(7));
        assertTrue(p.dominatorAnalysis.dominators.get(unitIds.get(9))==unitIds.get(6));
        for (int i=10;i<=11;i++) {
            assertTrue(p.dominatorAnalysis.dominators.get(unitIds.get(i))==unitIds.get(i-1));
        }
    }
}