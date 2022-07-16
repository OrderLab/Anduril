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
    public static final String prefix = System.getProperty("analysis.prefix", "org.apache.zookeeper");
    public static final String secondaryPrefix = System.getProperty("analysis.secondaryPrefix", "#");
    public static final boolean distributedMode = Boolean.getBoolean("analysis.distributedMode");

    public final Set<ProgramLocation> logEvents = new HashSet<>();
    public final List<SootClass> mainClasses = new ArrayList<>();

    //Easy constructor to use for test
    public AnalysisInput(IndexManager indexManager) {
        this.indexManager = indexManager;
        this.classes = new LinkedList<>(this.indexManager.classes.values());
        this.classes.sort(Comparator.comparing(SootClass::getName));
        this.classSet = new HashSet<>(this.classes);

    }

    public AnalysisInput(final AnalyzerOptions options, final Collection<SootClass> classes) {
        this.indexManager = new IndexManager(classes, prefix, secondaryPrefix);
        this.classes = new LinkedList<>(this.indexManager.classes.values());
        this.classes.sort(Comparator.comparing(SootClass::getName));
        this.classSet = new HashSet<>(this.classes);
        for (final SootClass c : classes) {
            if (c.getName().equals(options.getMainClass())) {
                mainClasses.add(c);
            } else if (!options.isSecondaryMainClassListEmpty()) {
                for (final String name : options.getSecondaryMainClassList()) {
                    if (c.getName().equals(name)) {
                        mainClasses.add(c);
                        break;
                    }
                }
            }
        }

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

        if (options.getFlakyCase().equals("hdfs-12070")) {
            this.testClass = Scene.v().getSootClass("org.apache.hadoop.hdfs.TestLeaseRecovery");
            this.testMethod = this.testClass.getMethod(
                    "void testBlockRecoveryRetryAfterFailedRecovery()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("fail") &&
                                inv.getDeclaringClass().getName().equals("org.junit.Assert")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("hdfs-12248")) {
            this.testClass = Scene.v().getSootClass("org.apache.hadoop.hdfs.TestRollingUpgrade");
            this.testMethod = this.testClass.getMethod(
                    "void queryForPreparation(org.apache.hadoop.hdfs.DistributedFileSystem)");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("fail") &&
                                inv.getDeclaringClass().getName().equals("org.junit.Assert")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("hdfs-4233")) {
            this.testClass = Scene.v().getSootClass("org.apache.hadoop.ipc.Server$Handler");
            this.testMethod = this.testClass.getMethod("void run()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("info") &&
                                inv.getDeclaringClass().getName().equals("org.apache.commons.logging.Log") &&
                                SootUtils.getLine(location.unit) == 1538) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("hdfs-15963")) {
            this.testClass = Scene.v().getSootClass("org.apache.hadoop.hdfs.TestDataTransferProtocol");
            this.testMethod = this.testClass.getMethod(
                    "void testReleaseVolumeRefIfExceptionThrown()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("fail") &&
                                inv.getDeclaringClass().getName().equals("org.junit.Assert")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("hbase-20492")) {
            this.testClass = Scene.v().getSootClass("org.apache.hadoop.hbase.master.assignment.TestUnexpectedStateException");
            this.testMethod = this.testClass.getMethod("void testUnableToAssign()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getDeclaringClass().getName().equals("org.apache.hadoop.hbase.master.assignment.TestUnexpectedStateException") &&
                                inv.getName().equals("foo")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("hbase-19608")) {
            this.testClass = Scene.v().getSootClass("org.apache.hadoop.hbase.client.TestGetProcedureResult");
            this.testMethod = this.testClass.getMethod(
                    "void testRace()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("fail") &&
                                inv.getDeclaringClass().getName().equals("org.junit.Assert")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("hbase-18137")) {
            this.testClass = Scene.v().getSootClass("org.apache.hadoop.hbase.replication.TestReplicationSmallTests");
            this.testMethod = this.testClass.getMethod("void testEmptyWALRecovery()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getDeclaringClass().getName().equals("java.lang.System") &&
                                inv.getName().equals("currentTimeMillis")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("hbase-25905")) {
            this.testClass = Scene.v().getSootClass("org.apache.hadoop.hbase.regionserver.wal.TestAsyncFSWALRollStuck");
            this.testMethod = this.testClass.getMethod("void testRoll()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getDeclaringClass().getName().equals("org.apache.hadoop.hbase.util.Bytes") &&
                                inv.getName().equals("toBytes")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("hbase-19893")) {
            this.testClass = Scene.v().getSootClass("org.apache.hadoop.hbase.client.TestRestoreSnapshotFromClient");
            this.testMethod = this.testClass.getMethod("void testRestoreSnapshotAfterSplittingRegions()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("fail") &&
                                inv.getDeclaringClass().getName().equals("org.junit.Assert")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("hbase-15252")) {
            this.testClass = Scene.v().getSootClass("org.apache.hadoop.hbase.regionserver.wal.TestWALReplay");
            this.testMethod = this.testClass.getMethod("void testDatalossWhenInputError()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
                for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("fail") &&
                                inv.getDeclaringClass().getName().equals("org.junit.Assert")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }

        if (options.getFlakyCase().equals("kafka-13419")) {
            this.testClass = Scene.v().getSootClass("org.apache.kafka.clients.consumer.internals.ConsumerCoordinatorTest");
            this.testMethod = this.testClass.getMethod("void testCommitOffsetIllegalGenerationShouldResetGenerationId()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
              for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("fail") &&
                                inv.getDeclaringClass().getName().equals("org.junit.jupiter.api.Assertions")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }
        if (options.getFlakyCase().equals("kafka-12508")) {
            this.testClass = Scene.v().getSootClass("org.apache.kafka.streams.integration.EmitOnChangeIntegrationTest");
            this.testMethod = this.testClass.getMethod("void shouldEmitSameRecordAfterFailover()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
              for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("dummy_sym")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }
        if (options.getFlakyCase().equals("kafka-10340")) {
            this.testClass = Scene.v().getSootClass("org.apache.kafka.connect.integration.ConnectWorkerIntegrationTest");
            this.testMethod = this.testClass.getMethod("void testSourceTaskNotBlockedOnShutdownWithNonExistentTopic()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
              for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("fail") &&
                                inv.getDeclaringClass().getName().equals("org.junit.Assert")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }                    
                    }
                }
            }
        }
        if (options.getFlakyCase().equals("kafka-9374")) {
            this.testClass = Scene.v().getSootClass("org.apache.kafka.connect.integration.BlockingConnectorTest");
            this.testMethod = this.testClass.getMethod("void testBlockInConnectorConfig()");
            for (final ProgramLocation location : indexManager.index.get(this.testClass).get(this.testMethod).values()) {
              for (final ValueBox valueBox : location.unit.getUseBoxes()) {
                    final Value value = valueBox.getValue();
                    if (value instanceof InvokeExpr) {
                        final SootMethod inv = ((InvokeExpr) value).getMethod();
                        if (inv.getName().equals("fail") &&
                                inv.getDeclaringClass().getName().equals("org.junit.Assert")) {
                            this.symptomEvent = new LocationEvent(location);
                            return;
                        }
                    }
                }
            }
        }
    }
}
