package runtime.baseline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.exception.ExceptionBuilder;

import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class BaselineAgent {
    private static final Logger LOG = LoggerFactory.getLogger(runtime.baseline.BaselineAgent.class);

    static public final boolean distributedMode = Boolean.getBoolean("flakyAgent.distributedMode");
    static public final boolean disableAgent = Boolean.getBoolean("flakyAgent.disableAgent");
    static public final int pid = Integer.getInteger("flakyAgent.pid", -1);
    static public final int trialTimeout = Integer.getInteger("flakyAgent.trialTimeout", -1);
    static public final boolean logInject = Boolean.getBoolean("flakyAgent.logInject");

    static private final int targetId = Integer.getInteger("flakyAgent.injectionId", -1);
    static private final int times = Integer.getInteger("flakyAgent.injectionTimes", 0);
    static private final String exceptionName = System.getProperty("flakyAgent.fault", "#");
    static public final boolean fixPointInjectionMode = Boolean.getBoolean("flakyAgent.fixPointInjectionMode");
    static private final AtomicInteger injectionCounter = new AtomicInteger();

    static private final ConcurrentMap<String, Throwable> exceptions = new ConcurrentHashMap<>();
    static private final Throwable sentinelException = new Exception("invalid injection");
    static private final ConcurrentMap<Integer, Integer> id2times = new ConcurrentHashMap<>();

    public static void inject(final int id, final String className, final String methodName,
                              final String invocationName, final int line, final String exceptionName) throws Throwable {
        if (disableAgent) {
            return;
        }
        if (logInject) {
            LOG.info("flaky record injection {}", id);
        }
        if (fixPointInjectionMode) {
            if (id == targetId) {
                if (injectionCounter.incrementAndGet() == times) {
                    final Throwable t = ExceptionBuilder.createException(BaselineAgent.exceptionName);
                    if (t == null) {
                        LOG.error("FlakyAgent: fail to construct the exception " + BaselineAgent.exceptionName);
                    } else {
//                        LOG.info("FlakyAgent: injected the exception " + exceptionName);
                        throw t;
                    }
                }
            }
            return;
        }
        final Throwable exception = exceptions.computeIfAbsent(exceptionName, e -> {
            final Throwable ex = ExceptionBuilder.createException(exceptionName);
            return ex == null ? sentinelException : ex;
        });
        if (exception == sentinelException) {
            return;
        }
        final int occurrence = id2times.merge(id, 1, Integer::sum);
        if (distributedMode) {
            int decision = 0;
            try {
                decision = getStub().inject(pid, id, occurrence, className, methodName, invocationName, line, exceptionName);
            } catch (RemoteException ignored) { }
            if (decision == 1) {
                throw exception;
            }
        } else {
            if (injectionManager.inject(-1, id, occurrence, className, methodName, invocationName, line, exceptionName)) {
                throw exception;
            }
        }
    }

    private static final AtomicReference<BaselineRemote> stub = new AtomicReference<>(null);

    public static final int RMI_PORT = 1099;
    public static final String RMI_NAME = "rmi_flaky_baseline";

    static public void initStub() {
        if (distributedMode && !disableAgent) {
            getStub();
        }
    }

    static private BaselineRemote getStub() {
        return stub.updateAndGet(s -> {
            if (s != null) {
                return s;
            }
            try {
                Registry registry = LocateRegistry.getRegistry(RMI_PORT);
                return (BaselineRemote) registry.lookup(RMI_NAME);
            } catch (RemoteException | NotBoundException e) {
                return null;
            }
        });
    }

    static public final CountDownLatch waiter = new CountDownLatch(1);
    static final AtomicBoolean dumpFlag = new AtomicBoolean(false);
    static public InjectionManager injectionManager = null;

    static public void main(final String[] args) throws Throwable {
        // util for shutdown server
        if (args.length == 0) {
            getStub().shutdown();
            return;
        }
        injectionManager = new InjectionManager(args[0], args[1]);
        if (distributedMode) {
            Runtime.getRuntime().addShutdownHook(new Thread(injectionManager::dump));
            final Registry rmiRegistry = LocateRegistry.createRegistry(RMI_PORT);
            final BaselineStub s = new BaselineStub();
            rmiRegistry.rebind(RMI_NAME, UnicastRemoteObject.exportObject(s, 0));
            LOG.info("Server started, waiting for the end ...");
            waiter.await();
            rmiRegistry.unbind(RMI_NAME);
            UnicastRemoteObject.unexportObject(s, true);
        } else {
            if (trialTimeout != -1) {
                new Thread(() -> {
                    try {
                        if (!waiter.await(trialTimeout, TimeUnit.SECONDS)) {
                            LOG.warn("This trial times out with {} seconds", trialTimeout);
                            if (dumpFlag.compareAndSet(false, true)) {
                                injectionManager.dump();
                            }
                            System.exit(0);
                        }
                    } catch (InterruptedException ignored) { }
                }).start();
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (dumpFlag.compareAndSet(false, true)) {
                    injectionManager.dump();
                }
                waiter.countDown();
            }));
            final Class<?> cls = Class.forName(args[2]);
            final Method method = cls.getMethod("main", String[].class);
            method.invoke(null, (Object) Arrays.copyOfRange(args, 3, args.length));
        }
    }
}
