package analyzer.analysis;

import analyzer.AnalyzerTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionAnalysisTest extends AnalyzerTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalCallGraphAnalysisTest.class);

    public static GlobalCallGraphAnalysis callGraphAnalysis;

    private static List<SootClass> classList;

    @BeforeAll
    public static void makingCallGraphAnalysis() {
        LOG.info("ClassGraphAnalysis.....");
        classList = new LinkedList<>(classes.values());
        classList.sort(Comparator.comparing(SootClass::getName));
        callGraphAnalysis = new GlobalCallGraphAnalysis(classList);
    }

    @Test
    void pidan() {

        for (final SootClass sootClass : classList) {
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                if (sootMethod.hasActiveBody()) {
                    final Body body = sootMethod.getActiveBody();
                    final UnitGraph graph = new BriefUnitGraph(body);
                    System.out.println(graph.toString());
                }

            }
        }
    }
}