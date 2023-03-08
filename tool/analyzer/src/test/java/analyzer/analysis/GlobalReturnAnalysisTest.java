package analyzer.analysis;

import analyzer.AnalyzerTestBase;
import analyzer.cases.exceptionHandlingAnalysis.ExceptionExample;
import analyzer.cases.returnAnalysis.ExceptionReturnExample;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalReturnAnalysisTest extends AnalyzerTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalReturnAnalysisTest.class);

    public static GlobalCallGraphAnalysis callGraphAnalysis;

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
    }

    @Test
    void simpleTransparentCase() {
        SootClass target = classes.get(ExceptionReturnExample.class.getName());
        SootMethod targetMethod = target.getMethod("java.io.IOException wrapper1(java.io.IOException)");
        assert (returnAnalysis.analyses.get(targetMethod).transparent);
        assert(returnAnalysis.analyses.get(targetMethod).passLocations.keySet().isEmpty());
    }

    @Test
    void simpleNewExceptionCase() {
        SootClass target = classes.get(ExceptionReturnExample.class.getName());
        SootMethod targetMethod = target.getMethod("java.lang.Throwable wrapper2(java.lang.Exception)");
        assert (!returnAnalysis.analyses.get(targetMethod).transparent);
        assert(returnAnalysis.analyses.get(targetMethod).newExceptions.size() == 1);
        SootClass ioException = Scene.v().loadClassAndSupport(IOException.class.getName());
        assert(returnAnalysis.analyses.get(targetMethod).newExceptions.contains(ioException));
    }

    @Test
    void transparentPropagatedCase() {
        SootClass target = classes.get(ExceptionReturnExample.class.getName());
        SootMethod targetMethod = target.getMethod("java.lang.Throwable wrapper3(java.io.IOException)");
        //System.out.println(returnAnalysis.analyses.get(targetMethod).passLocations);
        //System.out.println(returnAnalysis.analyses.get(targetMethod).wrapperCalls);
        assert (returnAnalysis.analyses.get(targetMethod).transparent);
        assert(returnAnalysis.analyses.get(targetMethod).newExceptions.size() == 0);
        SootClass ioException = Scene.v().loadClassAndSupport(IOException.class.getName());
    }

    @Test
    void transparentNotPropagatedCase() {
        SootClass target = classes.get(ExceptionReturnExample.class.getName());
        SootMethod targetMethod = target.getMethod("java.lang.Throwable wrapper4(java.io.IOException)");
        //System.out.println(returnAnalysis.analyses.get(targetMethod).passLocations);
        //System.out.println(returnAnalysis.analyses.get(targetMethod).wrapperCalls);
        assert (!returnAnalysis.analyses.get(targetMethod).transparent);
        // New Exceptions created  in wrapper 2 should be propagated
        assert(returnAnalysis.analyses.get(targetMethod).newExceptions.size() == 1);
        SootClass ioException = Scene.v().loadClassAndSupport(IOException.class.getName());
        assert(returnAnalysis.analyses.get(targetMethod).newExceptions.contains(ioException));
    }

    @Test
    void transparentPropagatedCastCase() {
        SootClass target = classes.get(ExceptionReturnExample.class.getName());
        SootMethod targetMethod = target.getMethod("java.lang.Throwable wrapper5(java.lang.Exception)");
        //System.out.println(returnAnalysis.analyses.get(targetMethod).passLocations);
        //System.out.println(returnAnalysis.analyses.get(targetMethod).wrapperCalls);
        assert (returnAnalysis.analyses.get(targetMethod).transparent);
        assert(returnAnalysis.analyses.get(targetMethod).newExceptions.size() == 0);
    }

    @Test
    void newExceptionNotPropagateCase() {
        SootClass target = classes.get(ExceptionReturnExample.class.getName());
        SootMethod targetMethod = target.getMethod("java.lang.Throwable wrapper6(java.lang.Exception)");
        //System.out.println(returnAnalysis.analyses.get(targetMethod).wrapperCalls);
        assert (!returnAnalysis.analyses.get(targetMethod).transparent);
        assert(returnAnalysis.analyses.get(targetMethod).newExceptions.size() == 0);
    }
}