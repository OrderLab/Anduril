package runtime;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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

    private static int threadNum = 0;
    private static final Object recordLock = new Object();

    static public void threadRecord(final String name, final int d) {
        synchronized (recordLock) {
            threadNum += d;
            System.out.println("time=" + System.nanoTime() + "  " + name + " : " + (d == 1 ? "start" : "end  ") + "  #threads: " + threadNum);
        }
    }

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

    static public void trace(final int id) {
        if (traceFlag.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                System.out.println("flakyAgent shutdown hook >>>>");
                try (final PrintWriter printWriter = new PrintWriter(new FileWriter(traceRecordFileName))) {
//                    Thread.sleep(500); // wait for other potentially active threads
                    for (final Trace trace : traces) {
                        printWriter.printf("%s,%d,%d\n", trace.threadName, trace.threadHash, trace.index);
                    }
                } catch (final Exception ignored) { }
            }));
        }
        traces.add(new Trace(id));
//        if (stub != null) {
//            try {
//                stub.trace(Thread.currentThread().getName(), Thread.currentThread().hashCode(), id);
//            } catch (final RemoteException ignored) {
//                // ==
//            }
//        }
    }

    static private final AtomicInteger injectionCounter = new AtomicInteger();
    static private final String injectionMark = "flaky test exception injection of TraceAgent";
    static private final int targetId = Integer.getInteger("flakyAgent.injectionId", -1);
    static private final int times = Integer.getInteger("flakyAgent.injectionTimes", 0);
    static private final String exceptionName = System.getProperty("flakyAgent.fault");

    static private Throwable createException(final String exceptionName) {
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
    }
}
