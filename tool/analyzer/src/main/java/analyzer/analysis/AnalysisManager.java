package analyzer.analysis;

import analyzer.event.*;
import analyzer.instrument.TraceInstrumentor;
import index.ProgramLocation;
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public final class AnalysisManager {
    //private final Map<SootClass, Map<SootMethod, IntraProceduralAnalysis>> intraproceduralAnalyses = new HashMap<>();
    public final GlobalExceptionAnalysis exceptionAnalysis;
    public final GlobalSlicingAnalysis slicingAnalysis;
    public final GlobalCallGraphAnalysis callGraphAnalysis;
    public final AnalysisInput analysisInput;
    public final GlobalIntraProceduralAnalysis globalIntraProceduralAnalysis;

    private int injectionCounter = 0;
    public InjectionPoint createInjectionPoint(final ProgramEvent caller, final ProgramEvent callee,
                                               final ProgramLocation location) {
        assert analysisInput != null;
        // Only need the class and the method to decide the excluded point
        if (analysisInput.excludedPoint.containsKey(location.sootClass)
            && analysisInput.excludedPoint.get(location.sootClass).contains(location.sootMethod)) {
            return null;
        }
        return new InjectionPoint(caller, callee, location, injectionCounter++);
    }


    public void instrument() {
        if (analysisInput.distributedMode) {
            for (final SootClass c : analysisInput.mainClasses) {
                final SootMethod method = c.getMethodByName("main");
                final PatchingChain<Unit> units = method.retrieveActiveBody().getUnits();
                Unit head = units.getFirst();
                while (BasicBlockAnalysis.isLeadingStmt(head)) {
                    head = units.getSuccOf(head);
                }
                final StaticInvokeExpr initExpr =
                        Jimple.v().newStaticInvokeExpr(TraceInstrumentor.initMethod.makeRef(), new ArrayList<>());
                final InvokeStmt initStmt = Jimple.v().newInvokeStmt(initExpr);
                units.insertBefore(initStmt, head);
            }
        }
        try (final FileWriter writer = new FileWriter("blockmap.txt")) {
            for (final Map.Entry<SootClass, Map<SootMethod, IntraProceduralAnalysis>> m
                    : globalIntraProceduralAnalysis.intraProceduralAnalyses.entrySet()) {
                for (final Map.Entry<SootMethod, IntraProceduralAnalysis> a : m.getValue().entrySet()) {
                    final BasicBlockAnalysis b = a.getValue().basicBlockAnalysis;
                    new TraceInstrumentor(b).instrument();
                    writer.write(m.getKey().getName() + "," + a.getKey().getSubSignature() + ","
                            + b.counterStart + "," + b.counterEnd + "\n");
                }
            }
        } catch (final IOException e) {
            // ===
        }
    }

//Used for test
    public AnalysisManager(List<SootClass> classes) {
        this.analysisInput = null;
        this.callGraphAnalysis = new GlobalCallGraphAnalysis(classes);
        this.exceptionAnalysis = new GlobalExceptionAnalysis(classes, this.callGraphAnalysis);
        this.globalIntraProceduralAnalysis = new GlobalIntraProceduralAnalysis(classes);
        this.slicingAnalysis = new GlobalSlicingAnalysis(classes, this.callGraphAnalysis, this.globalIntraProceduralAnalysis);
    }


    public AnalysisManager(final AnalysisInput analysisInput) {
        this.analysisInput = analysisInput;
        this.callGraphAnalysis = new GlobalCallGraphAnalysis(analysisInput.classes);
        this.exceptionAnalysis = new GlobalExceptionAnalysis(analysisInput.classes, this.callGraphAnalysis);
        this.globalIntraProceduralAnalysis = new GlobalIntraProceduralAnalysis(analysisInput.classes);
        this.slicingAnalysis = new GlobalSlicingAnalysis(analysisInput.classes, this.callGraphAnalysis, this.globalIntraProceduralAnalysis);
//        SootClass c = Scene.v()
//                .getSootClass("org.apache.zookeeper.server.quorum.Leader$LearnerCnxAcceptor$LearnerCnxAcceptorHandler");
//        SootMethod m = c.getMethod("void acceptConnections()");
//        ExceptionHandlingAnalysis ea = exceptionAnalysis.analyses.get(m);
//        IntraProceduralAnalysis ipa = intraproceduralAnalyses.get(c).get(m);
//        for (final Unit unit : m.getActiveBody().getUnits()) {
//            System.out.println("" + unit + "      " + ipa.dominatorAnalysis.dominators.get(unit));
//        }
//        for (final SootClass e : ea.methodExceptions.keySet()) {
//            System.out.println(e.getName());
//            for (final Unit unit : ea.methodExceptions.get(e)) {
//                System.out.println(unit);
//                final Unit head = ipa.basicBlockAnalysis.heads.get(unit);
//                System.out.println(head);
//                final Unit block = ipa.basicBlockAnalysis.basicBlocks.get(head);
//                System.out.println(block);
//                System.out.println(ipa.dominatorAnalysis.dominators.get(block));
//                System.out.println(ipa.dominatorAnalysis.domSets.get(block).size());
//                for (final Unit u : m.getActiveBody().getUnits()) {
//                    if (u instanceof IfStmt) {
//                        if (((IfStmt) u).getTarget() == block) {
//                            System.out.println(ipa.dominatorAnalysis.domSets.get(u).size());
//                            System.out.println(u);
//                        }
//                    }
//                }
//            }
//        }
    }

//    public void reason(ProgramEvent e) {
//        System.out.println(((LocationEvent) e).locationUnit);
//        e.explainsWith(this);
//        e = ((LocationEvent) e).dominationLocations.get(0);
//        System.out.println(((ConditionEvent) e).locationUnit);
//        e.explainsWith(this);
//        System.out.println(((ConditionEvent) e).preconditionLocations.get(0));
//    }
//
//    public void reason2(InvocationEvent e) {
//        e.explainsWith(this);
//    }
}
