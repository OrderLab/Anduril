package runtime.baseline;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BaselineRemote extends Remote {
    int inject(final int pid, final int id, final int occurrence, final String className, final String methodName,
               final String invocationName, final int line, final String exceptionName) throws RemoteException;
    void shutdown() throws RemoteException;
}
