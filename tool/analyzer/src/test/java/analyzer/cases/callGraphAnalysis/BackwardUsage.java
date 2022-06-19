package analyzer.cases.callGraphAnalysis;

public class BackwardUsage {
    void use1() {
        Person s = new ChildClass();
        s.disp();
    }
    void use2() {
        Person s = new ParentClass();
        s.disp();
    }
    void use3() {
        ParentClass s = new ChildClass();
        s.disp();
    }
    void use4() {
        ParentClass s = new ParentClass();
        s.disp();
    }
    void use5() {
        ChildClass s = new ChildClass();
        s.disp();
    }
}
