package analyzer.cases;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Test case borrowed from ZooKeeper LearnerCnxAcceptor.
 *
 * TODO: @haoze, comment what this is testing for and the expected result.
 */
public class SocketCnxAcceptor extends Thread {
    private volatile boolean stop = false;
    ServerSocket ss;
    int tickTime;
    int initLimit;
    boolean nodelay;

    @Override
    public void run() {
        try {
            while (!stop) {
                try{
                    Socket s = ss.accept();
                    s.setSoTimeout(tickTime * initLimit);
                    s.setTcpNoDelay(nodelay);
                    Thread fh = new Thread();
                    fh.start();
                } catch (SocketException e) {
                    if (stop) {
                        stop = true;
                    } else {
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public void halt() {
        stop = true;
    }
}
