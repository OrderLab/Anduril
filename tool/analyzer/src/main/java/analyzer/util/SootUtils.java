package analyzer.util;

import heros.solver.CountingThreadPoolExecutor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.Modifier;
import soot.PackManager;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Transform;
import soot.Transformer;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.baf.BafASMBackend;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.JasminClass;
import soot.jimple.NewArrayExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.util.Chain;
import soot.util.JasminOutputStream;

/**
 * A collection of helper functions for using Soot (main code snippets are from AutoWatchdog project).
 */
public class SootUtils {
    //private static final Logger LOG = LoggerFactory.getLogger(SootUtils.class);

    public static Map<String, Integer> UniqueMethodNameCountMap = new HashMap<>();

    /**
     * Get line number of a Soot unit
     */
    public static int getLine(Unit unit) {
        int line = -1;
        LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
        if (tag != null) {
            line = tag.getLineNumber();
        }
        return line;
    }

    private static void outputSootClassJimple(SootClass sootClass, PrintStream out) {

        out.println("public class " + sootClass.toString());
        out.println("{");
        for (SootField f : sootClass.getFields()) {
            out.println("    " + f.getDeclaration().toString());
        }
        out.println("");
        for (SootMethod m : sootClass.getMethods()) {
            if (m.hasActiveBody()) {
                out.println("    " + m.getActiveBody().toString());
            } else {
                out.println("    " + m.toString() + " [no active body found]");
            }
        }
        out.println("}");
    }

    public static void printSootClassJimple(SootClass sootClass) {
        outputSootClassJimple(sootClass, System.out);
    }

