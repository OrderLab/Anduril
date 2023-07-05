package runtime;

import javafx.util.Pair;
import runtime.time.TimePriorityTable;
import scala.Array;

import javax.json.JsonObject;
import java.util.*;
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
        if (allowMap.get(injectionId) == null) {
            return false;
        }
        return nodes[pid].get(injectionId)[occurrence - 1] <= allowMap.get(injectionId).getValue();
    }

    @Override
    public boolean isAllowed(final int injectionId, final int occurrence) {
        if (allowMap.get(injectionId) == null) {
            return false;
        }
        return standalone.get(injectionId)[occurrence - 1] <= allowMap.get(injectionId).getValue();
    }

    public void calc(final int windowSize, final int occurrenceSize) {
        this.allowMap.clear();
        for (int i = 0; i < this.graph.startNumber; i++) {
            this.graph.setStartValue(i, this.active.getOrDefault(i, 0));
        }
        this.graph.calculatePriorities((injectionId,sourceEvent) -> {
            Map<Integer, ArrayList<Integer>> buf = timePriorityTable.injection2Log2Time.get(injectionId);
            if (buf != null && buf.get(sourceEvent) != null) {
                if (buf.get(sourceEvent).size() >= occurrenceSize) {
                    this.allowMap.put(injectionId,
                            new Pair<>(sourceEvent, buf.get(sourceEvent).get(occurrenceSize - 1)));
                } else {
                    this.allowMap.put(injectionId, new Pair<>(sourceEvent, INF));
                }
            }
            return allowMap.size() >= windowSize;
        });

        if (this.timePriorityTable.distributed) {
            this.timePriorityTable.boundaries.forEach((k, v) -> this.nodes[k.pid].put(k.injection, newArray(v)));
            this.timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) -> {
                if (allowMap.get(injection) != null) {
                    this.nodes[k.pid].get(injection)[k.occurrence - 1] =
                            v.timePriorities.get(allowMap.get(injection).getKey());
                }
            }));
        } else {
            this.timePriorityTable.boundaries.forEach((k, v) -> this.standalone.put(k.injection, newArray(v)));
            this.timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) -> {
                if (allowMap.get(injection) != null) {
                    this.standalone.get(injection)[k.occurrence - 1] =
                            v.timePriorities.get(allowMap.get(injection).getKey());
                }
            }));
        }
    }

    private int[] newArray(int size) {
        int[] array = new int[size];
        Arrays.fill(array,INF);
        return array;
    }
}
