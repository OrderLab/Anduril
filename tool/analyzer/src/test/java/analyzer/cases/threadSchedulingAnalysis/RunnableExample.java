package analyzer.cases.threadSchedulingAnalysis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RunnableExample {
    ServerSocket ss;

    public void inner() {
        try {
            Socket s = ss.accept();
        } catch (IOException e) {
            throw new RuntimeException("GG");
        }
    }

    public void useMaybeMeasureLatency() {
        try {
            maybeMeasureLatency(() -> inner());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public static void maybeMeasureLatency(final Runnable actionToMeasure) {
        actionToMeasure.run();
    }
}
