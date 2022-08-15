package reporter.check;

import feedback.Symptoms;
import feedback.parser.DistributedLog;
import feedback.parser.Log;
import feedback.time.InjectionRequestRecord;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.Set;
import java.util.TreeSet;

public class Checker {

    public final Set<Integer> targetSet = new TreeSet<>();
    public final JsonObject spec;

    public Checker(JsonObject spec) {
        this.spec = spec;
        InjectionLocationMatcher matcher = new InjectionLocationMatcher(this.spec);
        final JsonArray arr = this.spec.getJsonArray("injections");
        for (int i = 0; i < arr.size(); i++) {
            final JsonObject injection = arr.getJsonObject(i);
            final int injectionId = injection.getInt("id");
            final JsonObject loc = injection.getJsonObject("location");
            if (matcher.match(loc)) {
                targetSet.add(injectionId);
            }
        }
    }

    public boolean checkTrial(DistributedLog trial, int injectionId) {
        return targetSet.contains(injectionId) && Symptoms.checkSymptom(trial, this.spec);
    }




}
