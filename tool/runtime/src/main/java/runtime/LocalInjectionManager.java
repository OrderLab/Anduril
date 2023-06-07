package runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.exception.ExceptionBuilder;
import runtime.time.TimeFeedbackManager;
import runtime.time.TimePriorityTable;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocalInjectionManager {
    private static final Logger LOG = LoggerFactory.getLogger(LocalInjectionManager.class);

    public static class ThreadTimePair {
        public final LocalDateTime time;
        public final String thread_name;

        public ThreadTimePair(LocalDateTime time, String thread_name) {
            this.time = time;
            this.thread_name = thread_name;
        }
    }

    protected final String trialsPath, specPath, injectionResultPath;

    protected final AtomicBoolean injected = new AtomicBoolean(false);
    protected volatile InjectionIndex injectionPoint = null;

    protected final ConcurrentMap<InjectionIndex, Object> injectionSet = new ConcurrentHashMap<>();
    protected final ConcurrentMap<Integer, String> id2name = new ConcurrentHashMap<>();

    private final ConcurrentMap<Integer, Throwable> id2exception = new ConcurrentHashMap<>();
    //private final ConcurrentMap<String, AtomicBoolean> name2Tried = new ConcurrentHashMap<>();
    //private final ConcurrentMap<String, Throwable> name2exception = new ConcurrentHashMap<>();

    private final ConcurrentMap<Integer, Integer> id2times = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, ConcurrentMap<Integer, ThreadTimePair>> id2times2time = new ConcurrentHashMap<>();
//    private final ConcurrentMap<Integer, Integer> thread2block = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Integer> block2times = new ConcurrentHashMap<>();

    protected int trialId = 0;
    protected int windowSize = TraceAgent.config.minimumTimeMode ? 1 : TraceAgent.config.slidingWindowSize;
    protected FeedbackManager feedbackManager = null;
    protected JsonObject json = null;

    private static final JsonWriterFactory writerFactory;

    static public final int INF = 1_000_000_000; // largest id

    public TimePriorityTable timePriorityTable = null;

    static {
        final Map<String, Object> options = new HashMap<>();
        options.put(JsonGenerator.PRETTY_PRINTING,true);
        writerFactory = Json.createWriterFactory(options);
    }

    public LocalInjectionManager(final String trialsPath,
                                 final String specPath,
                                 final String injectionResultPath) {
        this.trialsPath = trialsPath;
        this.specPath = specPath;
        this.injectionResultPath = injectionResultPath;
        int start = 0;
        if (TraceAgent.config.isTimeFeedback) {
            this.timePriorityTable = TimePriorityTable.load(TraceAgent.config.timePriorityTable);
        }
        try (final InputStream inputStream = Files.newInputStream(Paths.get(this.specPath));
             final JsonReader reader = Json.createReader(inputStream)) {
            System.out.printf("\nFlaky Agent Read Json Start Time:  %s\n",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
            this.json = reader.readObject();
            System.out.printf("\nFlaky Agent Read Json End Time:  %s\n",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
            start = this.json.getInt("start");
            if (TraceAgent.config.isTimeFeedback) {
                feedbackManager = new TimeFeedbackManager(this.specPath, this.json, this.timePriorityTable);
            } else {
                if (TraceAgent.config.minimumTimeMode) {
                    feedbackManager = new FeedbackManager(this.specPath, this.json, this.timePriorityTable);
                } else {
                    feedbackManager = new FeedbackManager(this.specPath, this.json);
                }
            }
        } catch (final IOException e) {
            LOG.error("Error while loading files", e);
            System.exit(-1);
        }
        final File[] files = new File(this.trialsPath).listFiles((file, name) -> name.endsWith(".json"));
        int latestOK = -2; // avoid -1 + 1 == 0
        for (final File result : files) {
            try (final InputStream inputStream = Files.newInputStream(result.toPath());
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
                if (TraceAgent.config.allowFeedback) {
                    final JsonArray events = json.getJsonArray("feedback");
                    for (int i = 0; i < events.size(); i++) {
                        feedbackManager.activate(events.getInt(i));
                    }
                    for (int i = 0; i < start; i++) {
                        feedbackManager.deactivate(i);
                    }
                }
                final int block = json.getInt("block");
                final InjectionIndex index = new InjectionIndex(json.getInt("pid"), json.getInt("id"),
                        json.getString("exception"), json.getInt("occurrence"), block);
                injectionSet.put(index, new Object());
            } catch (final IOException ignored) { }
        }
        if (TraceAgent.config.minimumTimeMode) {
            windowSize += 1;
        } else {
            if (latestOK + 1 == this.trialId) {
                windowSize *= 2;
            }
            if (windowSize > INF) {
                windowSize = INF;
            }
        }
        try (final FileWriter fw = new FileWriter(this.injectionResultPath);
             final JsonWriter jsonWriter = writerFactory.createWriter(fw)) {
            jsonWriter.writeObject(Json.createObjectBuilder()
                    .add("trial_id", this.trialId)
                    .add("window", windowSize).build());
        } catch (final IOException ignored) { }
        feedbackManager.calc(windowSize);
        if (TraceAgent.config.minimumTimeMode) {
            for (final InjectionIndex index : this.injectionSet.keySet()) {
                feedbackManager.allowSet.merge(index.id, -1, Integer::sum);
            }
            feedbackManager.allowSet.values().removeIf(count -> count <= 0);
        }
        if (TraceAgent.config.isTimeFeedback) {
            try (final PrintWriter csv = new PrintWriter(
                    Files.newOutputStream(Paths.get(this.trialsPath + "/" + this.trialId + ".csv")))) {
                ((TimeFeedbackManager) feedbackManager).printCSV(csv);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        final JsonArray arr = this.json.getJsonArray("injections");
        final JsonArray events = this.json.getJsonArray("nodes");
        for (int i = 0; i < arr.size(); i++) {
            final JsonObject spec = arr.getJsonObject(i);
            final int injectionId = spec.getInt("id");
            if (TraceAgent.config.distributedMode) {
                final String name = spec.getString("exception");
                if (name != null) {
                    id2name.put(injectionId, name);
                }
            } else {
                //System specific exception will be constructed dynamically
                //Find the corresponding callee event
                final int callee = spec.getInt("callee");
                final JsonObject event = events.getJsonObject(callee);
                assert(event.getInt("id") == callee);
                String event_type = event.getString("type");
                Throwable exception;
                if (TraceAgent.config.dynamicCreate) {
                    final String exception_name = spec.getString("exception");
                    id2name.put(injectionId, exception_name);
                } else {
                    if (event_type.equals("Uncaught_throw_injection_event")) {
                        final String exception_name = spec.getString("exception");
                        id2name.put(injectionId, exception_name);
                    } else {
                        exception = ExceptionBuilder.createException(spec.getString("exception"));
                        if (exception != null) {
                            id2exception.put(injectionId, exception);
                        }
                    }
                }
            }
        }
        if (!TraceAgent.config.isTimeFeedback) {
            System.out.println("injection allow set: " + feedbackManager.allowSet.keySet());
        }
    }

    public void inject(final int id, final int blockId) throws Throwable {
        if (!injected.get()) {
            if (!TraceAgent.config.isTimeFeedback && !feedbackManager.isAllowed(id)) {
                return;
            }
            // System-specific exception
            // One type exception will only be created once during running
            // Warn: Need synchronization?
            final String name = id2name.get(id);
            if (name != null) {
                // WARN: it makes sure that each injection point (id) has distinct fault instance
                //AtomicBoolean tried = name2Tried.get(name);
                synchronized (name) {
                    if (id2name.get(id) != null) {
                        Throwable created_exception = ExceptionBuilder.createException(name);
                        if (created_exception != null) {
                            id2exception.put(id, created_exception);
                        }
                        id2name.remove(id);
                    }
                }
            }
            final Throwable exception = id2exception.get(id);
            if (exception != null) {
                // WARN: it assumes that each injection point (id) has distinct fault instance
                synchronized (exception) {
                    final int occurrence = id2times.getOrDefault(id, 0) + 1;
                    id2times.put(id, occurrence);
                    if (!TraceAgent.config.avoidBlockMode) {
                        if (block2times.containsKey(blockId)) {
                            return;
                        }
                        block2times.put(blockId, 1);
                    }
                    final InjectionIndex index = new InjectionIndex(-1, id, exception.getClass().getName(), occurrence, blockId);
                    if (TraceAgent.config.isTimeFeedback) {
                        if (feedbackManager.isAllowed(id, occurrence) && !injectionSet.containsKey(index) &&
                                injected.compareAndSet(false, true)) {
                            injectionPoint = index;
                            throw exception;
                        }
                        return;
                    }
                    final boolean ok;
                    if (TraceAgent.config.isProbabilityFeedback) {
                        ok = Math.random() < TraceAgent.config.probability;
                    } else {
                        ok = occurrence <= TraceAgent.config.injectionOccurrenceLimit;
                    }
                    if (ok && !injectionSet.containsKey(index) &&
                            injected.compareAndSet(false, true)) {
                        injectionPoint = index;
                        throw exception;
                    }
                }
            }
        }
    }
//
//    public void trace(final int id) {
//        thread2block.put(System.identityHashCode(Thread.currentThread()), id);
//    }



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
        } catch (final IOException ignored) { }
    }

    // Note: This method also utilize the id2Times for counter
    public void recordInjectionTime(final int pid, final int id) {
        LocalDateTime now = LocalDateTime.now();
        id2times2time.putIfAbsent(id,new ConcurrentHashMap<>());
        final ConcurrentMap<Integer,ThreadTimePair> injection_trace = id2times2time.get(id);
        final int occurrence;
        // Get the current occurrence
        // Warn: should not be called at the same time with inject()
        synchronized (injection_trace) {
            occurrence = this.id2times.getOrDefault(id, 0) + 1;
            this.id2times.put(id, occurrence);
        }
        injection_trace.put(occurrence,new ThreadTimePair(now, Thread.currentThread().getName()));
    }

    public void printRecordInjectionTime() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");
        try (final PrintWriter csv = new PrintWriter(
                Files.newOutputStream(Paths.get(this.trialsPath + "/" + "InjectionTimeRecord" + ".csv")))) {
                csv.println("id,occurrence,time,thread");
                this.id2times2time.forEach((id, times2time)->
                        times2time.forEach((times,time_thread_pair)->
                                csv.printf("%d,%d,%s,%s\n", id,times,
                                        time_thread_pair.time.format(format),time_thread_pair.thread_name)));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
