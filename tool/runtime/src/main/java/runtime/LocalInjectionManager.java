package runtime;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LocalInjectionManager {

    private final String trialsPath, injectionPointsPath, injectionResultPath;
    private final AtomicBoolean injected = new AtomicBoolean(false);
    private volatile InjectionIndex injectionPoint = null;
    private final ConcurrentMap<InjectionIndex, Object> injectionSet = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Throwable> id2exception = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Integer> id2times = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Integer> thread2block = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Integer> block2times = new ConcurrentHashMap<>();
    private int trialId = 0;
    private int windowSize = 10;
    private int threshold = -1;

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
        } catch (final IOException ignored) {
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
                final int block = json.getInt("block");
                final InjectionIndex index = new InjectionIndex(json.getInt("id"),
                        json.getString("exception"), json.getInt("occurrence"), block);
                injectionSet.put(index, new Object());
            } catch (final IOException ignored) { }
        }
        if (latestOK + 1 == this.trialId) {
            windowSize *= 2;
        }
        if (injectionIds != null) {
            if (windowSize > injectionIds.length) {
                this.threshold = injectionIds[injectionIds.length - 1];
            } else {
                this.threshold = INF;
            }
        }
    }

    static private final int INF = 1000000000; // largest id

    public void inject(final int id) throws Throwable {
        if (!injected.get()) {
            if (id > this.threshold) {
                return;
            }
            final Throwable exception = id2exception.get(id);
            if (exception != null) {
                // WARN: it assumes that each injection point (id) has distinct fault instance
                synchronized (exception) {
                    final int occurrence = id2times.getOrDefault(id, 0) + 1;
                    id2times.put(id, occurrence);
                    final int block = thread2block.get(System.identityHashCode(Thread.currentThread()));
                    if (!TraceAgent.avoidBlockMode) {
                        if (block2times.containsKey(block)) {
                            return;
                        }
                        block2times.put(block, 1);
                    }
                    final InjectionIndex index = new InjectionIndex(id, exception.getClass().getName(), occurrence, block);
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
