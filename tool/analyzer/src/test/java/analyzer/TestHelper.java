package analyzer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import analyzer.phase.PhaseInfo;
import index.IndexManager;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;


/**
 * The helper class is used to load Soot body info for test cases reasons explained in GrayAnalyzerTestBase
 */
public class TestHelper extends SceneTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(TestHelper.class);

    public static final PhaseInfo PHASE_INFO = new PhaseInfo("wjtp", "testhelper",
            "Store body info for later analyses", true, false);

    public static Map<String, Map<String, Body>> bodyMap = new HashMap<>();


    protected void internalTransform(String phaseName, Map<String, String> options) {
        LOG.info("TestHelper running...");
        for (SootClass c : Scene.v().getApplicationClasses()) {
            if (c.getName().startsWith("analyzer.cases.")) { // store method body of all test cases
                LOG.info("Recording method body of test case " + c.getName());
                Map<String, Body> map = new HashMap<>();
                for (SootMethod m : c.getMethods()) {
                    if (m.hasActiveBody()) {
                        System.out.println(m.getSubSignature());
                        // Here we must use subsignature instead of m.getName() to correctly
                        // deal with method overloading! Otherwise, we will later retrieve
                        // we method body that does not match the method!!!
                        map.put(m.getSubSignature(), m.retrieveActiveBody());
                    }
                }
                bodyMap.put(c.getName(), map);
            }
        }
    }

    /**
     * Retrive the method body for a given class name and method sub-signature (i.e., the unique
     * identifying name for a method without the class name part). Note that it is
     * important to pass a sub-signature instead of simply the method name to retrieve the
     * correct body in the presence of method overloading.
     *
     * @param className The name of the class that contains this method
     * @param methodSubsig The subsignature of the method
     * @return The body of the method if there is a match, null otherwise.
     */
    public Body getBody(String className, String methodSubsig) {
        if (bodyMap.containsKey(className)) {
            Map<String, Body> map = bodyMap.get(className);
            if (map.containsKey(methodSubsig)) {
                return map.get(methodSubsig);
            }
        }
        LOG.error("Cannot find method '" + methodSubsig + "' in class " + className);
        return null;
    }

    /**
     * Load all the methods for a given class name.
     *
     * @param className The name of the class to be loaded
     * @return The Soot representation of the class
     */
    public SootClass loadSootClassMethods(String className) {
        SootClass cls = Scene.v().loadClassAndSupport(className);
        for (SootMethod method : cls.getMethods()) {
            if (!method.hasActiveBody() && !cls.isInterface()) {
                Body methodBody = getBody(className, method.getSubSignature());
                //assertNotNull(methodBody);
                method.setActiveBody(methodBody);
            }
        }
        return cls;
    }

}
