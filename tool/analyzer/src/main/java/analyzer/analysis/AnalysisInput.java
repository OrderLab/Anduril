package analyzer.analysis;

import analyzer.event.ConditionEvent;
import analyzer.event.LocationEvent;
import analyzer.event.ProgramEvent;
import analyzer.option.AnalyzerOptions;
import analyzer.util.SootUtils;
import index.IndexManager;
import index.ProgramLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;

import java.io.File;
import java.util.*;

public class AnalysisInput {
    private static final Logger LOG = LoggerFactory.getLogger(AnalysisInput.class);

    public final IndexManager indexManager;
    public final Set<SootClass> classSet; // for checking existence
    public final List<SootClass> classes; // for enumeration (in the order of class name)
    public ProgramEvent symptomEvent = null;
    public SootClass testClass = null;
    public SootMethod testMethod = null;
    public final String prefix;

    public final Set<ProgramLocation> logEvents = new HashSet<>();

    public AnalysisInput(final AnalyzerOptions options, final Collection<SootClass> classes, final String prefix) {
        this.prefix = prefix;
        this.indexManager = new IndexManager(classes, prefix);
        this.classes = new LinkedList<>(this.indexManager.classes.values());
        this.classes.sort(Comparator.comparing(SootClass::getName));
        this.classSet = new HashSet<>(this.classes);

        if (options.getDiffPath() != null) {
            final File diff_file = new File(options.getDiffPath());
            if (diff_file.exists()) {
                LOG.info("using diff log file {}", diff_file.getPath());
                try (final Scanner scanner = new Scanner(diff_file)) {
                    while (scanner.hasNext()) {
                        final String name = scanner.next();
                        final int line = scanner.nextInt();
                        final ProgramLocation loc = indexManager.logEntries.get(new IndexManager.LogEntry(name, line));
                        if (loc != null) {
//                        System.out.println(loc.dump().build().toString());
                            logEvents.add(loc);
                        } else {
//                        System.out.println("null");
                        }
                    }
                } catch (final Exception ignored) { }
            } else {
                LOG.info("diff log file {} not exists", diff_file.getPath());
            }
        }

        // TODO: make symptom configurable
//        this.testClass = Scene.v().getSootClass("org.apache.hadoop.io.retry.RetryInvocationHandler");
//        this.testMethod = this.testClass.getMethodByName("handleException");
//        for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
//            for (final ValueBox valueBox : location.unit.getUseBoxes()) {
//                final Value value = valueBox.getValue();
//                if (value instanceof InvokeExpr) {
//                    if (SootUtils.getLine(location.unit) == 379) {
//                        this.symptomEvent = new ConditionEvent(location, false, ((InvokeExpr) value).getArg(0));
//                        return;
//                    }
//                }
//            }
//        }

//        this.testClass = Scene.v().getSootClass("org.apache.hadoop.io.retry.RetryInvocationHandler");
//        org.apache.zookeeper.KeeperException


//        this.testClass = Scene.v().getSootClass("org.apache.zookeeper.server.quorum.MultipleAddressesTest");
//        this.testMethod = this.testClass.getMethod("void testGetValidAddressWithNotValid()");
//        for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
//            for (final ValueBox valueBox : location.unit.getUseBoxes()) {
//                final Value value = valueBox.getValue();
//                if (value instanceof InvokeExpr) {
//                    if (((InvokeExpr) value).getMethod().getName().equals("assertTrue")) {
//                        this.symptomEvent = new ConditionEvent(location, false, ((InvokeExpr) value).getArg(0));
//                        return;
//                    }
//                }
//            }
//        }

//        this.testClass = Scene.v().getSootClass("org.apache.zookeeper.server.quorum.FuzzySnapshotRelatedTest");
//        this.testMethod = this.testClass.getMethod("void testPZxidUpdatedWhenLoadingSnapshot()");

        if (options.getFlakyCase().equals("zookeeper-4203")) {
            this.testClass = Scene.v().getSootClass("org.apache.zookeeper.server.quorum.LeaderLeadingStateTest");
            this.testMethod = this.testClass.getMethod("void leadingStateTest()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof NewExpr) {
                        if (((NewExpr) value).getBaseType().getClassName().equals("java.lang.IllegalStateException")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("zookeeper-3006")) {
            this.testClass = Scene.v().getSootClass("org.apache.zookeeper.server.ZKDatabase");
            this.testMethod = this.testClass.getMethod("long calculateTxnLogSizeLimit()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof NewExpr) {
                        if (((NewExpr) value).getBaseType().getClassName().equals("java.lang.NullPointerException")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("zookeeper-3157")) {
            this.testClass = Scene.v().getSootClass("org.apache.zookeeper.ZooKeeper");
            this.testMethod = this.testClass.getMethod(
                    "byte[] getData(java.lang.String,org.apache.zookeeper.Watcher,org.apache.zookeeper.data.Stat)");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("create") &&
                                inv.getDeclaringClass().getName().equals("org.apache.zookeeper.KeeperException")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("zookeeper-2247")) {
            this.testClass = Scene.v().getSootClass("org.apache.zookeeper.ZooKeeper");
            this.testMethod = this.testClass.getMethod(
                    "java.lang.String create(java.lang.String,byte[],java.util.List,org.apache.zookeeper.CreateMode)");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("create") &&
                                inv.getDeclaringClass().getName().equals("org.apache.zookeeper.KeeperException")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }
    }
}
