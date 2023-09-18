package runtime.stacktrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class InjectionManager {
    private static final Logger LOG = LoggerFactory.getLogger(runtime.stacktrace.InjectionManager.class);

    public final String trialsPath, injectionResultPath;
    private final AtomicBoolean injected = new AtomicBoolean(false);
    protected final Map<Integer, String> injection2Stacktrace;

    public final ConcurrentMap<runtime.stacktrace.InjectionManager.InjectionPoint, Object> injectionSet = new ConcurrentHashMap<>();
    private runtime.stacktrace.InjectionManager.InjectionPoint grant = null;

    private final ConcurrentMap<Integer, AtomicInteger> id2times = new ConcurrentHashMap<>();

    public boolean inject(final int id, final int pid) {
        if (injected.get()) {
            return false;
        }
        // Check stack trace
        StackTraceElement[] es = Thread.currentThread().getStackTrace();
        StringBuilder s = new StringBuilder();
        for (StackTraceElement e : es) {
            s.append(e.toString()+",");
        }
        // Calculate occurence
        int occurrence = -1;
        if (s.toString().contains(injection2Stacktrace.get(id))) {
            AtomicInteger count = id2times.get(id);
            occurrence = count.addAndGet(1);
        } else {
            return false;
        }
        final runtime.stacktrace.InjectionManager.InjectionPoint injection = new runtime.stacktrace.InjectionManager.InjectionPoint(id, occurrence, pid);
        if (!injectionSet.containsKey(injection)) {
            if (injected.compareAndSet(false, true)) {
                grant = injection;
                return true;
            }
        }
        return false;
    }

    public InjectionManager(final String trialsPath, final String specPath, final String injectionResultPath) throws Exception {
        this.trialsPath = trialsPath;
        this.injectionResultPath = injectionResultPath;
        for (final File file : new File(this.trialsPath).listFiles((file, name) -> name.endsWith(".json"))) {
            injectionSet.put(new runtime.stacktrace.InjectionManager.InjectionPoint(file), new Object());
        }
        try (final InputStream inputStream = Files.newInputStream(Paths.get(specPath));
             final JsonReader reader = Json.createReader(inputStream)) {
            final JsonObject json = reader.readObject();
            final JsonArray injections_json = json.getJsonArray("injections");

            this.injection2Stacktrace = new HashMap<>();
            for (int i = 0; i < injections_json.size(); i++) {
                final JsonObject spec = injections_json.getJsonObject(i);
                final int injectionId = spec.getInt("id");
                final JsonArray stackTrace = spec.getJsonArray("stackTrace");
                StringBuilder s = new StringBuilder();
                for (int j = 0; j < stackTrace.size(); j++) {
                    s.append(stackTrace.getString(j)+",");
                }
                System.out.println(s);
                this.injection2Stacktrace.put(injectionId, s.toString());
                this.id2times.put(injectionId, new AtomicInteger(0));
            }
        }
    }

    private static final JsonWriterFactory writerFactory;

    static {
        final Map<String, Object> options = new HashMap<>();
        options.put(JsonGenerator.PRETTY_PRINTING,true);
        writerFactory = Json.createWriterFactory(options);
    }

    public void dump() {
        if (grant == null) {
            return;
        }
        try (final FileWriter fw = new FileWriter(this.injectionResultPath);
             final JsonWriter jsonWriter = writerFactory.createWriter(fw)) {
            jsonWriter.writeObject(grant.dump().build());
        } catch (final IOException e) {
            LOG.warn("error while writing injection json", e);
        }
    }

    static public final class InjectionPoint {
        public final int id;
        public final int occurrence;
        public final int pid;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof runtime.stacktrace.InjectionManager.InjectionPoint)) return false;
            runtime.stacktrace.InjectionManager.InjectionPoint that = (runtime.stacktrace.InjectionManager.InjectionPoint) o;
            return id == that.id && occurrence == that.occurrence && pid == that.pid;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, occurrence, pid);
        }

        public InjectionPoint(int id, int occurrence, int pid) {
            this.id = id;
            this.occurrence = occurrence;
            this.pid = pid;
        }

        public InjectionPoint(final File file) throws IOException {
            try (final InputStream inputStream = Files.newInputStream(file.toPath());
                 final JsonReader reader = Json.createReader(inputStream)) {
                final JsonObject json = reader.readObject();
                this.id = json.getInt("id");
                this.occurrence = json.getInt("occurrence");
                this.pid = json.getInt("pid");
            }
        }

        public JsonObjectBuilder dump() {
            return Json.createObjectBuilder()
                    .add("id", id)
                    .add("occurrence", occurrence)
                    .add("pid", pid);
        }
    }
}
