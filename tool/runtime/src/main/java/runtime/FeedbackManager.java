package runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;

public class FeedbackManager {
    private static final Logger LOG = LoggerFactory.getLogger(FeedbackManager.class);
    private final JsonObject json;
    public FeedbackManager(final JsonObject json) {
        this.json = json;
    }

    private final Map<Integer, Integer> active = new TreeMap<>();
    public void activate(final int id) {
        active.merge(id, -1, Integer::sum);
    }

    public final Set<Integer> allowSet = new TreeSet<>();
    public boolean ifAllowed(final int injectionId) {
        return allowSet.contains(injectionId);
    }

    public void calc(final int windowSize) {
        final Map<Integer, Map<Integer, ArrayList<Integer>>> injections = new TreeMap<>();
        final JsonArray injections_json = this.json.getJsonArray("injections");
        if (injections_json.size() <= windowSize) {
            for (int i = 0; i < injections_json.size(); i++) {
                final JsonObject spec = injections_json.getJsonObject(i);
                final int injectionId = spec.getInt("id");
                allowSet.add(injectionId);
            }
            return;
        }
        for (int i = 0; i < injections_json.size(); i++) {
            final JsonObject spec = injections_json.getJsonObject(i);
            final int injectionId = spec.getInt("id");
            final int caller = spec.getInt("caller");
            final int callee = spec.getInt("callee");
            if (!injections.containsKey(caller)) {
                injections.put(caller, new TreeMap<>());
            }
            final Map<Integer, ArrayList<Integer>> map = injections.get(caller);
            if (!map.containsKey(callee)) {
                map.put(callee, new ArrayList<>());
            }
            map.get(callee).add(injectionId);
        }

        final int startNumber = json.getInt("start");
        final int[] startValues = new int[startNumber];
        final int[] starts = new int[startNumber];
        for (int i = 0; i < startNumber; i++) {
            starts[i] = i;
            startValues[i] = active.getOrDefault(i, 0);
        }
        for (int i = 0; i < startNumber; i++) {
            for (int j = i + 1; j < startNumber; j++) {
                if (startValues[starts[i]] > startValues[starts[j]]) {
                    int tmp = starts[j];
                    starts[j] = starts[i];
                    starts[i] = tmp;
                }
            }
        }

        final Map<Integer, ArrayList<Integer>> graph = new TreeMap<>();
        final JsonArray graph_json = this.json.getJsonArray("tree");
        for (int i = 0; i < graph_json.size(); i++) {
            final JsonObject spec = graph_json.getJsonObject(i);
            final int node = spec.getInt("id");
            final ArrayList<Integer> nodes = new ArrayList<>();
            graph.put(node, nodes);
            final JsonArray children = spec.getJsonArray("children");
            for (int j = 0; j < children.size(); j++) {
                nodes.add(children.getInt(j));
            }
        }

        final LinkedList<Integer> queue = new LinkedList<>();
        final LinkedList<Integer> weights = new LinkedList<>();
        queue.add(starts[0]);
        weights.add(startValues[starts[0]]);
        for (int i = 1; i < startNumber; i++) {
            if (startValues[starts[i]] == startValues[starts[0]]) {
                queue.add(starts[i]);
                weights.add(startValues[starts[i]]);
            }
        }
        int index = queue.size();
        final Set<Integer> visited = new TreeSet<>(queue);
        while (!queue.isEmpty()) {
            final int node = queue.getFirst();
            final int weight = weights.getFirst() + 1;
            queue.removeFirst();
            weights.removeFirst();
            if (graph.containsKey(node)) {
                while (index < startNumber && startValues[starts[index]] == weight) {
                    queue.add(starts[index]);
                    weights.add(startValues[starts[index]]);
                    index++;
                }
                final Map<Integer, ArrayList<Integer>> m1 = injections.get(node);
                for (final Integer child : graph.get(node)) {
                    if (!visited.contains(child)) {
                        visited.add(child);
                        queue.add(child);
                        weights.add(weight);
                    }
                    if (m1 != null && m1.containsKey(child)) {
                        for (final Integer injectionId : m1.get(child)) {
                            allowSet.add(injectionId);
                            if (allowSet.size() == windowSize) {
                                return;
                            }
                        }
                    }
                }
            }
            if (queue.isEmpty() && index < startNumber) {
                queue.add(starts[index]);
                weights.add(startValues[starts[index]]);
                for (int i = index + 1; i < startNumber; i++) {
                    if (startValues[starts[i]] == startValues[starts[index]]) {
                        queue.add(starts[i]);
                        weights.add(startValues[starts[i]]);
                    }
                }
                index += queue.size();
            }
        }
    }
}
