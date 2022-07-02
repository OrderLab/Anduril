package runtime.baseline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class InjectionManager {
    private static final Logger LOG = LoggerFactory.getLogger(runtime.baseline.InjectionManager.class);

    public final ConcurrentMap<InjectionPoint, Object> injectionSet = new ConcurrentHashMap<>();
    public final String trialsPath, injectionResultPath;

    private enum Policy {
        RANDOM, EXHAUSTIVE
    }
    private final Policy policy;
    private final double probability = Double.parseDouble(System.getProperty("baseline.probability", "0"));
    private final AtomicBoolean injected = new AtomicBoolean(false);
    private InjectionPoint grant = null;

    public InjectionManager(final String trialsPath, final String injectionResultPath) throws Exception {
        this.trialsPath = trialsPath;
        this.injectionResultPath = injectionResultPath;
        switch (System.getProperty("baseline.policy")) {
            case "random"     : this.policy = Policy.RANDOM; break;
            case "exhaustive" : this.policy = Policy.EXHAUSTIVE; break;
            default           : throw new Exception("invalid baseline policy");
        }
        for (final File file : new File(this.trialsPath).listFiles((file, name) -> name.endsWith(".json"))) {
            injectionSet.put(new InjectionPoint(file), new Object());
        }
    }

    public boolean inject(final int pid, final int id, final int occurrence, final String className, final String methodName,
                          final String invocationName, final int line, final String exceptionName) throws RemoteException {
        if (injected.get()) {
            return false;
        }
        switch (policy) {
            case RANDOM: {
                if (Math.random() < probability) {
                    if (injected.compareAndSet(false, true)) {
                        grant = new InjectionPoint(pid, id, occurrence, className, methodName, invocationName, line, exceptionName);
                        return true;
                    }
                }
                break;
            }
            case EXHAUSTIVE : {
                final InjectionPoint injection = new InjectionPoint(pid, id, occurrence, className, methodName, invocationName, line, exceptionName);
                if (!injectionSet.containsKey(injection)) {
                    if (injected.compareAndSet(false, true)) {
                        grant = injection;
                        return true;
                    }
                }
                break;
            }
        }
        return false;
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

    static public class InjectionPoint {
        final int pid;
        final int id;
        final int occurrence;
        final String className;
        final String methodName;
        final String invocationName;
        final int line;
        final String exceptionName;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InjectionPoint that = (InjectionPoint) o;
            return pid == that.pid && id == that.id && occurrence == that.occurrence;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pid, id, occurrence);
        }

        public InjectionPoint(int pid, int id, int occurrence, String className, String methodName, String invocationName, int line, String exceptionName) {
            this.pid = pid;
            this.id = id;
            this.occurrence = occurrence;
            this.className = className;
            this.methodName = methodName;
            this.invocationName = invocationName;
            this.line = line;
            this.exceptionName = exceptionName;
        }

        public InjectionPoint(final File file) throws IOException {
            try (final InputStream inputStream = Files.newInputStream(file.toPath());
                 final JsonReader reader = Json.createReader(inputStream)) {
                final JsonObject json = reader.readObject();
                this.pid = json.getInt("pid");
                this.id = json.getInt("id");
                this.occurrence = json.getInt("occurrence");
                this.className = json.getString("className");
                this.methodName = json.getString("methodName");
                this.invocationName = json.getString("invocationName");
                this.line = json.getInt("line");
                this.exceptionName = json.getString("exceptionName");
            }
        }

        public JsonObjectBuilder dump() {
            return Json.createObjectBuilder()
                    .add("pid", pid)
                    .add("id", id)
                    .add("occurrence", occurrence)
                    .add("className", className)
                    .add("methodName", methodName)
                    .add("invocationName", invocationName)
                    .add("line", line)
                    .add("exceptionName", exceptionName);
        }
    }
}