    public static void dumpSootClassJimple(SootClass sootClass) {
        PrintStream out;
        String fileName = SourceLocator.v().getFileNameFor(sootClass, Options.output_format_jimple);
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        try {
            out = new PrintStream(file);
            outputSootClassJimple(sootClass, out);
            //LOG.info("Writing class " + sootClass.getName() + " to " + fileName);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void printBodyJimple(Body body) {
        System.out.println(body.toString());
    }

    public static void dumpBodyJimple(Body body) {
        String fileName =
                body.getMethod().getDeclaringClass().getName() + "_" + body.getMethod().getName()
                        + ".jimple";
        //LOG.info(
        //        "Writing method " + body.getMethod().getName() + " to " + SourceLocator.v()
        //                .getOutputDir() + "/" + fileName);
        File file = new File(SourceLocator.v().getOutputDir(), fileName);
        file.getParentFile().mkdirs();
        try {
            PrintStream out = new PrintStream(file);
            out.println(body.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void dumpSootClass(SootClass sClass) {
        // Since it is .class we are generating, we must validate its integrity before dumping.
        sClass.validate();
        String fileName = SourceLocator.v().getFileNameFor(sClass, Options.output_format_class);
        //LOG.info("Writing class " + sClass.getName() + " to " + fileName);
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        try {
            OutputStream streamOut = new FileOutputStream(file);
            if (Options.v().jasmin_backend()) {
                streamOut = new JasminOutputStream(streamOut);
            }
            PrintWriter writerOut = new PrintWriter(
                    new OutputStreamWriter(streamOut));
            if (!Options.v().jasmin_backend()) {
                new BafASMBackend(sClass, Options.v().java_version()).generateClassFile(streamOut);
            } else {
                JasminClass jasminClass = new soot.jimple.JasminClass(sClass);
                jasminClass.print(writerOut);
            }
            writerOut.flush();
            streamOut.close();
            writerOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void tryDumpSootClass(SootClass sClass) {
        try {
            SootUtils.dumpSootClass(sClass);
        } catch (Exception e) {
            e.printStackTrace();
            SootUtils.printSootClassJimple(sClass);
            System.exit(-1);
        }
    }

    public static void SecureExportSootClass(SootClass sClass) {
        //SootUtils.printSootClassJimple(contextClass);
        SootUtils.dumpSootClassJimple(sClass);
        sClass.validate();
        SootUtils.tryDumpSootClass(sClass);
    }

    public static boolean isPrimJavaType(Type type) {
        //we need to take String into consideration, which type not provided by soot primtype
        if (type.equals(ShortType.v()) || type.equals(ByteType.v()) ||
                type.equals(BooleanType.v()) || type.equals(CharType.v()) ||
                type.equals(IntType.v()) || type.equals(LongType.v()) ||
                type.equals(FloatType.v()) || type.equals(DoubleType.v())) {
            return true;
        }

        if (type.equals(RefType.v("java.lang.String"))) {
            return true;
        }

        return false;
    }

    public static boolean isThreadOrRunnable(SootClass sClass) {
        final SootClass sp = sClass.getSuperclass();
        if (sp.getName().equals("java.lang.Thread")) {
            return true;
        }
        Iterator<SootClass> scit = sClass.getInterfaces().iterator();
        while (scit.hasNext()) {
            final SootClass si = scit.next();
            if (si.getName().equals("java.lang.Runnable")) {
                return true;
            }
        }
        return false;
    }

    public static RefType boxBasicJavaType(Type type) {
        if (type.equals(BooleanType.v())) {
            return RefType.v("java.lang.Boolean");
        } else if (type.equals(ByteType.v())) {
            return RefType.v("java.lang.Byte");
        } else if (type.equals(CharType.v())) {
            return RefType.v("java.lang.Character");
        } else if (type.equals(FloatType.v())) {
            return RefType.v("java.lang.Float");
        } else if (type.equals(IntType.v())) {
            return RefType.v("java.lang.Integer");
        } else if (type.equals(LongType.v())) {
            return RefType.v("java.lang.Long");
        } else if (type.equals(ShortType.v())) {
            return RefType.v("java.lang.Short");
        } else if (type.equals(DoubleType.v())) {
            return RefType.v("java.lang.Double");
        } else {
            throw new RuntimeException("New type not supported:" + type.toString());
        }
    }

    public static String getUniqueMethodName(SootMethod method) {
        String methodName = method.getName();
        return getUniqueMethodName(methodName, method.getNumberedSubSignature().toString());
    }

    public static String getUniqueMethodName(String str, String strToHash) {
        String methodName = str;
        //use hashcode to avoid generate same name reduced functions for same name func with different parameters
        int hashcode = strToHash.hashCode();
        //make sure method name is legal
        methodName = (str + "_" + hashcode).replaceAll("[^a-zA-Z0-9_]", "");

        return methodName;
    }

    public static String getIncrementalUniqueMethodName(SootMethod method) {
        String methodName = method.getName();
        methodName = getUniqueMethodName(methodName, method.getNumberedSubSignature().toString());
        //since a method could have multiple critical operations inside, we should support number
        int count = 0;
        if (UniqueMethodNameCountMap.containsKey(methodName)) {
            count = UniqueMethodNameCountMap.get(methodName) + 1;
        } else {
            count = 0;
        }
        UniqueMethodNameCountMap.put(methodName, count);

        methodName = methodName + "_" + Integer.toString(count);
        return methodName;
    }

    public static NewArrayExpr retrieveNewArrayExpr(Stmt stmt) {
        if (stmt instanceof AssignStmt) {
            Value rightOp = ((AssignStmt) stmt).getRightOp();
            if (rightOp instanceof NewArrayExpr) {
                return (NewArrayExpr) rightOp;
            }
        }
        return null;
    }

    public static ArrayRef retrieveArrayRefExpr(Stmt stmt) {
        if (stmt instanceof AssignStmt) {
            Value rightOp = ((AssignStmt) stmt).getRightOp();
            if (rightOp instanceof ArrayRef) {
                return (ArrayRef) rightOp;
            }
        }
        return null;
    }

    public static void tryMakeClassPublic(SootClass c) {
        if (c.isPrivate()) {
            c.setModifiers(c.getModifiers() - Modifier.PRIVATE);
        }

        if (c.isProtected()) {
            c.setModifiers(c.getModifiers() - Modifier.PROTECTED);
        }

        if (!c.isPublic()) {
            c.setModifiers(c.getModifiers() | Modifier.PUBLIC);
        }
    }

    public static void tryMakeMethodPublic(SootMethod m) {

        //workaround to avoid hbase startup
        // Unhandled: Region server startup failed
        // Caused by: java.lang.ClassFormatError: Method start in class org/apache/hadoop/hbase/replication/ReplicationEndpoint has illegal modifiers: 0x1

        if (m.getDeclaringClass().getName()
                .equals("org.apache.hadoop.hbase.replication.ReplicationEndpoint")
                && m.getName().equals("start")) {
            return;
        }

        //workaround for kafka startup
        // unhandled: java.lang.ClassFormatError: Method $greater$eq in class kafka/api/ApiVersion has illegal modifiers: 0x1
        if(m.getDeclaringClass().getName().equals("kafka/api/ApiVersion") ||
                m.getDeclaringClass().getName().equals("kafka.api.ApiVersion")) {
            return;
        }

        try {
            if (m.isPrivate()) {
                m.setModifiers(m.getModifiers() - Modifier.PRIVATE);
            }
            if (m.isProtected()) {
                m.setModifiers(m.getModifiers() - Modifier.PROTECTED);
            }
            if (!m.isPublic()) {
                m.setModifiers(m.getModifiers() | Modifier.PUBLIC);
            }
        } catch (Exception ex) {
            //LOG.warn("cannot make " + SootUtils.getSimpleSignature(m) + " public");
        }
    }

    public static String getSimpleSignature(SootMethod method) {
        return method.getDeclaringClass().getName() + "@" + method.getNumberedSubSignature()
                .toString();
    }

    public static SootClass loadClassAndRetrieveMethods(String className) {
        SootClass targetClass = Scene.v()
                .loadClassAndSupport(className);
        targetClass.setApplicationClass();
        for (SootMethod origMethod : targetClass.getMethods()) {
            //loadClassAndSupport() only create skeleton of class, we need to actually retrieve method body
            origMethod.retrieveActiveBody();
        }

        return targetClass;
    }

    public static interface SootMethodVisitor {

        void visit(SootMethod method);
    }

    /**
     * terate all methods in a given class and apply a function on each method.
     *
     * @param c given soot class
     * @param visitor function to be applied to all methods in c
     */
    public static void iterateAllMethods(SootClass c, final SootMethodVisitor visitor) {
        for (SootMethod m : c.getMethods()) {
            visitor.visit(m);
        }
    }

    /**
     * Iterate all methods in the specified list of classes and apply a function on each method.
     * Note: the iteration will be performed concurrently in a non-thread-safe way.
     */
    public static void iterateAllMethods(final Iterator<SootClass> classes,
            final SootMethodVisitor visitor) {
        int threadNum = Runtime.getRuntime().availableProcessors();
        CountingThreadPoolExecutor executor = new CountingThreadPoolExecutor(threadNum,
                threadNum, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        while (classes.hasNext()) {
            final SootClass c = classes.next();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    iterateAllMethods(c, visitor);
                }

            });
        }
        // Wait till all packs have been executed
        try {
            executor.awaitCompletion();
            executor.shutdown();
        } catch (InterruptedException e) {
            // Something went horribly wrong
            throw new RuntimeException(
                    "Could not wait for pack threads to " + "finish: " + e.getMessage(), e);
        }

        // If something went wrong, we tell the world
        if (executor.getException() != null) {
            if (executor.getException() instanceof RuntimeException) {
                throw (RuntimeException) executor.getException();
            } else {
                throw new RuntimeException(executor.getException());
            }
        }
    }

    /**
     * Add a new phase into a phase pack in Soot
     *
     * @return the new phase added
     */
    public static Transform addNewTransform(String phasePackName, String phaseName,
            Transformer transformer) {
        Transform phase = new Transform(phaseName, transformer);
        phase.setDeclaredOptions("enabled");
        phase.setDefaultOptions("enabled:false");
        PackManager.v().getPack(phasePackName).add(phase);
        return phase;
    }

    public static List<SootClass> getSubClass(SootClass superClass) {
        List<SootClass> lst = new ArrayList<>();
        for (SootClass c : Scene.v().getApplicationClasses()) {
            SootClass it = c;
            if (it.hasSuperclass()) {
                if (it.getSuperclass().getName().equals(superClass.getName())) {
                    lst.add(c);
                }
            }
        }

        return lst;
    }

    /**
     * check if a class has xx method
     * @param clazz target class
     * @return true if it has
     */
    public static boolean hasMethod(SootClass clazz, String methodName)
    {
        // we check methods one by one ourselves, why not use Soot getMethodByName()?
        // because that method could throw two exceptions for ambigious methods or not found
        // which is stupid since we need to differ whether it abort for having no method
        // or two method with same name..
        for(SootMethod m:clazz.getMethods()) {
            if(m.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * return all methods from a class named xx
     * @param clazz target class
     * @return method list
     */
    public static List<SootMethod> getMethodByNameIgnoreAmbiguous(SootClass clazz, String methodName)
    {
        List<SootMethod> methods = new ArrayList<>();
        for(SootMethod m:clazz.getMethods()) {
            if(m.getName().equals(methodName)) {
                methods.add(m);
            }
        }
        return methods;
    }

    /**
     * get the base from invoker expr, e.g. base.doIO() -> base
     *
     * @param stmt invokerstmt
     * @return base
     */
    public static Value getBaseFromInvokerExpr(Stmt stmt) {
        Value base = null;
        if (stmt.getInvokeExpr() instanceof VirtualInvokeExpr) {
            base = ((VirtualInvokeExpr) stmt.getInvokeExpr()).getBase();
        } else if (stmt.getInvokeExpr() instanceof SpecialInvokeExpr) {
            base = ((SpecialInvokeExpr) stmt.getInvokeExpr()).getBase();
        } else if (stmt.getInvokeExpr() instanceof InterfaceInvokeExpr) {
            base = ((InterfaceInvokeExpr) stmt.getInvokeExpr()).getBase();
        } else {
            throw new RuntimeException(
                    "InvokeExpr type not supported\n stmt:" + stmt);
        }
        return base;
    }
}
