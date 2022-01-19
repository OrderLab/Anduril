package runtime;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class TraceAgent {
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
    static private final String injectionMark = "flaky test exception injection of TraceAgent";
    static private final int targetId = Integer.getInteger("flakyAgent.injectionId", -1);
    static private final int times = Integer.getInteger("flakyAgent.injectionTimes", 0);
    static private final String exceptionName = System.getProperty("flakyAgent.fault", "#");
    static private final boolean fixPointInjectionMode = Boolean.getBoolean("flakyAgent.fixPointInjectionMode");

    static public Throwable createException(final String exceptionName) {
        try {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            final Class<?> exceptionClass = Class.forName(exceptionName, true, loader);
            final Constructor<?>[] cons = exceptionClass.getDeclaredConstructors();
            for (final Constructor<?> ctr : cons) {
                ctr.setAccessible(true);
            }
            try {
                return (Throwable) exceptionClass.getConstructor(String.class).newInstance(injectionMark);
            } catch (Exception ignored) { }
            try {
                return (Throwable) exceptionClass.getConstructor().newInstance();
            } catch (Exception ignored) { }
        } catch (final Exception ignored) { }
        return null;
    }

    static public void inject(final int id) throws Throwable {
        if (fixPointInjectionMode) {
            if (id == targetId) {
                if (injectionCounter.incrementAndGet() == times) {
                    final Throwable t = createException(exceptionName);
                    if (t == null) {
                        System.out.println("FlakyAgent: fail to construct the exception " + exceptionName);
                    } else {
                        System.out.println("FlakyAgent: injected the exception " + exceptionName);
                        throw t;
                    }
                }
            }
        } else {
            localInjectionManager.inject(id);
        }
    }

    static private LocalInjectionManager localInjectionManager = null;

    static public void main(final String[] args) throws Throwable {
        localInjectionManager = new LocalInjectionManager(args[0], args[1], args[2]);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            localInjectionManager.dump();
        }));
        final Class<?> cls = Class.forName(args[3]);
        final Method method = cls.getMethod("main", String[].class);
        method.invoke(null, (Object) Arrays.copyOfRange(args, 4, args.length));
    }
}
