package analyzer.analysis;

import analyzer.AnalyzerTestBase;
import analyzer.cases.callGraphAnalysis.Person;
import analyzer.cases.exceptionHandlingAnalysis.ExceptionExample;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionAnalysisTest extends AnalyzerTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalCallGraphAnalysisTest.class);

    public static GlobalCallGraphAnalysis callGraphAnalysis;

    public static GlobalExceptionAnalysis exceptionAnalysis;

    private static List<SootClass> classList;

    @BeforeAll
    public static void makingCallGraphAnalysis() {
        LOG.info("ClassGraphAnalysis.....");
        classList = new LinkedList<>(classes.values());
        classList.sort(Comparator.comparing(SootClass::getName));
        callGraphAnalysis = new GlobalCallGraphAnalysis(classList);
        LOG.info("ExceptionAnalysis.....");
        exceptionAnalysis = new GlobalExceptionAnalysis(classList, callGraphAnalysis);
    }

    @Test
    void simpleLocalCase() {
        SootClass target = classes.get(ExceptionExample.class.getName());
        SootMethod targetMethod = target.getMethod("void simpleLocalCaught()");
        ExceptionHandlingAnalysis targetMethodAnalysis = exceptionAnalysis.analyses.get(targetMethod);
        Map<Integer, Unit> unitIds = methodUnitIds.get(targetMethod);
        //for (Integer i :  unitIds.keySet()) {
          //  System.out.println(i);
            //System.out.println(unitIds.get(i).toString());
        //}
        //Test for the logic: {$stack3 := @caughtexception={java.io.IOException=[throw $stack2]}}
        assertTrue(targetMethodAnalysis.transit2throw.containsKey(unitIds.get(4)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(4))
                .get(Scene.v().loadClassAndSupport(IOException.class.getName())).contains(unitIds.get(3)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(4))
                .get(Scene.v().loadClassAndSupport(IOException.class.getName())).size() == 1);
    }
}