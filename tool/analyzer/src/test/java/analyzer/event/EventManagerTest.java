package analyzer.event;

import analyzer.AnalyzerTestBase;
import analyzer.analysis.AnalysisManager;
import analyzer.analysis.GlobalCallGraphAnalysis;
import analyzer.analysis.GlobalIntraProceduralAnalysis;
import analyzer.analysis.GlobalSlicingAnalysis;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import soot.SootClass;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventManagerTest extends AnalyzerTestBase {

    public static AnalysisManager analysisManager;
    public static EventGraph eventGraph;

    @BeforeAll
    public static void makingIntraProceduralAnalysis() {
        List<SootClass> classList = new LinkedList<>(classes.values());
        classList.sort(Comparator.comparing(SootClass::getName));
        analysisManager = new AnalysisManager(classList);


        //eventGraph = new EventGraph(analysisManager);
    }

    @Test
    void dummy() {

    }


}