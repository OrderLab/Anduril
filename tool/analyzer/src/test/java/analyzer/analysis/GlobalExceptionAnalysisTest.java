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

    public static GlobalReturnAnalysis returnAnalysis;

    private static List<SootClass> classList;

    @BeforeAll
    public static void makingCallGraphAndExceptionAnalysis() {
        LOG.info("ClassGraphAnalysis.....");
        classList = new LinkedList<>(classes.values());
        classList.sort(Comparator.comparing(SootClass::getName));
        callGraphAnalysis = new GlobalCallGraphAnalysis(classList);
        LOG.info("ReturnAnalysis.....");
        returnAnalysis = new GlobalReturnAnalysis(classList, callGraphAnalysis);
        LOG.info("ExceptionAnalysis.....");
        exceptionAnalysis = new GlobalExceptionAnalysis(classList, callGraphAnalysis,returnAnalysis.analyses);
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
        assertTrue(targetMethodAnalysis.methodExceptions.containsKey(Scene.v().loadClassAndSupport(IOException.class.getName())));
    }

    @Test
    void complexLocalCase() {
        SootClass target = classes.get(ExceptionExample.class.getName());
        SootMethod targetMethod = target.getMethod("void complexLocalCaught()");
        ExceptionHandlingAnalysis targetMethodAnalysis = exceptionAnalysis.analyses.get(targetMethod);
        Map<Integer, Unit> unitIds = methodUnitIds.get(targetMethod);
        //for (Integer i :  unitIds.keySet()) {
            //System.out.println(i);
            //System.out.println(unitIds.get(i).toString());
        //}
        //System.out.println(targetMethodAnalysis.transit2throw.toString());
        SootClass ioException = Scene.v().loadClassAndSupport(IOException.class.getName());
        //$stack3 := @caughtexception={java.io.IOException=[throw $stack4]}
        assertTrue(targetMethodAnalysis.transit2throw.containsKey(unitIds.get(6)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(6)).get(ioException).contains(unitIds.get(5)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(6)).get(ioException).size()==1);
        //$stack3 := @caughtexception={java.io.IOException=[throw $stack4]}
        assertTrue(targetMethodAnalysis.transit2throw.containsKey(unitIds.get(4)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(4)).get(ioException).contains(unitIds.get(3)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(4)).get(ioException).size()==1);
        //Test for place whose exception can not be caught.
        assertTrue(targetMethodAnalysis.methodExceptions.containsKey(ioException));
        assertTrue(targetMethodAnalysis.methodExceptions.get(ioException).contains(unitIds.get(7)));
    }

    @Test
    void internalCallingCase() {
        SootClass target = classes.get(ExceptionExample.class.getName());
        SootMethod targetMethod = target.getMethod("void internalCalling()");
        ExceptionHandlingAnalysis targetMethodAnalysis = exceptionAnalysis.analyses.get(targetMethod);
        Map<Integer, Unit> unitIds = methodUnitIds.get(targetMethod);
        //for (Integer i :  unitIds.keySet()) {
        //System.out.println(i);
        //System.out.println(unitIds.get(i).toString());
        //}
        //System.out.println(targetMethodAnalysis.transit2throw.toString());
        SootClass ioException = Scene.v().loadClassAndSupport(IOException.class.getName());

        //The local Variable name may change but unitIds will not change through each test.

        //$$stack2 := @caughtexception={java.io.IOException=
        // [virtualinvoke this.<analyzer.cases.exceptionHandlingAnalysis.ExceptionExample: void complexLocalCaught()>()]}
        assertTrue(targetMethodAnalysis.transit2throw.containsKey(unitIds.get(3)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(3)).get(ioException).contains(unitIds.get(1)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(3)).get(ioException).size()==1);
        //$stack3 := @caughtexception={java.io.IOException=[throw $stack2]}
        assertTrue(targetMethodAnalysis.transit2throw.containsKey(unitIds.get(6)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(6)).get(ioException).contains(unitIds.get(4)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(6)).get(ioException).size()==1);
        //Test for place whose exception can not be caught.
        assertTrue(targetMethodAnalysis.methodExceptions.containsKey(ioException));
        assertTrue(targetMethodAnalysis.methodExceptions.get(ioException).contains(unitIds.get(7)));
    }

    @Test
    void externalCallingCase() {
        SootClass target = classes.get(ExceptionExample.class.getName());
        SootMethod targetMethod = target.getMethod("void externalCalling()");
        ExceptionHandlingAnalysis targetMethodAnalysis = exceptionAnalysis.analyses.get(targetMethod);
        Map<Integer, Unit> unitIds = methodUnitIds.get(targetMethod);
        //for (Integer i :  unitIds.keySet()) {
         //System.out.println(i);
        //System.out.println(unitIds.get(i).toString());
        //}
        //System.out.println(targetMethodAnalysis.transit2throw.toString());
        SootClass ioException = Scene.v().loadClassAndSupport(IOException.class.getName());
        //$stack4 := @caughtexception={java.io.IOException=[throw $stack5]}
        assertTrue(targetMethodAnalysis.transit2throw.containsKey(unitIds.get(7)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(7)).get(ioException).contains(unitIds.get(5)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(7)).get(ioException).size()==1);
        //$stack5 := @caughtexception=
        // {java.io.IOException=[virtualinvoke $stack2.<java.net.ServerSocket: java.net.Socket accept()>()]}
        assertTrue(targetMethodAnalysis.transit2throw.containsKey(unitIds.get(4)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(4)).get(ioException).contains(unitIds.get(2)));
        assertTrue(targetMethodAnalysis.transit2throw.get(unitIds.get(4)).get(ioException).size()==1);
        assertTrue(targetMethodAnalysis.methodExceptions.containsKey(ioException));
        assertTrue(targetMethodAnalysis.methodExceptions.get(ioException).contains(unitIds.get(8)));
    }

    @Test
    void unCaughtExceptionSimpleCase() {
        SootClass target = classes.get(ExceptionExample.class.getName());
        SootMethod targetMethod = target.getMethod("void simpleExceptionUncaught(int)");
        ExceptionHandlingAnalysis targetMethodAnalysis = exceptionAnalysis.analyses.get(targetMethod);
        assertTrue(targetMethodAnalysis.NewExceptionUncaught.size() == 1);
        SootClass ioException = Scene.v().loadClassAndSupport(IOException.class.getName());
        SootClass runtimeException = Scene.v().loadClassAndSupport(RuntimeException.class.getName());
        assertTrue(targetMethodAnalysis.NewExceptionUncaught.contains(ioException));
        assertTrue(!targetMethodAnalysis.NewExceptionUncaught.contains(runtimeException));
    }

    @Test
    void unCaughtExceptionComplexCase() {
        SootClass target = classes.get(ExceptionExample.class.getName());
        SootMethod targetMethod = target.getMethod("void complexExceptionUncaught(int)");
        ExceptionHandlingAnalysis targetMethodAnalysis = exceptionAnalysis.analyses.get(targetMethod);
        assertTrue(targetMethodAnalysis.NewExceptionUncaught.size() == 1);
        SootClass ioException = Scene.v().loadClassAndSupport(IOException.class.getName());
        SootClass runtimeException = Scene.v().loadClassAndSupport(RuntimeException.class.getName());
        assertTrue(!targetMethodAnalysis.NewExceptionUncaught.contains(ioException));
        assertTrue(targetMethodAnalysis.NewExceptionUncaught.contains(runtimeException));
        //System.out.println(targetMethodAnalysis.throwLocations);
    }

    @Test
    void transparentWrapperCase() {
        SootClass target = classes.get(ExceptionExample.class.getName());
        SootMethod targetMethod = target.getMethod("void transparentWrapper()");
        ExceptionHandlingAnalysis targetMethodAnalysis = exceptionAnalysis.analyses.get(targetMethod);
        assertTrue(targetMethodAnalysis.throwLocations.size()==1);
        assertTrue(targetMethodAnalysis.throw2transit.size()==1);
        SootClass ioException = Scene.v().loadClassAndSupport(IOException.class.getName());
        assertTrue(targetMethodAnalysis.methodExceptions.containsKey(ioException));
    }

    @Test
    void newExceptionWrapperCase() {
        SootClass target = classes.get(ExceptionExample.class.getName());
        SootMethod targetMethod = target.getMethod("void newExceptionInWrapper()");
        ExceptionHandlingAnalysis targetMethodAnalysis = exceptionAnalysis.analyses.get(targetMethod);
        assertTrue(targetMethodAnalysis.methodExceptions.size()==1);
        SootClass secException = Scene.v().loadClassAndSupport(SecurityException.class.getName());
        assertTrue(targetMethodAnalysis.methodExceptions.containsKey(secException));
    }

}