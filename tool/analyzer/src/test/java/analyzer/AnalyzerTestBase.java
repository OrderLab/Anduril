package analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import analyzer.option.AnalyzerOptions;
import analyzer.option.OptionParser;
import analyzer.phase.FlakyTestAnalyzer;
import analyzer.phase.PhaseManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzerTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyzerTestBase.class);
    protected static AnalyzerMain analyzer;
    protected static TestHelper helper;

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
            LOG.info("TestHelper has already been run...");
            return;
        }
        helper = new TestHelper();

        // Need to put ../common/target/classes as indir as well to load the McGrayAgent class
        String[] args = {"-o", "sootTestOutput", "-i",
                "/Users/panjia/Desktop/flaky-reproduction/tool/analyzer/target/test-classes",
                "-a", TestHelper.PHASE_INFO.getFullName(), "-e",
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
        PhaseManager.getInstance().addPhaseInfo(TestHelper.PHASE_INFO);
        OptionParser parser = new OptionParser();
        AnalyzerOptions options = parser.parse(args);
        AnalyzerMain main = new AnalyzerMain(options){
            @Override
            protected void registerAnalyses() {
                PhaseManager.getInstance().registerAnalysis(new TestHelper(),
                        TestHelper.PHASE_INFO);
            }
        };

        assertTrue(main.initialize());

        // The enabled analyses at this point must be only containing the TestHelper
        assertEquals(PhaseManager.getInstance().enabledAnalyses().size(), 1);
        assertTrue(PhaseManager.getInstance().enabledAnalyses().contains(TestHelper.PHASE_INFO.getFullName()));

        // Run the TestHelper analysis to retrieve the method body correctly.
        assertTrue(main.run());

    }

    @AfterAll
    public static void teardown() {
        LOG.info("GrayAnalyzerTestBase teardown");
        // analyzer.reset();
    }

}
