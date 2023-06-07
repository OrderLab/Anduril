package runtime;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TraceRemote extends Remote {
//    void trace(final String name, final int threadId, final int blockId) throws RemoteException;
    int inject(final int pid, final int id, final int blockId) throws RemoteException;
    void shutdown() throws RemoteException;
    void recordInjectionTime(final int pid, final int id) throws RemoteException;
}
