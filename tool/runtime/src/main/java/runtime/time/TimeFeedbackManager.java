package runtime.time;

import runtime.FeedbackManager;

import javax.json.JsonObject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class TimeFeedbackManager extends FeedbackManager {
    protected final TimePriorityTable timePriorityTable;
    public TimeFeedbackManager(final JsonObject json, final String timePriorityTable) {
        super(json);
        try (final ObjectInputStream objectInputStream = new ObjectInputStream(
                Files.newInputStream(Paths.get(timePriorityTable)))) {
            this.timePriorityTable = (TimePriorityTable) objectInputStream.readObject();
        } catch (final IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.nodes = new Map[this.timePriorityTable.nodes];
        for (int i = 0; i < this.timePriorityTable.nodes; i++) {
            this.nodes[i] = new TreeMap<>();
        }
    }

    private long boundary;

    private final Map<Integer, long[]>[] nodes;

    @Override
    public boolean isAllowed(final int pid, final int injectionId, final int occurrence) {
        final long[] priorities = nodes[pid].get(injectionId);
        if (priorities == null) {
            return false;
        }
        if (occurrence > priorities.length) {
            return false;
        }
        return priorities[occurrence - 1] <= boundary;
    }

    private final Map<Integer, long[]> standalone = new TreeMap<>();

    @Override
    public boolean isAllowed(final int injectionId, final int occurrence) {
        final long[] priorities = standalone.get(injectionId);
        if (priorities == null) {
            return false;
        }
        if (occurrence > priorities.length) {
            return false;
        }
        return priorities[occurrence - 1] <= boundary;
    }

    @Override
    public void calc(final int windowSize) {
        for (int i = 0; i < super.graph.startNumber; i++) {
            final int finalI = i;
            super.graph.calculatePriorities(i, super.active.getOrDefault(i, 0), (injectionId, weight) -> {
                final Map<TimePriorityTable.Key, TimePriorityTable.UtilityReducer> injections =
                        this.timePriorityTable.injections.get(injectionId);
                if (injections != null) {
                    injections.forEach((k, v) -> v.add(finalI, weight));
                }
            });
        }
        final ArrayList<Long> priorities = new ArrayList<>(
                this.timePriorityTable.boundaries.values().stream().reduce(0, Integer::sum));
        if (this.timePriorityTable.distributed) {
            this.timePriorityTable.boundaries.forEach((k, v) -> this.nodes[k.pid].put(k.injection, new long[v]));
            this.timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) ->
                    this.nodes[k.pid].get(injection)[k.occurrence] = v.computeUtility(priorities)));
        } else {
            this.timePriorityTable.boundaries.forEach((k, v) -> this.standalone.put(k.injection, new long[v]));
            this.timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) ->
                    this.standalone.get(injection)[k.occurrence] = v.computeUtility(priorities)));
        }
        this.boundary = PriorityCalculator.kth(priorities, windowSize);
    }
}
