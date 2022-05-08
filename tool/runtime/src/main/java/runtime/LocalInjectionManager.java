package runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LocalInjectionManager {
    private static final Logger LOG = LoggerFactory.getLogger(LocalInjectionManager.class);

    private final String trialsPath, injectionPointsPath, injectionResultPath;
    private final AtomicBoolean injected = new AtomicBoolean(false);
    private volatile InjectionIndex injectionPoint = null;
    private final ConcurrentMap<InjectionIndex, Object> injectionSet = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Throwable> id2exception = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Integer> id2times = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Integer> thread2block = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Integer> block2times = new ConcurrentHashMap<>();
    private int trialId = 0;
    private int windowSize = TraceAgent.slidingWindowSize;
    private FeedbackManager feedbackManager = null;

    private static final JsonWriterFactory writerFactory;

    static {
        final Map<String, Object> options = new HashMap<>();
        options.put(JsonGenerator.PRETTY_PRINTING,true);
        writerFactory = Json.createWriterFactory(options);
    }

    public LocalInjectionManager(final String trialsPath,
                                 final String injectionPointsPath,
                                 final String injectionResultPath) {
        this.trialsPath = trialsPath;
        this.injectionPointsPath = injectionPointsPath;
        this.injectionResultPath = injectionResultPath;
        int[] injectionIds = null;
        try (final InputStream inputStream = new FileInputStream(this.injectionPointsPath);
             final JsonReader reader = Json.createReader(inputStream)) {
            final JsonObject json = reader.readObject();
            final JsonArray arr = json.getJsonArray("injections");
            injectionIds = new int[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                final JsonObject spec = arr.getJsonObject(i);
                final int injectionId = spec.getInt("id");
                injectionIds[i] = injectionId;
                final Throwable exception = TraceAgent.createException(spec.getString("exception"));
                if (exception != null) {
                    id2exception.put(injectionId, exception);
                }
            }
            feedbackManager = new FeedbackManager(json);
        } catch (final IOException e) {
            LOG.error("Error while loading files", e);
            System.exit(-1);
        }
        final File[] files = new File(this.trialsPath).listFiles((file, name) -> name.endsWith(".json"));
        int latestOK = -2; // avoid -1 + 1 == 0
        for (final File result : files) {
            try (final InputStream inputStream = new FileInputStream(result);
                 final JsonReader reader = Json.createReader(inputStream)) {
                final JsonObject json = reader.readObject();
                final int trialId = json.getInt("trial_id");
                if (trialId >= this.trialId) {
                    this.trialId = trialId + 1;
                    this.windowSize = json.getInt("window");
                }
                if (!json.containsKey("id")) {
                    if (latestOK < trialId) {
                        latestOK = trialId;
                    }
                    continue;
                }
                if (TraceAgent.allowFeedback) {
                    final JsonArray events = json.getJsonArray("feedback");
                    for (int i = 0; i < events.size(); i++) {
                        feedbackManager.activate(events.getInt(i));
                    }
                }
                final int block = json.getInt("block");
                final InjectionIndex index = new InjectionIndex(json.getInt("id"),
                        json.getString("exception"), json.getInt("occurrence"), block);
                injectionSet.put(index, new Object());
            } catch (final IOException ignored) { }
        }
        if (latestOK + 1 == this.trialId) {
            windowSize *= 2;
            if (windowSize > INF) {
                windowSize = INF;
            }
        }
        feedbackManager.calc(windowSize);
        LOG.info("injection allow set: {}", feedbackManager.allowSet);
    }

    static private final int INF = 1000000000; // largest id

    public void inject(final int id, final int blockId) throws Throwable {
        if (!injected.get()) {
            if (!feedbackManager.ifAllowed(id)) {
                return;
            }
            final Throwable exception = id2exception.get(id);
            if (exception != null) {
                // WARN: it assumes that each injection point (id) has distinct fault instance
                synchronized (exception) {
                    final int occurrence = id2times.getOrDefault(id, 0) + 1;
                    id2times.put(id, occurrence);
                    if (!TraceAgent.avoidBlockMode) {
                        if (block2times.containsKey(blockId)) {
                            return;
                        }
                        block2times.put(blockId, 1);
                    }
                    final InjectionIndex index = new InjectionIndex(id, exception.getClass().getName(), occurrence, blockId);
                    if (occurrence <= TraceAgent.injectionOccurrenceLimit && !injectionSet.containsKey(index)) {
                        if (injected.compareAndSet(false, true)) {
                            injectionPoint = index;
                            throw exception;
                        }
                    }
                }
            }
        }
    }

    public void trace(final int id) {
        thread2block.put(System.identityHashCode(Thread.currentThread()), id);
    }

    public void dump() {
        final JsonObjectBuilder json;
        if (injected.get()) {
            json = injectionPoint.dump();
        } else {
            json = Json.createObjectBuilder();
        }
        json.add("trial_id", this.trialId).add("window", windowSize);
        try (final FileWriter fw = new FileWriter(this.injectionResultPath);
             final JsonWriter jsonWriter = writerFactory.createWriter(fw)) {
            jsonWriter.writeObject(json.build());
        } catch (final IOException ignored) {
        }
    }
}
