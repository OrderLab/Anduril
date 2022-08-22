package runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.exception.ExceptionBuilder;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import java.lang.management.ManagementFactory;
public final class TraceAgent {
    private static final Logger LOG = LoggerFactory.getLogger(runtime.TraceAgent.class);
//    static private TraceRemote stub = null;

//    static {
//        try {
//            final Registry registry = LocateRegistry.getRegistry(1099);
//            stub = (TraceRemote) registry.lookup("flaky-trace");
//        } catch (final RemoteException ignored) {
//            //==
//        } catch (final NotBoundException ignored) {
//            //==
//        }
//    }

    // for recording the number of active threads

    private static int threadNum = 0;
    private static final Object recordLock = new Object();

    static public void threadRecord(final String name, final int d) {
        synchronized (recordLock) {
            threadNum += d;
            System.out.println("time=" + System.nanoTime() + "  " + name + " : " + (d == 1 ? "start" : "end  ") + "  #threads: " + threadNum);
        }
    }

    // for recording the basic block traces

    static private final class Trace {
        private final String threadName;
        private final int threadHash;
        private final int index;

        private Trace(int index) {
            final Thread cur = Thread.currentThread();
            this.threadName = cur.getName();
            this.threadHash = cur.hashCode();
            this.index = index;
        }
    }

    static private final String traceRecordFileName = System.getProperty("flakyAgent.traceFile");
    static private final AtomicBoolean traceFlag = new AtomicBoolean(false);
    static private final CopyOnWriteArrayList<Trace> traces = new CopyOnWriteArrayList<>();
    static private final AtomicBoolean stopTracing = new AtomicBoolean(false);

    static public void trace(final int id) {
        if (traceFlag.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                System.out.println("flakyAgent shutdown hook >>>>");
                stopTracing.set(true);
                try (final PrintWriter printWriter = new PrintWriter(new FileWriter(traceRecordFileName))) {
//                    Thread.sleep(500); // wait for other potentially active threads
                    for (final Trace trace : traces) {
                        printWriter.printf("%s,%d,%d\n", trace.threadName, trace.threadHash, trace.index);
                    }
                } catch (final Exception ignored) { }
            }));
        }
        if (!stopTracing.get()) {
            traces.add(new Trace(id));
        }
        if (!fixPointInjectionMode) {
            localInjectionManager.trace(id);
        }
