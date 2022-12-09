package runtime.fate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class InjectionManager {
    private static final Logger LOG = LoggerFactory.getLogger(InjectionManager.class);

    public final String trialsPath, injectionResultPath;
    private final AtomicBoolean injected = new AtomicBoolean(false);

    public final ConcurrentMap<InjectionPoint, Object> injectionSet = new ConcurrentHashMap<>();
    private InjectionPoint grant = null;


    public boolean inject(final String func, final String file, final int stacktrace, final int pid) {
        if (injected.get()) {
            return false;
        }
        final InjectionPoint injection = new InjectionPoint(func, file, stacktrace, pid);
        if (!injectionSet.containsKey(injection)) {
            if (injected.compareAndSet(false, true)) {
                grant = injection;
                return true;
            }
        }
        return false;
    }

    public InjectionManager(final String trialsPath, final String injectionResultPath) throws Exception {
        this.trialsPath = trialsPath;
        this.injectionResultPath = injectionResultPath;
        for (final File file : new File(this.trialsPath).listFiles((file, name) -> name.endsWith(".json"))) {
            injectionSet.put(new InjectionPoint(file), new Object());
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
        public final String func;
        public final String file;
        public final int stacktrace;
        public final int pid;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InjectionPoint)) return false;
            InjectionPoint that = (InjectionPoint) o;
            return stacktrace == that.stacktrace && pid == that.pid && func.equals(that.func) && file.equals(that.file);
        }

        @Override
        public int hashCode() {
            return Objects.hash(func, file, stacktrace, pid);
        }

        public InjectionPoint(String func, String file, int stacktrace, int pid) {
            this.func = func;
            this.file = file;
            this.stacktrace = stacktrace;
            this.pid = pid;
        }

        public InjectionPoint(final File file) throws IOException {
            try (final InputStream inputStream = Files.newInputStream(file.toPath());
                 final JsonReader reader = Json.createReader(inputStream)) {
                final JsonObject json = reader.readObject();
                this.func = json.getString("func");
                this.file = json.getString("file");
                this.stacktrace = json.getInt("stacktrace");
                this.pid = json.getInt("node");
            }
        }

        public JsonObjectBuilder dump() {
            return Json.createObjectBuilder()
                    .add("func", func)
                    .add("file", file)
                    .add("stacktrace", stacktrace)
                    .add("node", pid);
        }
    }
}
