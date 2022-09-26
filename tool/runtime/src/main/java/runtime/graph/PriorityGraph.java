package runtime.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.time.TimePriorityTable;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PriorityGraph {
    private static final Logger LOG = LoggerFactory.getLogger(PriorityGraph.class);

    private final JsonObject json;
    protected final Map<Integer, Map<Integer, ArrayList<Integer>>> caller2callee2injections = new TreeMap<>();
    protected final Map<Integer, ArrayList<Integer>> outcome2cause = new TreeMap<>();
    private final int[] startValues;
    private final int[] starts;
    public final int startNumber;
    public final TreeMap<Integer, Integer> w = new TreeMap<>();

    protected PriorityGraph(final JsonObject json, final int startNumber) {
        this.json = json;
        this.startNumber = startNumber;
        this.startValues = new int[startNumber];
        this.starts = new int[startNumber];
    }

    public PriorityGraph(final JsonObject json) {
        this(json, json.getInt("start"));

        final JsonArray injectionsJson = this.json.getJsonArray("injections");
        for (int i = 0; i < injectionsJson.size(); i++) {
            final JsonObject spec = injectionsJson.getJsonObject(i);
            final int injectionId = spec.getInt("id");
            final int caller = spec.getInt("caller");
            final int callee = spec.getInt("callee");
            final Map<Integer, ArrayList<Integer>> map;
            if (caller2callee2injections.containsKey(caller)) {
                map = this.caller2callee2injections.get(caller);
            } else {
                map = new TreeMap<>();
                this.caller2callee2injections.put(caller, map);
            }
            if (!map.containsKey(callee)) {
                map.put(callee, new ArrayList<>());
            }
            map.get(callee).add(injectionId);
        }

        final JsonArray graphJson = this.json.getJsonArray("tree");
        for (int i = 0; i < graphJson.size(); i++) {
            final JsonObject spec = graphJson.getJsonObject(i);
            final int node = spec.getInt("id");
            final ArrayList<Integer> nodes = new ArrayList<>();
            this.outcome2cause.put(node, nodes);
            final JsonArray children = spec.getJsonArray("children");
            for (int j = 0; j < children.size(); j++) {
                nodes.add(children.getInt(j));
            }
        }
    }

    public void setStartValue(final int i, final int v) {
        this.startValues[i] = v;
    }

    public void calculatePriorities(final Predicate<Integer> terminator) {
        // the initialization must be here (not in the constructor) for the testing
        for (int i = 0; i < startNumber; i++) {
            starts[i] = i;
        }
        // O(n^2) is fine
        for (int i = 0; i < startNumber; i++) {
            for (int j = i + 1; j < startNumber; j++) {
                if (startValues[starts[i]] > startValues[starts[j]]) {
                    final int tmp = starts[j];
                    starts[j] = starts[i];
                    starts[i] = tmp;
                }
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
        w.clear();
        while (!queue.isEmpty()) {
            final int node = queue.getFirst();
            final int weight = weights.getFirst() + 1;
            queue.removeFirst();
            weights.removeFirst();
            if (this.outcome2cause.containsKey(node)) {
                while (index < startNumber && startValues[starts[index]] == weight) {
                    queue.add(starts[index]);
                    weights.add(startValues[starts[index]]);
                    index++;
                }
                final Map<Integer, ArrayList<Integer>> m1 = caller2callee2injections.get(node);
                for (final Integer child : outcome2cause.get(node)) {
                    if (!visited.contains(child)) {
                        visited.add(child);
                        queue.add(child);
                        weights.add(weight);
                    }
                    if (m1 != null && m1.containsKey(child)) {
                        for (final Integer injectionId : m1.get(child)) {
                            w.putIfAbsent(injectionId, weight);
                            if (terminator.test(injectionId)) {
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

    public void calculatePriorities(final int start, final int initialPriority,
                                    final BiConsumer<Integer, Integer> consumer) {
        final LinkedList<Integer> queue = new LinkedList<>();
        final LinkedList<Integer> weights = new LinkedList<>();
        queue.add(start);
        weights.add(initialPriority);
        final Set<Integer> visited = new TreeSet<>(queue);
        while (!queue.isEmpty()) {
            final int node = queue.getFirst();
            final int weight = weights.getFirst() + 1;
            queue.removeFirst();
            weights.removeFirst();
            if (this.outcome2cause.containsKey(node)) {
                final Map<Integer, ArrayList<Integer>> m1 = caller2callee2injections.get(node);
                for (final Integer child : outcome2cause.get(node)) {
                    if (!visited.contains(child)) {
                        visited.add(child);
                        queue.add(child);
                        weights.add(weight);
                    }
                    if (m1 != null && m1.containsKey(child)) {
                        for (final Integer injectionId : m1.get(child)) {
                            consumer.accept(injectionId, weight);
                        }
                    }
                }
            }
        }
    }

    // Find the event leading from start to end using depth first search
    public boolean findPath(int start, int end, int depth, int limit, final Set<Integer> visited, final Consumer<Integer> consumer) {
        if (depth == limit) {
            return false;
        }
        visited.add(start);
        if (start == end) {
            consumer.accept(end);
            return true;
        }
        if (!this.outcome2cause.containsKey(start)) {
            return false;
        }
        for (final Integer child : outcome2cause.get(start)) {
            if (!visited.contains(child) && findPath(child,end,depth+1, limit, visited,consumer)) {
                consumer.accept(start);
                return true;
            }

        }
        return false;
    }
}
