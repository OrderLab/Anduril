package reporter.check;

import feedback.symptom.Symptoms;
import feedback.log.Log;


import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.Set;
import java.util.TreeSet;

public class Checker {

    public final Set<Integer> targetSet = new TreeSet<>();
    public final JsonObject spec;
    public final String name;

    public Checker(JsonObject spec) {
        this.spec = spec;
        InjectionLocationMatcher matcher = new InjectionLocationMatcher(this.spec);
        final JsonArray arr = this.spec.getJsonArray("injections");
        for (int i = 0; i < arr.size(); i++) {
            final JsonObject injection = arr.getJsonObject(i);
            final int injectionId = injection.getInt("id");
            if (matcher.match(injection)) {
                targetSet.add(injectionId);
            }
        }
        name = null;
    }

    public Checker(String name) {
        spec = null;
        this.name = name;
    }

    public boolean checkTrial(Log trial, int injectionId) {
        return targetSet.contains(injectionId) && Symptoms.hasResultEvent(trial, spec);
    }

    public boolean checkBaselineTrial(Log trial) {
        return Symptoms.hasResultEvent(trial, name);
    }
}
