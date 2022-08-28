package analyzer.cases.exceptionHandlingAnalysis;

import fj.data.IO;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ExceptionExample {
    ServerSocket ss;

    void simpleLocalCaught () throws Exception{
        try {
            Exception e = new IOException();
            throw e;
        } catch (IOException f) {
            System.out.println("catched!");
        }
        throw new IOException();
    }

    void complexLocalCaught() throws Exception{
        try{
            try {
                Exception e = new IOException();
                throw e;
            } catch (Exception f){
            throw f;
            }
        } catch (Exception g) {
            throw g;
        }
    }

    void internalCalling() throws Exception {
        try{
            try {
                complexLocalCaught();
            } catch (Exception f){
                throw f;
            }
        } catch (Exception g) {
            throw g;
        }
    }

    void externalCalling() throws Exception {
        try{
            try {
                Socket s = ss.accept();
            } catch (Exception f){
                throw f;
            }
        } catch (Exception g) {
            throw g;
        }
    }

    void simpleExceptionUncaught(int x) throws Exception {
        try{
            Exception e = new IOException();
            Exception f = new RuntimeException();
            if (x >= 3) {
                x = 7;
                throw e;
            } else if (x >= 0) {
                throw e;
            }
            throw f;
        } catch (RuntimeException g) {
            //doNothing
        }
        catch (Exception g){
            throw g;
        }
    }

    void complexExceptionUncaught(int x) throws Exception {
        try {
            try {
                throw new IOException();
            } catch (Exception e) {
                if (x >= 3) {
                    x = 7;
                    throw e;
                } else if (x >= 0) {
                    throw new RuntimeException();
                }
            }
        } catch (IOException f) {
            //do nothing
        }
    }
}
