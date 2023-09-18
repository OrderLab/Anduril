package runtime.stacktrace;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface StacktraceRemote extends Remote {
    int inject(final int id, final int pid) throws RemoteException;
    void shutdown() throws RemoteException;
}
