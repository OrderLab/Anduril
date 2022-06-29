package runtime.baseline;

import java.rmi.RemoteException;

public final class BaselineStub implements BaselineRemote {
    @Override
    public int inject(final int pid, final int id, final int occurrence, final String className, final String methodName,
                      final String invocationName, final int line, final String exceptionName) throws RemoteException {
        if (BaselineAgent.injectionManager.inject(pid, id, occurrence, className, methodName, invocationName, line, exceptionName)) {
            return 1;
        }
        return 0;
    }

    @Override
    public void shutdown() throws RemoteException {
        BaselineAgent.waiter.countDown();
    }
}
