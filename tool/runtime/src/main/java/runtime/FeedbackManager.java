package runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.graph.PriorityGraph;
import runtime.time.TimePriorityTable;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FeedbackManager {
    private static final Logger LOG = LoggerFactory.getLogger(FeedbackManager.class);
    private final JsonObject json;
    protected final PriorityGraph graph;
    protected final ArrayList<Integer> injections;
    protected final TimePriorityTable timePriorityTable;
    private static final int INF = 1_000_000_000;

    public FeedbackManager(final String specPath, final JsonObject json, final TimePriorityTable timePriorityTable) {
        this.json = json;
        this.graph = new PriorityGraph(specPath, this.json);
        this.timePriorityTable = timePriorityTable;
        final JsonArray injections_json = this.json.getJsonArray("injections");
        this.injections = new ArrayList<>(injections_json.size());
        for (int i = 0; i < injections_json.size(); i++) {
            final JsonObject spec = injections_json.getJsonObject(i);
            final int injectionId = spec.getInt("id");
            this.injections.add(injectionId);
        }
    }

    public FeedbackManager(final String specPath, final JsonObject json) {
        this(specPath, json, null);
    }

    protected final Map<Integer, Integer> active = new TreeMap<>();

    public void activate(final int id) {
        active.merge(id, - TraceAgent.config.feedbackDelta, Integer::sum);
    }

    public void deactivate(final int id) {
        active.merge(id, TraceAgent.config.feedbackDelta, Integer::sum);
    }

    public final Map<Integer, Integer> allowSet = new TreeMap<>();
    // TODO: maybe concurrent set is better? without synchronized
    public synchronized boolean isAllowed(final int injectionId) {
        return allowSet.getOrDefault(injectionId, 0) > 0;
    }

    public boolean isAllowed(final int pid, final int injectionId, final int occurrence) {
        LOG.error("Should not enter here");
        System.exit(-1);
        throw new RuntimeException("Should not enter here");
    }

    public boolean isAllowed(final int injectionId, final int occurrence) {
        LOG.error("Should not enter here");
        System.exit(-1);
        throw new RuntimeException("Should not enter here");
    }

    public void calc(final int windowSize) {
        if (timePriorityTable == null) {
            if (this.injections.size() <= windowSize) {
                for (final Integer injection : this.injections) {
                    this.allowSet.put(injection, INF);
                }
                return;
            }
            this.allowSet.clear();
            for (int i = 0; i < this.graph.startNumber; i++) {
                this.graph.setStartValue(i, this.active.getOrDefault(i, 0));
            }
            this.graph.calculatePriorities(injectionId -> {
                this.allowSet.put(injectionId, INF);
                return allowSet.size() >= windowSize;
            });
        } else {
            final AtomicInteger count = new AtomicInteger(0);
            this.allowSet.clear();
            for (int i = 0; i < this.graph.startNumber; i++) {
                this.graph.setStartValue(i, this.active.getOrDefault(i, 0));
            }
            this.graph.calculatePriorities(injectionId -> {
                int total = 0;
                if (this.timePriorityTable.distributed) {
                    for (int i = 0; i < timePriorityTable.nodes; i++) {
                        total += timePriorityTable.boundaries.getOrDefault(new TimePriorityTable.BoundaryKey(i, injectionId), 0);
                    }
                } else {
                    total += timePriorityTable.boundaries.getOrDefault(new TimePriorityTable.BoundaryKey(-1, injectionId), 0);
                }
                total = Math.min(total, TraceAgent.config.injectionOccurrenceLimit);
                if (total > 0) {
                    this.allowSet.put(injectionId, total);
                }
                return count.addAndGet(total) >= windowSize;
            });
        }
    }
}
