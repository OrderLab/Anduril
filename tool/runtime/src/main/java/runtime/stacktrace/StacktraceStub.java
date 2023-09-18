package runtime.stacktrace;



import java.rmi.RemoteException;

public final class StacktraceStub implements StacktraceRemote {
    @Override
    public int inject(final int id, final int pid) {
        return StacktraceAgent.injectionManager.inject(id, pid) ? 1 : 0;
    }

    @Override
    public void shutdown() throws RemoteException {
        StacktraceAgent.waiter.countDown();
    }
}
