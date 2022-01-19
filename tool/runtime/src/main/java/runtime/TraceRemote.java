package runtime;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TraceRemote extends Remote {
    void trace(final String name, final int threadId, final int blockId) throws RemoteException;
}
