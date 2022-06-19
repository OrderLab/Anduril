package analyzer.analysis;

import analyzer.AnalyzerTestBase;
import analyzer.cases.callGraphAnalysis.BackwardUsage;
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
        SootClass parentClass = classes.get(ParentClass.class.getName());
        SootMethod parenMethod = parentClass.getMethod("void disp()");
        SootClass childClass = classes.get(ChildClass.class.getName());
        SootMethod childMethod = childClass.getMethod("void disp()");
        SootClass callingClass = classes.get(BackwardUsage.class.getName());
        //Method use1-4 call both
        for (int i=1;i<5;i++) {
            String methodName = "void use" + String.valueOf(i) + "()";
            assertTrue(callGraphAnalysis.backwardCallMap.get(parenMethod).containsKey(callingClass.getMethod(methodName)));
            assertTrue(callGraphAnalysis.backwardCallMap.get(parenMethod).get(callingClass.getMethod(methodName)).size() == 1);
            assertTrue(callGraphAnalysis.backwardCallMap.get(childMethod).containsKey(callingClass.getMethod(methodName)));
            assertTrue(callGraphAnalysis.backwardCallMap.get(parenMethod).get(callingClass.getMethod(methodName)).size() == 1);
        }
        //Method use5 only has potential call child one.
        String last = "void use" + String.valueOf(5) + "()";
        assertFalse(callGraphAnalysis.backwardCallMap.get(parenMethod).containsKey(callingClass.getMethod(last)));
        assertTrue(callGraphAnalysis.backwardCallMap.get(childMethod).containsKey(callingClass.getMethod(last)));
        assertTrue(callGraphAnalysis.backwardCallMap.get(childMethod).get(callingClass.getMethod(last)).size() == 1);
    }

}