package analyzer.baseline;

import analyzer.analysis.AnalysisInput;
import analyzer.analysis.BasicBlockAnalysis;
import analyzer.option.AnalyzerOptions;
import runtime.baseline.BaselineAgent;
import soot.*;
import soot.jimple.*;
import soot.tagkit.LineNumberTag;

import java.util.*;

public class BaselineAnalyzer {
    public static void run(final AnalyzerOptions options) {
        final Set<SootClass> classes = new HashSet<>();
        final List<SootClass> mainClasses = new ArrayList<>();
        for (final SootClass sootClass : Scene.v().getApplicationClasses()) {
            if (sootClass.getName().startsWith(AnalysisInput.prefix) ||
                    sootClass.getName().startsWith(AnalysisInput.secondaryPrefix)) {
                classes.add(sootClass);
                // find main classes
                if (sootClass.getName().equals(options.getMainClass())) {
                    mainClasses.add(sootClass);
                } else if (!options.isSecondaryMainClassListEmpty()) {
                    for (final String name : options.getSecondaryMainClassList()) {
                        if (sootClass.getName().equals(name)) {
                            mainClasses.add(sootClass);
                            break;
                        }
                    }
                }
            }
        }

        final SootClass agentClass = Scene.v().loadClassAndSupport(BaselineAgent.class.getCanonicalName());
        final SootMethod injectMethod = agentClass.getMethodByName("inject");
        final SootMethod initMethod = agentClass.getMethodByName("initStub");

        // init main
        if (AnalysisInput.distributedMode) {
            for (final SootClass c : mainClasses) {
                final SootMethod method = c.getMethodByName("main");
                final PatchingChain<Unit> units = method.retrieveActiveBody().getUnits();
                Unit head = units.getFirst();
                while (BasicBlockAnalysis.isLeadingStmt(head)) {
                    head = units.getSuccOf(head);
                }
                final StaticInvokeExpr initExpr =
                        Jimple.v().newStaticInvokeExpr(initMethod.makeRef(), new ArrayList<>());
                final InvokeStmt initStmt = Jimple.v().newInvokeStmt(initExpr);
                units.insertBefore(initStmt, head);
            }
        }

        // injections
        int injectionNumber = 0;
        for (final SootClass sootClass : classes) {
            final Collection<SootMethod> methods = sootClass.getMethods();
            for (final SootMethod sootMethod : methods) {
                if (sootMethod.hasActiveBody()) {
                    final PatchingChain<Unit> units = sootMethod.retrieveActiveBody().getUnits();
                    final List<Injection> injections = new ArrayList<>();
                    for (final Unit unit : units) {
                        for (final ValueBox valueBox : unit.getUseBoxes()) {
                            final Value value = valueBox.getValue();
                            if (value instanceof InvokeExpr) {
                                final SootMethod invocation = ((InvokeExpr) value).getMethod();
                                if (!classes.contains(invocation.getDeclaringClass())) {
                                    for (final SootClass exception : invocation.getExceptions()) {
                                        injections.add(new Injection(injectionNumber++,
                                                sootClass.getName(),
                                                sootMethod.getSubSignature(),
                                                invocation.getDeclaringClass().getName() + ": " + invocation.getSubSignature(),
                                                getLineNumber(unit),
                                                exception.getName(),
                                                unit));
                                    }
                                }
                            }
                        }
                    }
                    for (final Injection injection : injections) {
                        final StaticInvokeExpr injectExpr =
                                Jimple.v().newStaticInvokeExpr(injectMethod.makeRef(), injection.getArgs());
                        final InvokeStmt injectStmt = Jimple.v().newInvokeStmt(injectExpr);
                        units.insertBefore(injectStmt, injection.unit);
                    }
                }
            }
        }
        System.out.println("injection points: " + injectionNumber);
    }

    public static int getLineNumber(final Unit unit) {
        final LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
        if (tag == null) {
            return -1;
        }
        return tag.getLineNumber();
    }

    public static final class Injection {
        final int id;
        final String className;
        final String methodName;
        final String invocationName;
        final int line;
        final String exceptionName;

        final Unit unit;

        public Injection(int id, String className, String methodName, String invocationName, int line, String exceptionName, Unit unit) {
            this.id = id;
            this.className = className;
            this.methodName = methodName;
            this.invocationName = invocationName;
            this.line = line;
            this.exceptionName = exceptionName;
            this.unit = unit;
        }

        public List<Value> getArgs() {
            final List<Value> args = new ArrayList<>();
            args.add(IntConstant.v(id));
            args.add(StringConstant.v(className));
            args.add(StringConstant.v(methodName));
            args.add(StringConstant.v(invocationName));
            args.add(IntConstant.v(line));
            args.add(StringConstant.v(exceptionName));
            return args;
        }
    }
}
