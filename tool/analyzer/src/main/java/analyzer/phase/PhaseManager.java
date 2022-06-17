package analyzer.phase;

import analyzer.util.SootUtils;

import java.util.*;

import soot.Transform;
import soot.Transformer;

/**
 * Manage all the analysis phases.
 */
public class PhaseManager {
    private Map<String, Transform> analysisMap;
    private Map<String, PhaseInfo> phaseInfoMap;
    private Set<String> enabledAnalysisSet;

    // Information about all the phases available in AutoWatchdog.
    private static PhaseInfo[] PHASES  = {
        FlakyTestAnalyzer.PHASE_INFO
    };
    private static PhaseManager instance;

    private PhaseManager() {
        analysisMap = new HashMap<>();
        phaseInfoMap = new HashMap<>();
        for (PhaseInfo info : PHASES) {
            phaseInfoMap.put(info.getFullName(), info);
        }
        enabledAnalysisSet = new HashSet<>();
    }

    public void addPhaseInfo (PhaseInfo toBeAdd) {
        PhaseInfo[] newPHASES = Arrays.copyOf(PHASES, PHASES.length + 1);
        newPHASES[newPHASES.length - 1] = toBeAdd;
        PHASES = newPHASES;
        phaseInfoMap.put(toBeAdd.getFullName(),toBeAdd);
    }

    public static PhaseManager getInstance() {
        if (instance == null) {
            instance = new PhaseManager();
        }
        return instance;
    }

    public Transform getAnalysis(String name) {
        return analysisMap.get(name);
    }

    public boolean isAnalysiEnabled(String name) {
        return enabledAnalysisSet.contains(name);
    }

    public boolean enableAnalysis(String name) {
        if (phaseInfoMap.containsKey(name)) {
            // Enable only it is available
            enabledAnalysisSet.add(name);
            return true;
        }
        return false;
    }

    public Set<String> enabledAnalyses() {
        return enabledAnalysisSet;
    }

    public PhaseInfo getPhaseInfo(String name) {
        return phaseInfoMap.get(name);
    }

    public Iterator<PhaseInfo> phaseInfoIterator() {
        return phaseInfoMap.values().iterator();
    }

    public Transform registerAnalysis(Transformer analysis, PhaseInfo info) {
        Transform phase = SootUtils.addNewTransform(info.getPack(), info.getFullName(), analysis);
        analysisMap.put(info.getFullName(), phase);
        phaseInfoMap.put(info.getFullName(), info);
        return phase;
    }
}
