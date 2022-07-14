package analyzer.event;

import index.IndexManager;
import analyzer.AnalyzerTestBase;
import analyzer.analysis.AnalysisInput;
import analyzer.analysis.AnalysisManager;
import analyzer.cases.eventManager.EventGraphExample;
import index.ProgramLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import soot.*;
import soot.jimple.InvokeExpr;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EventManagerTest extends AnalyzerTestBase {

    public static AnalysisManager analysisManager;


    @BeforeAll
    public static void makingIntraProceduralAnalysis() {
        AnalysisInput analysisInput = new AnalysisInput(new IndexManager(index,classes));
        analysisManager = new AnalysisManager(analysisInput);
    }

    ProgramEvent findSymptom1(){
        SootClass testClass = classes.get(EventGraphExample.class.getName());
        SootMethod testMethod = testClass.getMethod("void run()");
        for (final ProgramLocation location : index.get(testClass).get(testMethod).values()) {
            for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                final Value value = valueBox.getValue();
                if (value instanceof InvokeExpr) {
                    //System.out.println(value);
                    final SootMethod inv = ((InvokeExpr) value).getMethod();
                    if (inv.getDeclaringClass().getName().equals("java.lang.AssertionError")) {
                        //System.out.println("good"+location.unit);
                        return new LocationEvent(location);
                    }
                }
            }
        }
        return null;
    }

    @Test
    void allExternal() {
        ProgramEvent symptomEvent = findSymptom1();
        //Make symptom event!
        assertTrue(symptomEvent!=null);

        Set<ProgramLocation> logEvents = new HashSet<>();
        for (ProgramLocation p:logEntries.values()) {
            System.out.println(p.unit.toString());
            logEvents.add(p);
        }
        EventGraph eventGraph = new EventGraph(analysisManager,symptomEvent,logEvents);
        for (EventGraph.Node p:eventGraph.bfs()) {
            System.out.println(p.depth);
            System.out.println(p.event.toString());
        }
    }



}