package runtime.fate;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FateRemote extends Remote {
    int inject(final String func, final String file, final int stacktrace, final int pid) throws RemoteException;
    void shutdown() throws RemoteException;
}
