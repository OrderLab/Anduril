package runtime.fate;

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
import java.util.concurrent.atomic.AtomicReference;

public final class FateAgent {
    private static final Logger LOG = LoggerFactory.getLogger(FateAgent.class);

    private static final AtomicReference<FateRemote> stub = new AtomicReference<>(null);

    static private final ConcurrentMap<String, Throwable> exceptions = new ConcurrentHashMap<>();
    static private final Throwable sentinelException = new Exception("invalid injection");

    public static final int RMI_PORT = 1099;
    public static final String RMI_NAME = "rmi_flaky_fate";
    public static final int pid = Integer.getInteger("flakyAgent.pid", -1);

    public static final boolean distributedMode = Boolean.getBoolean("flakyAgent.distributedMode");
    public static final boolean disableAgent = Boolean.getBoolean("flakyAgent.disableAgent");
    public static final int trialTimeout = Integer.getInteger("flakyAgent.trialTimeout", -1);

    public static void inject(final String func, final String file, final String exceptionName) throws Throwable {
        if (disableAgent) {
            return;
        }
        final Throwable exception = exceptions.computeIfAbsent(exceptionName, e -> {
            final Throwable ex = ExceptionBuilder.createException(exceptionName);
            return ex == null ? sentinelException : ex;
        });
        if (exception == sentinelException) {
            return;
        }
        if (distributedMode) {
            if (getStub().inject(func, file, getStackTrace(), pid) == 1) {
                throw exception;
            }
        } else {
            if (injectionManager.inject(func, file, getStackTrace(), pid)) {
                throw exception;
            }
        }
    }

    public static int getStackTrace() {
        return Arrays.hashCode(Thread.currentThread().getStackTrace());
    }

    static public void initStub() {
        if (distributedMode && !disableAgent) {
            getStub();
        }
    }

    static private FateRemote getStub() {
        return stub.updateAndGet(s -> {
            if (s != null) {
                return s;
            }
            try {
                Registry registry = LocateRegistry.getRegistry(RMI_PORT);
                return (FateRemote) registry.lookup(RMI_NAME);
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
            final FateStub s = new FateStub();
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
