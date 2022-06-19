package analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import analyzer.option.AnalyzerOptions;
import analyzer.option.OptionParser;
import analyzer.phase.PhaseManager;
import index.IndexManager;
import index.ProgramLocation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.tagkit.LineNumberTag;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AnalyzerTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyzerTestBase.class);
    protected static AnalyzerMain analyzer;
    protected static TestHelper helper;
    public static Map<String, SootClass> classes = new TreeMap<>();
    public static Map<SootClass, Map<SootMethod, Map<Unit, ProgramLocation>>> index = new HashMap<>();
    public static Map<SootMethod, Map<Integer, Unit>> methodUnitIds = new HashMap<>();
    public static Map<IndexManager.LogEntry, ProgramLocation> logEntries = new HashMap<>();

    @BeforeAll
    public static void setup() throws Exception {
        LOG.info("AnalyzerTestBase setup");
        if (helper != null) {
            // Although Soot offers the G.reset API to support running an analysis multiple times,
            // when it is run more than a few (e.g., 6), we encounter problems that the TestHelper
            // can no longer extract any Soot class! In other words, the Scene.v().getApplicationClasses()
            // will return empty, which makes a test fail meaninglessly.
            //
            // Since we only need the TestHelper to give us the Soot class, we can just run it once
            // if we supply all the test classes upfront.
            //LOG.info("TestHelper has already been run...");
            return;
        }
        helper = new TestHelper();

        // Need to put ../common/target/classes as indir as well to load the McGrayAgent class
        String[] args = {"-o", "sootTestOutput", "-i",
                "target/test-classes",
                "-a", TestHelper.PHASE_INFO.getFullName(), "-w",
                "-p", "jb", "use-original-names:true"};
        //String[] args = {"-o", "sootTestOutput", "-i",
              //  "/Users/panjia/Desktop/flaky-reproduction/tool/analyzer/target/test-classes",
              //  "-a","wjtp.flaky" , "-e",
              //  "-p", "jb", "use-original-names:true"};


        // Chang Lou's comments on why we need to use a TestHelper phase:
        //
        // we execute main analyzer to load our test recipes into sootclass
        // however, some info would be missing after the whole process is finished,
        // e.g. java.lang.RuntimeException: No method source set for method xxx
        // we either save the info when we still have it, or we transform test case as a transformer
        // and run it, we chose the first one due to implementation complexity
        //PhaseManager.getInstance().registerAnalysis(new TestHelper(), TestHelper.PHASE_INFO);
        PhaseManager.getInstance().addPhaseInfo(TestHelper.PHASE_INFO);
        OptionParser parser = new OptionParser();
        AnalyzerOptions options = parser.parse(args);
        AnalyzerMain main = new AnalyzerMain(options){
            @Override
            protected void registerAnalyses() {
                PhaseManager.getInstance().registerAnalysis(new TestHelper(), TestHelper.PHASE_INFO);
                //LOG.info("Neglected");
            }
        };

        assertTrue(main.initialize());

        // The enabled analyses at this point must be only containing the TestHelper
        assertEquals(PhaseManager.getInstance().enabledAnalyses().size(), 1);
        assertTrue(PhaseManager.getInstance().enabledAnalyses().contains(TestHelper.PHASE_INFO.getFullName()));

        // Run the TestHelper analysis to retrieve the method body correctly.
        assertTrue(main.run());
        String prefix = "analyzer.cases.";
        for (final SootClass sootClass : Scene.v().getApplicationClasses()) {
            //System.out.println(sootClass.toString());
            if (sootClass.getName().startsWith(prefix)) {
                classes.put(sootClass.getName(), sootClass);
                //Load Active Bodies from TestHelper
                helper.loadSootClassMethods(sootClass.getName());
                final Map<SootMethod, Map<Unit, ProgramLocation>> maps = new HashMap<>();
                index.put(sootClass, maps);
                final String shortClassName = sootClass.getName().substring(sootClass.getName().lastIndexOf('.') + 1);
                for (final SootMethod sootMethod : sootClass.getMethods()) {
                    if (sootMethod.hasActiveBody()) {
                        //System.out.println(sootMethod.toString());
                        final Map<Unit, ProgramLocation> locations = new HashMap<>();
                        maps.put(sootMethod, locations);
                        final Map<Integer, Unit> unitIds = new HashMap<>();
                        methodUnitIds.put(sootMethod, unitIds);
                        int id = 0;
                        for (final Unit unit : sootMethod.getActiveBody().getUnits()) {
                            //helper.getBody(sootClass.getName(), sootMethod.getSubSignature()).getUnits()) {
                            //System.out.println(unit.toString());
                            final ProgramLocation loc = new ProgramLocation(sootClass, sootMethod, unit, id);
                            locations.put(unit, loc);
                            unitIds.put(id, unit);
                            id++;
                            for (final ValueBox valueBox : unit.getUseBoxes()) {
                                final Value value = valueBox.getValue();
                                //System.out.println(value.toString());
                                if (value instanceof InvokeExpr) {
                                    //System.out.println("Invoke!");
                                    //System.out.println(((InvokeExpr) value).getMethod().toString());
                                    final SootMethod log = ((InvokeExpr) value).getMethod();
                                    //System.out.println(log.getDeclaringClass().getName());
                                    //System.out.println(getLine(unit));
                                    final String name = log.getDeclaringClass().getName();
                                    if (name.equals("org.apache.commons.logging.Log") ||
                                            name.equals("org.slf4j.Logger")) {
                                        switch (log.getName()) {
                                            case "error":
                                            case "info":
                                            case "warn":
                                            case "debug":
                                                logEntries.put(new IndexManager.LogEntry(shortClassName, getLine(unit)), loc);
                                            default:
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static int getLine(Unit unit) {
        final LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
        if (tag != null) {
            return tag.getLineNumber();
        }
        return -1;
    }

    @AfterAll
    public static void teardown() {
        LOG.info("GrayAnalyzerTestBase teardown");
        // analyzer.reset();
    }

}
