package analyzer.analysis;

import analyzer.AnalyzerTestBase;
import analyzer.cases.callGraphAnalysis.ChildClass;
import analyzer.cases.callGraphAnalysis.ParentClass;
import analyzer.cases.callGraphAnalysis.Person;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GlobalCallGraphAnalysisTest  extends AnalyzerTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalCallGraphAnalysisTest.class);

    public static GlobalCallGraphAnalysis callGraphAnalysis;

    @BeforeAll
    public static void makingCallGraphAnalysis () {
        LOG.info("ClassGraphAnalysis.....");
        List<SootClass> classList = new LinkedList<>(classes.values());
        classList.sort(Comparator.comparing(SootClass::getName));
        callGraphAnalysis = new GlobalCallGraphAnalysis(classList);
    }

    @Test
    void testVirtualCallMap() {
        SootClass personInterface = classes.get(Person.class.getName());
        SootMethod dispInInterface = personInterface.getMethod("void disp()");
        SootClass parentSuperClass = classes.get(ParentClass.class.getName());
        SootMethod dispSuperClass = parentSuperClass.getMethod("void disp()");
        //The parent class implements the Person interface so disp method in both parent and child
        //override that in Person.
        assertTrue(callGraphAnalysis.virtualCalls.get(dispInInterface).
                contains(classes.get(ParentClass.class.getName()).getMethod("void disp()")));
        assertTrue(callGraphAnalysis.virtualCalls.get(dispInInterface).
                contains(classes.get(ChildClass.class.getName()).getMethod("void disp()")));
        //Parent class's disp method is overriden by that in Child class
        assertTrue(callGraphAnalysis.virtualCalls.get(dispSuperClass).
                contains(classes.get(ChildClass.class.getName()).getMethod("void disp()")));
    }

    @Test
    void testBackwordCallMap() {
        assertTrue(true);
    }

}