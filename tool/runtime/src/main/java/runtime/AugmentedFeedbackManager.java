package runtime;

import javafx.util.Pair;
import runtime.time.TimePriorityTable;
import scala.Array;

import javax.json.JsonObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

public class AugmentedFeedbackManager extends FeedbackManager {

    private static final int INF = 1_000_000_000;

    private final Map<Integer, int[]>[] nodes;

    private final Map<Integer, int[]> standalone = new TreeMap<>();

    private Map<Integer, Pair<Integer,Integer>> allowMap = new HashMap<>();

    public AugmentedFeedbackManager(final String specPath, final JsonObject json, final TimePriorityTable timePriorityTable) {
        super(specPath, json, timePriorityTable);
        this.nodes = new Map[super.timePriorityTable.nodes];
        for (int i = 0; i < super.timePriorityTable.nodes; i++) {
            this.nodes[i] = new TreeMap<>();
        }
    }

    @Override
    public boolean isAllowed(final int pid, final int injectionId, final int occurrence) {
        final double[] priorities = nodes[pid].get(injectionId);
        if (priorities == null) {
            return false;
        }
        if (occurrence > priorities.length) {
            return false;
        }
        return priorities[occurrence - 1] <= boundary;
    }

    @Override
    public boolean isAllowed(final int injectionId, final int occurrence) {
        final double[] priorities = standalone.get(injectionId);
        if (priorities == null) {
            return false;
        }
        if (occurrence > priorities.length) {
            return false;
        }
        return priorities[occurrence - 1] <= boundary;
    }

    public void calc(final int windowSize, final int occurrenceSize) {
        this.allowMap.clear();
        for (int i = 0; i < this.graph.startNumber; i++) {
            this.graph.setStartValue(i, this.active.getOrDefault(i, 0));
        }
        this.graph.calculatePriorities((injectionId,sourceEvent) -> {
            this.allowMap.put(injectionId,
                    new Pair<>(sourceEvent,timePriorityTable.injection2Log2Time.get(injectionId).get(sourceEvent).get(occurrenceSize)));
            return allowMap.size() >= windowSize;
        });

        if (this.timePriorityTable.distributed) {
            this.timePriorityTable.boundaries.forEach((k, v) -> this.nodes[k.pid].put(k.injection, new int[v]));
            this.timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) -> {

                this.nodes[k.pid].get(injection)[k.occurrence - 1] = priority;
            }));
        } else {
            Arrays.fill(new int[2],1);
            this.timePriorityTable.boundaries.forEach((k, v) -> this.standalone.put(k.injection, ));
            this.timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) -> {
                this.standalone.putIfAbsent(injection,).get(injection)[k.occurrence - 1] = priority;
                priorities.add(priority);
            }));
        }
    }

    private int[] newArray(int size) {

    }
}
