package runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.config.Config;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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

//    private static int threadNum = 0;
//    private static final Object recordLock = new Object();

//    static public void threadRecord(final String name, final int d) {
//        synchronized (recordLock) {
//            threadNum += d;
//            System.out.println("time=" + System.nanoTime() + "  " + name + " : " + (d == 1 ? "start" : "end  ") + "  #threads: " + threadNum);
//        }
//    }

    // for recording the basic block traces

//    static private final class Trace {
//        private final String threadName;
//        private final int threadHash;
//        private final int index;
//
//        private Trace(int index) {
//            final Thread cur = Thread.currentThread();
//            this.threadName = cur.getName();
//            this.threadHash = cur.hashCode();
//            this.index = index;
//        }
//    }

//    static private final String traceRecordFileName = System.getProperty("flakyAgent.traceFile");
//    static private final AtomicBoolean traceFlag = new AtomicBoolean(false);
//    static private final CopyOnWriteArrayList<Trace> traces = new CopyOnWriteArrayList<>();
//    static private final AtomicBoolean stopTracing = new AtomicBoolean(false);

//    static public void trace(final int id) {
//        if (traceFlag.compareAndSet(false, true)) {
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
////                System.out.println("flakyAgent shutdown hook >>>>");
//                stopTracing.set(true);
//                try (final PrintWriter printWriter = new PrintWriter(new FileWriter(traceRecordFileName))) {
////                    Thread.sleep(500); // wait for other potentially active threads
//                    for (final Trace trace : traces) {
//                        printWriter.printf("%s,%d,%d\n", trace.threadName, trace.threadHash, trace.index);
//                    }
//                } catch (final Exception ignored) { }
//            }));
//        }
//        if (!stopTracing.get()) {
//            traces.add(new Trace(id));
//        }
////        if (!fixPointInjectionMode) {
////            localInjectionManager.trace(id);
////        }
////        if (stub != null) {
////            try {
////                stub.trace(Thread.currentThread().getName(), Thread.currentThread().hashCode(), id);
////            } catch (final RemoteException ignored) {
////                // ==
////            }
////        }
//    }

    // for fault injection

    static private final AtomicInteger injectionCounter = new AtomicInteger();
    public static final Config config = Config.getDefaultExperimentConfig();

    protected static final AtomicBoolean enableInject = new AtomicBoolean(!config.waitForStartup || config.distributedMode);
    protected static final ConcurrentMap<Integer, Throwable> id2exception = new ConcurrentHashMap<>();


    public static final AtomicInteger injectionCount = new AtomicInteger(0);
    public static final AtomicLong injectionOverhead = new AtomicLong(0);

    static {
        if (config.distributedMode && !config.disableAgent) {
            try (final InputStream inputStream = new FileInputStream(config.injectionPointsPath);
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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("0xfff flaky agent workload time used: {} / {} ns", injectionOverhead.get(), injectionCount.get());
        }));
    }

    static public void inject(final int id, final int blockId) throws Throwable {
        final long time = System.nanoTime();
        try {
            if (!enableInject.get()) {
                return;
            }
            if (config.timeTraceCollectMode) {
                if (config.distributedMode) {
                    getStub().recordInjectionTime(config.pid, id, Thread.currentThread().getName());
                } else {
                    localInjectionManager.recordInjectionTime(id);
                }
                return;
            }
            if (config.logInject) {
                LOG.info("flaky record injection {}", id);
            }
            if (config.disableAgent) {
                return;
            }
            if (config.distributedMode) {
                final Throwable exception = id2exception.get(id);
                if (exception != null) {
                    int decision = 0;
                    try {
                        decision = getStub().inject(config.pid, id, blockId);
                    } catch (RemoteException ignored) {
                    }
                    if (decision == 1) {
                        throw exception;
                    }
                }
                long elapsed = System.nanoTime() - time;
                if (elapsed > 0) {
                  injectionCount.addAndGet(1);
                  injectionOverhead.addAndGet(System.nanoTime() - time);
                }
                return;
            }
            try {
                if (config.fixPointInjectionMode) {
                    if (id == config.targetId) {
                        if (injectionCounter.incrementAndGet() == config.times) {
                            final Throwable t = ExceptionBuilder.createException(config.exceptionName);
                            if (t == null) {
                                LOG.error("FlakyAgent: fail to construct the exception " + config.exceptionName);
                            } else {
//                            LOG.info("FlakyAgent: injected the exception " + exceptionName);
                                throw t;
                            }
                        }
                    }
                } else {
                    localInjectionManager.inject(id, blockId);
                }
            } catch (Throwable t) {
                if (config.recordOnthefly && dumpFlag.compareAndSet(false, true)) {
                    localInjectionManager.dump();
                }
                throw t;
            }
        } finally {
            long elapsed = System.nanoTime() - time;
            if (elapsed > 0) {
              injectionCount.addAndGet(1);
              injectionOverhead.addAndGet(System.nanoTime() - time);
            }
        }
    }

    static public void initStub() {
        if (config.distributedMode && !config.disableAgent) {
            getStub();
        }
    }

    static public void triggerInject() {

        enableInject.compareAndSet(false, true);
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
        if (config.distributedMode) {
            distributedInjectionManager = new DistributedInjectionManager(Integer.parseInt(args[0]), args[1], args[2], args[3]);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> distributedInjectionManager.dump()));
            if (config.timeTraceCollectMode) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> distributedInjectionManager.printRecordInjectionTime()));
            }
            final Registry rmiRegistry = LocateRegistry.createRegistry(RMI_PORT);
            final TraceStub s = new TraceStub();
            rmiRegistry.rebind(RMI_NAME, UnicastRemoteObject.exportObject(s, 0));
            LOG.info("Server started, waiting for the end ...");
            waiter.await();
            rmiRegistry.unbind(RMI_NAME);
            UnicastRemoteObject.unexportObject(s, true);
        } else {
            System.out.printf("\nFlaky Agent Init Start Time:  %s\n",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
            localInjectionManager = new LocalInjectionManager(args[0], args[1], args[2]);
            if (config.trialTimeout != -1) {
                new Thread(() -> {
                    try {
                        if (!waiter.await(config.trialTimeout, TimeUnit.SECONDS)) {
                            LOG.warn("This trial times out with {} seconds", config.trialTimeout);
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
            if (config.timeTraceCollectMode) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> localInjectionManager.printRecordInjectionTime()));
            }
            final Class<?> cls = Class.forName(args[3]);
            final Method method = cls.getMethod("main", String[].class);
            System.out.printf("\nFlaky Agent Init End Time:   %s\n",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
            System.out.flush();  // flush before the workload log starts
            LOG.info("flaky workload sentinel message in case of empty logs");
            method.invoke(null, (Object) Arrays.copyOfRange(args, 4, args.length));
        }
    }
}

