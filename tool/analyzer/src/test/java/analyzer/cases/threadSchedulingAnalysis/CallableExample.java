package analyzer.cases.threadSchedulingAnalysis;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
        int x = 1;
        Future<Integer> zz = consume.submit(() -> {
    Socket s = ss.accept();
    return x;
});

        try {
            zz.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.getCause();
        }
    }

    public void arrayListInBetween() {
        int x = 1;
        List<Future<Integer>> uploads = new ArrayList<>();
        Future<Integer> upload = consume.submit(new Callable<Integer>() {
            @Override
            public Integer call()
                    throws IOException{
                Socket s = ss.accept();
                return x;
            }
        });
        uploads.add(upload);

        Future<Integer> upload1 = uploads.get(0);
        try {
            upload1.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.getCause();
        }
    }

    public void arrayInBetween() {
        int x = 1;
        Future[] uploads = new Future[2];
        Future<Integer> upload = consume.submit(new Callable<Integer>() {
            @Override
            public Integer call()
                    throws IOException{
                Socket s = ss.accept();
                return x;
            }
        });
        uploads[0] = upload;

        Future<Integer> upload1 = uploads[0];
        try {
            Integer p = upload1.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.getCause();
        }
    }
}
