package analyzer.stacktrace;

import analyzer.analysis.AnalysisInput;
import analyzer.analysis.BasicBlockAnalysis;
import analyzer.option.AnalyzerOptions;
import feedback.LogStatistics;
import feedback.parser.LogParser;
import runtime.stacktrace.StacktraceAgent;
import soot.*;
import soot.jimple.*;
import soot.tagkit.LineNumberTag;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class StackTraceAnalyzer {
    public static final String bad = System.getProperty("analysis.badLog", "#");
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

        final SootClass agentClass = Scene.v().loadClassAndSupport(StacktraceAgent.class.getCanonicalName());
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
        // Parser
        LogStatistics.StackTraceInjection[] injectionArray =
                LogStatistics.collectExceptionStackTrace(LogParser.parseLog(bad));
        // injections
        int injectionNumber = 0;
        final List<StackTraceAnalyzer.Injection> injections = new ArrayList<>();
        for (LogStatistics.StackTraceInjection record : injectionArray) {
            final SootClass sootClass = Scene.v().getSootClass(record.className());
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                if (sootMethod.hasActiveBody()) {
                    if (sootMethod.getName().equals(record.methodName())) {
                        final PatchingChain<Unit> units = sootMethod.retrieveActiveBody().getUnits();
                        for (final Unit unit : units) {
                            if (getLineNumber(unit) == record.lineNumber()) {
                                Injection ij = new Injection(injectionNumber, record, unit);
                                injections.add(ij);
                                injectionNumber++;
                                final StaticInvokeExpr injectExpr =
                                        Jimple.v().newStaticInvokeExpr(injectMethod.makeRef(), ij.getArgs());
                                final InvokeStmt injectStmt = Jimple.v().newInvokeStmt(injectExpr);
                                units.insertBefore(injectStmt, ij.unit);
                                break;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("injection points: " + injectionNumber);
        String path = "stacktrace.json";
        final JsonArrayBuilder injectionsJson = Json.createArrayBuilder();
        for (Injection injection : injections) {
            final JsonArrayBuilder stackTrace = Json.createArrayBuilder();
            for (int i = 0; i < injection.stackTrace.length; i++) {
                stackTrace.add(injection.stackTrace[i]);
            }
            injectionsJson.add(Json.createObjectBuilder()
                    .add("id", injection.id)
                    .add("exception", injection.exception)
                    .add("class", injection.className)
                    .add("method", injection.methodName)
                    .add("line", injection.lineNumber)
                    .add("stackTrace", stackTrace));
        }
        final JsonObject json = Json.createObjectBuilder()
                .add("injections", injectionsJson).build();
        final Map<String, Object> dump_options = new HashMap<>();
        dump_options.put(JsonGenerator.PRETTY_PRINTING, true);
        final JsonWriterFactory writerFactory = Json.createWriterFactory(dump_options);
        try (final FileWriter fw = new FileWriter(path);
             final JsonWriter jsonWriter = writerFactory.createWriter(fw)) {
            jsonWriter.writeObject(json);
        } catch (final IOException ignored) { }
    }

    public static int getLineNumber(final Unit unit) {
        final LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
        if (tag == null) {
            return -1;
        }
        return tag.getLineNumber();
    }

    public static String getFile(final String name) {
        if (name.contains("$")) {
            return name.substring(0, name.indexOf('$'));
        }
        return name;
    }

    public static final class Injection {
        final int id;
        final String exception;
        final String className;
        final String methodName;
        final String fileName;
        final int lineNumber;
        final String[] stackTrace;
        final Unit unit;

        public Injection(int id, LogStatistics.StackTraceInjection target, Unit unit) {
            this.id = id;
            this.exception = target.exception();
            this.className = target.className();
            this.methodName = target.methodName();
            this.fileName = target.fileName();
            this.lineNumber = target.lineNumber();
            this.stackTrace = target.stackTrace();
            this.unit = unit;
        }

        public List<Value> getArgs() {
            final List<Value> args = new ArrayList<>();
            args.add(IntConstant.v(id));
            args.add(StringConstant.v(exception));
            return args;
        }
    }
}
