package analyzer.cases.eventManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class EventGraphExample extends Thread{
    private static final Logger LOG = LoggerFactory.getLogger(EventGraphExample.class);
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
                    LOG.debug("Add 1");
                    if (stop) {
                        stop = true;
                    } else {
                        throw e;
                    }
                }
            }
        } catch (IOException e) {
            assert(e!=null);
        }
    }

    public void halt() {
        stop = true;
    }
}
