package analyzer.cases.threadSchedulingAnalysis;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class CallableExample {

    private ExecutorService consume =
            Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setDaemon(true).build());;

    ServerSocket ss;

    public void submitThenGetSimple() {
        int x = 1;
        Future<Integer> zz = consume.submit(new Callable<Integer>() {
            @Override
            public Integer call()
                    throws IOException{
                Socket s = ss.accept();
                return x;
            }
        });

        try {
            zz.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.getCause();
        }
    }

    public void submitThenGetLamda() {

    }
}
