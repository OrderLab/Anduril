package server;

import runtime.TraceRemote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public final class TraceServer implements TraceRemote {

    @Override
    public void trace(final String name, final int threadId, final int blockId) throws RemoteException {
        System.out.println(name + "  thread id = " + threadId + "; block id = " + blockId);
    }

    private Registry rmiRegistry;
    private volatile boolean started = false;

    public TraceServer() throws RemoteException {
        try {
            // We'll try to create the registry by calling createRegistry. This allows us to skip
            // invoking the rmiregistry bin. Doing so will also setup the classpath properly for
            // the rmiregistry (otherwise, we may encounter ClassNotFound exception)
            rmiRegistry = LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
            // We may have already created the registry before. And the registry will only be destroyed
            // when the JVM exits. In this case, if we call the createRegistry again (e.g., when
            // running multiple server related unit tests, we may get the exception that ObjID is in use.
            // In this case, we should get the registry
            rmiRegistry = LocateRegistry.getRegistry(1099);
        }
    }

    public void start() throws Exception {
        Remote stub = UnicastRemoteObject.exportObject(this, 0);
        rmiRegistry.rebind("flaky-trace", stub);
        started = true;
    }

    public synchronized void shutdown() {
        if (started) {
            try {
                rmiRegistry.unbind("flaky-trace");
                UnicastRemoteObject.unexportObject(this, true);
            } catch (final Exception e) {
                // ===
            }
            started = false;
        }
    }

    public static void main(final String[] args) {
        try {
            final TraceServer server = new TraceServer();
            server.start();
            while (true) Thread.sleep(100000);
        } catch (final Exception ignored) { }
    }
}
