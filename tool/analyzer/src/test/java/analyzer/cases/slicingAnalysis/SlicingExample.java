package analyzer.cases.slicingAnalysis;

import java.io.IOException;

public class SlicingExample {
    private int a;
    private Object b;
    private IOException c;

    void write1(int a) {
        this.a = a;
    }

    void write2(Object b) {
        this.b = b;
    }

    boolean check1() {return b == null || a == 0;}

    boolean check2() {
        if (a == 1 && b != null) {
            c = null;
            return true;
        }
        return false;
    }

    void useCheck() {
        if (check1()) {
            c.printStackTrace();
        }
    }
}
