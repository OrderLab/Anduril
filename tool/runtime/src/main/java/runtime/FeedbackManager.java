package runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.graph.PriorityGraph;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;

public class FeedbackManager {
    private static final Logger LOG = LoggerFactory.getLogger(FeedbackManager.class);
    private final JsonObject json;
    protected final PriorityGraph graph;
    protected final ArrayList<Integer> injections;
    public FeedbackManager(final JsonObject json) {
        this.json = json;
        this.graph = new PriorityGraph(this.json);
        final JsonArray injections_json = this.json.getJsonArray("injections");
        this.injections = new ArrayList<>(injections_json.size());
        for (int i = 0; i < injections_json.size(); i++) {
            final JsonObject spec = injections_json.getJsonObject(i);
            final int injectionId = spec.getInt("id");
            this.injections.add(injectionId);
        }
    }

    protected final Map<Integer, Integer> active = new TreeMap<>();
    public void activate(final int id) {
        active.merge(id, -1, Integer::sum);
    }

    public final Set<Integer> allowSet = new TreeSet<>();
    // TODO: maybe concurrent set is better? without synchronized
    public synchronized boolean isAllowed(final int injectionId) {
        return allowSet.contains(injectionId);
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
        if (this.injections.size() <= windowSize) {
            this.allowSet.addAll(this.injections);
            return;
        }
        this.allowSet.clear();
        for (int i = 0; i < this.graph.startNumber; i++) {
            this.graph.setStartValue(i, this.active.getOrDefault(i, 0));
        }
        this.graph.calculatePriorities(injectionId -> {
            this.allowSet.add(injectionId);
            return allowSet.size() >= windowSize;
        });
    }
}
