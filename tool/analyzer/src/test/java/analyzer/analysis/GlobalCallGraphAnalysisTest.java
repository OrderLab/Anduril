package analyzer.analysis;

import analyzer.AnalyzerTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalCallGraphAnalysisTest  extends AnalyzerTestBase {

    @Test
    void testSimple() {
        System.out.print("Pidan");
    }
}