package runtime.fate;

import java.rmi.RemoteException;

public final class FateStub implements FateRemote {
    @Override
    public int inject(final String func, final String file, final int stacktrace, final int pid) {
        return FateAgent.injectionManager.inject(func, file, stacktrace, pid) ? 1 : 0;
    }

    @Override
    public void shutdown() throws RemoteException {
        FateAgent.waiter.countDown();
    }
}
