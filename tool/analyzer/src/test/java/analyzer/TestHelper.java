package analyzer;


import analyzer.phase.PhaseInfo;
import index.IndexManager;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Scene;
import soot.SceneTransformer;


/**
 * The helper class is used to load Soot body info for test cases reasons explained in GrayAnalyzerTestBase
 */
public class TestHelper extends SceneTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(TestHelper.class);

    public static final PhaseInfo PHASE_INFO = new PhaseInfo("wjtp", "testhelper",
            "Store body info for later analyses", true, false);

    public IndexManager indexManager;

    protected void internalTransform(String phaseName, Map<String, String> options) {
        LOG.info("TestHelper running...");
        this.indexManager = new IndexManager(Scene.v().getApplicationClasses(),
                "analyzer.cases.");
    }

}
