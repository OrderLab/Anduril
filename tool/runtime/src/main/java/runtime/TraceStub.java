package runtime;

import java.rmi.RemoteException;

public final class TraceStub implements TraceRemote {
    @Override
    public int inject(final int pid, final int id, final int blockId) throws RemoteException {
        return TraceAgent.distributedInjectionManager.inject(pid, id, blockId);
    }

    @Override
    public void shutdown() throws RemoteException {
        TraceAgent.waiter.countDown();
    }

    public void recordInjectionTime(final int pid, final int id) {
        TraceAgent.distributedInjectionManager.recordInjectionTime(pid,id);
    }
}
