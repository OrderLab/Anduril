package analyzer.cases.exceptionHandlingAnalysis;

import java.io.IOException;

public class ExceptionExample {
    void simpleCaught () throws Exception{
        try {
            Exception e = new IOException();
            throw e;
        } catch (IOException f) {
            System.out.println("catched!");
        }
    }
}
