package analyzer.analysis;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalIntraProceduralAnalysis {
    public final Map<SootClass, Map<SootMethod, IntraProceduralAnalysis>> intraproceduralAnalyses = new HashMap<>();

    GlobalIntraProceduralAnalysis(List<SootClass> classes) {
        for (final SootClass sc : classes) {
            analyzeClass(sc);
        }
    }

    private void analyzeClass(final SootClass sootClass) {
        final Map<SootMethod, IntraProceduralAnalysis> result = new HashMap<>();
        intraproceduralAnalyses.put(sootClass, result);
        for (final SootMethod method : sootClass.getMethods()) {
            if (method.hasActiveBody()) {
                final Body body = method.getActiveBody();
                final IntraProceduralAnalysis a = new IntraProceduralAnalysis(method, body);
                result.put(method, a);
            }
//            if (sootClass.getName()
//                    .equals("org.apache.zookeeper.server.quorum.Leader$LearnerCnxAcceptor$LearnerCnxAcceptorHandler")) {
//
//            }
//            System.out.println(method.getName());
        }
    }

    public IntraProceduralAnalysis getAnalysis(final SootClass sootClass, final SootMethod sootMethod) {
        final Map<SootMethod, IntraProceduralAnalysis> map = intraproceduralAnalyses.get(sootClass);
        if (map == null) {
            return null;
        }
        return map.get(sootMethod);
    }
}
