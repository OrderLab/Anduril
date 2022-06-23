package analyzer.cases.intraProceduralAnalysis;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class DominatorExample {
    ServerSocket ss;

    void controlFlow() {
        Random rand = new Random();
        int a = rand.nextInt();
        int b = rand.nextInt();
        int c = 0;
        if (a>b) {
            c = 1;
        } else {
            c = 2;
        }
        System.out.println(c);
    }

    void exceptionThrow () throws Exception{
        Random rand = new Random();
        int a = rand.nextInt();
        try {
            Socket s = ss.accept();
        } catch (Exception e){
            throw e;
        }
        System.out.println(a);
    }
}
