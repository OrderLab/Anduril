package analyzer.analysis;

import analyzer.AnalyzerTestBase;
import analyzer.cases.intraProceduralAnalysis.DominatorExample;
import analyzer.cases.slicingAnalysis.SlicingExample;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GlobalSlicingAnalysisTest extends AnalyzerTestBase {

    public static GlobalCallGraphAnalysis callGraphAnalysis;
    public static GlobalIntraProceduralAnalysis globalIntraProceduralAnalysis;
    public static GlobalSlicingAnalysis slicingAnalysis;

    @BeforeAll
    public static void makingIntraProceduralAnalysis() {
        List<SootClass> classList = new LinkedList<>(classes.values());
        classList.sort(Comparator.comparing(SootClass::getName));
        callGraphAnalysis = new GlobalCallGraphAnalysis(classList);
        globalIntraProceduralAnalysis = new GlobalIntraProceduralAnalysis(classList);
        slicingAnalysis = new GlobalSlicingAnalysis(classList, callGraphAnalysis, globalIntraProceduralAnalysis);
    }

    @Test
    void testDataWrite() {
        SootClass target = classes.get(SlicingExample.class.getName());
        SootMethod targetMethod1 = target.getMethod("void write1(int)");
        SootMethod targetMethod2 = target.getMethod("void write2(java.lang.Object)");
        Map<Integer, Unit> unitIds1 = methodUnitIds.get(targetMethod1);
        Map<Integer, Unit> unitIds2 = methodUnitIds.get(targetMethod2);
        //for (final SootField f : target.getFields()) {
            //System.out.println(f.getSubSignature());
        //}
        SootField fieldA = target.getField("int a");
        SootField fieldB = target.getField("java.lang.Object b");
        for (final GlobalSlicingAnalysis.Location l : slicingAnalysis.dataWrite.get(fieldA)){
            assertTrue(l.method==targetMethod1);
            assertTrue(l.unit==unitIds1.get(2));
        }
        for (final GlobalSlicingAnalysis.Location l : slicingAnalysis.dataWrite.get(fieldB)){
            assertTrue(l.method==targetMethod2);
            assertTrue(l.unit==unitIds2.get(2));
        }
    }

    @Test
    void testCheck1() {
        SootClass target = classes.get(SlicingExample.class.getName());
        SootMethod targetMethod1 = target.getMethod("void write1(int)");
        SootMethod targetMethod2 = target.getMethod("void write2(java.lang.Object)");
        SootMethod targetMethod3 = target.getMethod("boolean check1()");
        assert(slicingAnalysis.retTrue.get(targetMethod3).size() == 1);
        assert(slicingAnalysis.retFalse.get(targetMethod3).size() == 1);
        for (Unit unit : slicingAnalysis.retFalse.get(targetMethod3)) {
            System.out.println(unit);
        }
    }

    @Test
    void testCheck2() {
        SootClass target = classes.get(SlicingExample.class.getName());
        SootMethod targetMethod1 = target.getMethod("void write1(int)");
        SootMethod targetMethod2 = target.getMethod("void write2(java.lang.Object)");
        SootMethod targetMethod3 = target.getMethod("boolean check2()");
        assert(slicingAnalysis.retTrue.get(targetMethod3).size() == 1);
        assert(slicingAnalysis.retFalse.get(targetMethod3).size() == 1);
        for (Unit unit : slicingAnalysis.retTrue.get(targetMethod3)) {
            System.out.println(unit);
        }
    }
}