package runtime.baseline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.config.Config;
import runtime.config.Hash;
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

    public static final Config config = Config.getDefaultBaselineConfig();

    static private final AtomicInteger injectionCounter = new AtomicInteger();

    static private final ConcurrentMap<String, Throwable> exceptions = new ConcurrentHashMap<>();
    static private final Throwable sentinelException = new Exception("invalid injection");
    static private final ConcurrentMap<Integer, Integer> id2times = new ConcurrentHashMap<>();

    public static void injectStackTrace(final int hash, final String exceptionName) throws Throwable {
        if (config.disableAgent) {
            return;
        }
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        long v = 0;
        for (int i = stackTrace.length - 1; i > -1; i--) {
            if (stackTrace[i].getClassName().equals(BaselineAgent.class.getTypeName()) &&
                    stackTrace[i].getMethodName().equals("injectStackTrace")) {
                break;
            }
            v = Hash.addStackTrace(v, stackTrace[i].getClassName(), stackTrace[i].getMethodName(), stackTrace[i].getLineNumber());
        }
        if (v == hash) {
            if (config.logInject) {
                LOG.info("flaky record injection {}", Hash.getStackTrace(stackTrace));
            }
        }
    }

    public static void inject(final int id, final String className, final String methodName,
                              final String invocationName, final int line, final String exceptionName) throws Throwable {
        if (config.disableAgent) {
            return;
        }
        if (config.logInject) {
            LOG.info("flaky record injection {}", id);
        }
        if (config.fixPointInjectionMode) {
            if (id == config.targetId) {
                if (injectionCounter.incrementAndGet() == config.times) {
                    final Throwable t = ExceptionBuilder.createException(BaselineAgent.config.exceptionName);
                    if (t == null) {
                        LOG.error("FlakyAgent: fail to construct the exception " + BaselineAgent.config.exceptionName);
                    } else {
//                        LOG.info("FlakyAgent: injected the exception " + exceptionName);
                        if (config.recordOnthefly && dumpFlag.compareAndSet(false, true)) {
                            injectionManager.dump();
                        }
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
        if (config.distributedMode) {
            int decision = 0;
            try {
                decision = getStub().inject(config.pid, id, occurrence, className, methodName, invocationName, line, exceptionName);
            } catch (RemoteException ignored) { }
            if (decision == 1) {
                throw exception;
            }
        } else {
            if (injectionManager.inject(-1, id, occurrence, className, methodName, invocationName, line, exceptionName)) {
                if (config.recordOnthefly && dumpFlag.compareAndSet(false, true)) {
                    injectionManager.dump();
                }
                throw exception;
            }
        }
    }

    private static final AtomicReference<BaselineRemote> stub = new AtomicReference<>(null);

    public static final int RMI_PORT = 1099;
    public static final String RMI_NAME = "rmi_flaky_baseline";

    static public void initStub() {
        if (config.distributedMode && !config.disableAgent) {
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
        if (config.distributedMode) {
            Runtime.getRuntime().addShutdownHook(new Thread(injectionManager::dump));
            final Registry rmiRegistry = LocateRegistry.createRegistry(RMI_PORT);
            final BaselineStub s = new BaselineStub();
            rmiRegistry.rebind(RMI_NAME, UnicastRemoteObject.exportObject(s, 0));
            LOG.info("Server started, waiting for the end ...");
            waiter.await();
            rmiRegistry.unbind(RMI_NAME);
            UnicastRemoteObject.unexportObject(s, true);
        } else {
            if (config.trialTimeout != -1) {
                new Thread(() -> {
                    try {
                        if (!waiter.await(config.trialTimeout, TimeUnit.SECONDS)) {
                            LOG.warn("This trial times out with {} seconds", config.trialTimeout);
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
