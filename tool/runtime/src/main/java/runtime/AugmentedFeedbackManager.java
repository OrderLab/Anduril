package runtime;

import runtime.time.TimePriorityTable;

import javax.json.JsonObject;
import java.io.Serializable;
import java.util.*;

public class AugmentedFeedbackManager extends FeedbackManager {

    private static final int INF = 1_000_000_000;

    private final Map<Integer, int[]>[] nodes;

    private final Map<Integer, int[]> standalone = new TreeMap<>();


    public final static class AllowValue implements Serializable {
        public final int log, priority;
        public AllowValue(final int log, final int priority) {
            this.log = log;
            this.priority = priority;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AugmentedFeedbackManager.AllowValue)) return false;
            AugmentedFeedbackManager.AllowValue that = (AugmentedFeedbackManager.AllowValue) o;
            return log == that.log && priority == that.priority;
        }

        @Override
        public int hashCode() {
            return Objects.hash(log, priority);
        }
    }


    public final Map<Integer, AugmentedFeedbackManager.AllowValue> allowMap = new HashMap<>();

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
        if (occurrence > nodes[pid].get(injectionId).length) {
            return false;
        }
        return nodes[pid].get(injectionId)[occurrence - 1] <= allowMap.get(injectionId).priority;
    }

    @Override
    public boolean isAllowed(final int injectionId, final int occurrence) {
        if (allowMap.get(injectionId) == null) {
            return false;
        }
        if (occurrence > standalone.get(injectionId).length) {
            return false;
        }
        return standalone.get(injectionId)[occurrence - 1] <= allowMap.get(injectionId).priority;
    }

    @Override
    public synchronized boolean isAllowed(final int injectionId) {
        return allowMap.containsKey(injectionId);
    }

    public void calc(final int windowSize, final int occurrenceSize, final Map<Integer,Integer> id2Occur, final int maxTry) {
        this.allowMap.clear();
        for (int i = 0; i < this.graph.startNumber; i++) {
            this.graph.setStartValue(i, this.active.getOrDefault(i, 0));
        }
        this.graph.calculatePriorities((injectionId,sourceEvent) -> {
            // TODO: What about the case where one injection point has same distances to two different logs?
            if (!allowMap.containsKey(injectionId)) {
                if (!(id2Occur.containsKey(injectionId) && id2Occur.get(injectionId) == maxTry)) {
                    Map<Integer, ArrayList<Integer>> buf = timePriorityTable.injection2Log2Time.get(injectionId);
                    if (buf != null && buf.get(sourceEvent) != null) {
                        if (buf.get(sourceEvent).size() >= occurrenceSize) {
                            this.allowMap.put(injectionId,
                                    new AugmentedFeedbackManager.AllowValue(sourceEvent, buf.get(sourceEvent).get(occurrenceSize - 1)));
                        } else {
                            this.allowMap.put(injectionId, new AugmentedFeedbackManager.AllowValue(sourceEvent, INF));
                        }
                    }
                }
            }
            return allowMap.size() >= windowSize;
        });

        if (this.timePriorityTable.distributed) {
            this.timePriorityTable.boundaries.forEach((k, v) -> this.nodes[k.pid].put(k.injection, newArray(v)));
            this.timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) -> {
                if (allowMap.get(injection) != null) {
                    this.nodes[k.pid].get(injection)[k.occurrence - 1] =
                            v.timePriorities.get(allowMap.get(injection).log);
                }
            }));
        } else {
            this.timePriorityTable.boundaries.forEach((k, v) -> this.standalone.put(k.injection, newArray(v)));
            this.timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) -> {
                if (allowMap.get(injection) != null) {
                    this.standalone.get(injection)[k.occurrence - 1] =
                            v.timePriorities.get(allowMap.get(injection).log);
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
