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
        try (final InputStream inputStream = new FileInputStream(this.injectionPointsPath);
             final JsonReader reader = Json.createReader(inputStream)) {
            final JsonObject json = reader.readObject();
            final JsonArray arr = json.getJsonArray("injections");
            for (int i = 0; i < arr.size(); i++) {
                final JsonObject spec = arr.getJsonObject(i);
                final Throwable exception = TraceAgent.createException(spec.getString("exception"));
                if (exception != null) {
                    id2exception.put(spec.getInt("id"), exception);
                }
            }
        } catch (final IOException ignored) {
        }
        final File[] files = new File(this.trialsPath).listFiles((file, name) -> name.endsWith(".json"));
        for (final File result : files) {
            try (final InputStream inputStream = new FileInputStream(result);
                 final JsonReader reader = Json.createReader(inputStream)) {
                final JsonObject json = reader.readObject();
                final InjectionIndex index = new InjectionIndex(json.getInt("id"),
                        json.getString("exception"), json.getInt("occurrence"));
                injectionSet.put(index, new Object());
            } catch (final IOException ignored) { }
        }
    }

    public void inject(final int id) throws Throwable {
        if (!injected.get()) {
            final Throwable exception = id2exception.get(id);
            if (exception != null) {
                // WARN: it assumes that each injection point (id) has distinct fault instance
                synchronized (exception) {
                    final int occurrence = id2times.getOrDefault(id, 0) + 1;
                    id2times.put(id, occurrence);
                    final InjectionIndex index = new InjectionIndex(id, exception.getClass().getName(), occurrence);
                    if (occurrence <= 3 && !injectionSet.containsKey(index)) {
                        if (injected.compareAndSet(false, true)) {
                            injectionPoint = index;
                            throw exception;
                        }
                    }
                }
            }
        }
    }

    public void dump() {
        if (injected.get()) {
            try (final FileWriter fw = new FileWriter(this.injectionResultPath);
                 final JsonWriter jsonWriter = writerFactory.createWriter(fw)) {
                jsonWriter.writeObject(injectionPoint.dump().build());
            } catch (final IOException ignored) {
            }
        }
    }
}
