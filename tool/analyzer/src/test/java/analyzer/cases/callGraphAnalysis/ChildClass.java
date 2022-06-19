package analyzer.cases.callGraphAnalysis;

public class ChildClass extends ParentClass {
    ChildClass(){
        System.out.println("Constructor of Child");
    }

    public void disp(){
        System.out.println("Child Method");
        //Calling the disp() method of parent class
    }
}
