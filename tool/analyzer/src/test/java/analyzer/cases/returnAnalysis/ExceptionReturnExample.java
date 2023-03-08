package analyzer.cases.returnAnalysis;

import java.io.IOException;

public class ExceptionReturnExample {

    public IOException wrapper1(IOException e) {
        if (e != null) {
            return e;
        }
        return null;
    }

    public Throwable wrapper2(Exception e) {
        if (e != null) {
            Throwable buf1 = new IOException();
            return buf1;
        }
        Throwable buf2 = new SecurityException();
        return null;
    }

    public  Throwable wrapper3(IOException e) {
         if (e != null) {
             Throwable buf = wrapper1(e);
             return buf;
         }
         return null;
    }

    public  Throwable wrapper4(IOException e) {
        if (e != null) {
            Throwable buf = wrapper2(e);
            return buf;
        }
        return null;
    }

    // Cast case
    public  Throwable wrapper5(Exception e) {
        Throwable buf = wrapper1((IOException)e);
        return buf;
    }

    public  Throwable wrapper6(Exception e) {
        Throwable buf = wrapper2(e);
        return null;
    }

}