//        if (stub != null) {
//            try {
//                stub.trace(Thread.currentThread().getName(), Thread.currentThread().hashCode(), id);
//            } catch (final RemoteException ignored) {
//                // ==
//            }
//        }
    }

    // for fault injection

    static private final AtomicInteger injectionCounter = new AtomicInteger();
    static private final int targetId = Integer.getInteger("flakyAgent.injectionId", -1);
    static private final int times = Integer.getInteger("flakyAgent.injectionTimes", 0);
    static private final String exceptionName = System.getProperty("flakyAgent.fault", "#");
    static public final boolean fixPointInjectionMode = Boolean.getBoolean("flakyAgent.fixPointInjectionMode");
    static public final boolean avoidBlockMode = Boolean.getBoolean("flakyAgent.avoidBlockMode");
    static public final boolean allowFeedback = Boolean.getBoolean("flakyAgent.feedback");
    static public final int slidingWindowSize = Integer.getInteger("flakyAgent.slidingWindow", 10);
    static public final int injectionOccurrenceLimit = Integer.getInteger("flakyAgent.injectionOccurrenceLimit", 1);
    static public final String injectionPointsPath = System.getProperty("flakyAgent.injectionPointsPath", "#");

    protected static final ConcurrentMap<Integer, Throwable> id2exception = new ConcurrentHashMap<>();

    static public final boolean distributedMode = Boolean.getBoolean("flakyAgent.distributedMode");
    static public final boolean disableAgent = Boolean.getBoolean("flakyAgent.disableAgent");
    static public final int pid = Integer.getInteger("flakyAgent.pid", -1);
    static public final int trialTimeout = Integer.getInteger("flakyAgent.trialTimeout", -1);
    static public final boolean logInject = Boolean.getBoolean("flakyAgent.logInject");
    static public final boolean recordOnthefly = Boolean.getBoolean("flakyAgent.recordOnthefly");
    static {
        if (distributedMode && !disableAgent) {
            try (final InputStream inputStream = new FileInputStream(injectionPointsPath);
                 final JsonReader reader = Json.createReader(inputStream)) {
                final JsonObject json = reader.readObject();
                final JsonArray arr = json.getJsonArray("injections");
                for (int i = 0; i < arr.size(); i++) {
                    final JsonObject spec = arr.getJsonObject(i);
                    final int injectionId = spec.getInt("id");
                    final Throwable exception = ExceptionBuilder.createException(spec.getString("exception"));
                    if (exception != null) {
                        id2exception.put(injectionId, exception);
                    }
                }
            } catch (final IOException e) {
                LOG.error("Error while loading files", e);
                System.exit(-1);
            }
        }
    }

    static public void inject(final int id, final int blockId) throws Throwable {
        if (logInject) {
            LOG.info("flaky record injection {} ", id);
        }
        if (disableAgent) {
            return;
        }
        if (distributedMode) {
            final Throwable exception = id2exception.get(id);
            if (exception != null) {
                int decision = 0;
                try {
                    decision = getStub().inject(pid, id, blockId);
                } catch (RemoteException ignored) { }
                if (decision == 1) {
                    throw exception;
                }
            }
            return;
        }
        try {
            if (fixPointInjectionMode) {
                if (id == targetId) {
                    if (injectionCounter.incrementAndGet() == times) {
                        final Throwable t = ExceptionBuilder.createException(exceptionName);
                        if (t == null) {
                            LOG.error("FlakyAgent: fail to construct the exception " + exceptionName);
                        } else {
//                            LOG.info("FlakyAgent: injected the exception " + exceptionName);
                            throw t;
                        }
                    }
                }
            } else {
                localInjectionManager.inject(id, blockId);
            }
        } catch(Throwable t) {
            if (recordOnthefly && dumpFlag.compareAndSet(false, true)) {
                localInjectionManager.dump();
            }
            throw t;
        }
    }

    static public void initStub() {
        if (distributedMode && !disableAgent) {
            getStub();
        }
    }

    private static final AtomicReference<TraceRemote> stub = new AtomicReference<>(null);
    public static final int RMI_PORT = 1099;
    public static final String RMI_NAME = "rmi_flaky";

    static private TraceRemote getStub() {
        return stub.updateAndGet(s -> {
            if (s != null) {
                return s;
            }
            try {
                Registry registry = LocateRegistry.getRegistry(RMI_PORT);
                return (TraceRemote) registry.lookup(RMI_NAME);
            } catch (RemoteException | NotBoundException e) {
                LOG.info(e.getMessage());
                return null;
            }
        });
    }

    static private volatile LocalInjectionManager localInjectionManager = null;
    static public volatile DistributedInjectionManager distributedInjectionManager = null;
    static public final CountDownLatch waiter = new CountDownLatch(1);
    static final AtomicBoolean dumpFlag = new AtomicBoolean(false);

    static public void main(final String[] args) throws Throwable {
        // util for shutdown server
        if (args.length == 0) {
            getStub().shutdown();
            return;
        }
        if (distributedMode) {
            distributedInjectionManager = new DistributedInjectionManager(Integer.parseInt(args[0]), args[1], args[2], args[3]);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> distributedInjectionManager.dump()));
            final Registry rmiRegistry = LocateRegistry.createRegistry(RMI_PORT);
            final TraceStub s = new TraceStub();
            rmiRegistry.rebind(RMI_NAME, UnicastRemoteObject.exportObject(s, 0));
            LOG.info("Server started, waiting for the end ...");
            waiter.await();
            rmiRegistry.unbind(RMI_NAME);
            UnicastRemoteObject.unexportObject(s, true);
        } else {
            localInjectionManager = new LocalInjectionManager(args[0], args[1], args[2]);
            if (trialTimeout != -1) {
                new Thread(() -> {
                    try {
                        if (!waiter.await(trialTimeout, TimeUnit.SECONDS)) {
                            LOG.warn("This trial times out with {} seconds", trialTimeout);
                            if (dumpFlag.compareAndSet(false, true)) {
                                localInjectionManager.dump();
                            }
                            Runtime.getRuntime().halt(0);
                        }
                    } catch (InterruptedException ignored) { }
                }).start();
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (dumpFlag.compareAndSet(false, true)) {
                    localInjectionManager.dump();
                }
                waiter.countDown();
            }));
            final Class<?> cls = Class.forName(args[3]);
            final Method method = cls.getMethod("main", String[].class);
            method.invoke(null, (Object) Arrays.copyOfRange(args, 4, args.length));
        }
    }
}
