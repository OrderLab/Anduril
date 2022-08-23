package feedback.parser;

import feedback.common.ThreadTestBase;
import feedback.log.LogFile;
import feedback.log.LogTestUtil;
import feedback.log.entry.LogEntry;
import feedback.log.entry.LogEntryBuilders;
import feedback.log.entry.LogType;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

final class ParserTest extends ThreadTestBase {
    private static final Random random = new Random(System.currentTimeMillis());

    private static void testDatetime(String datetimeText, int y, int m, int d, int hr, int min, int s, int ms) {
        assertEquals(new DateTime(y, m, d, hr, min, s, ms), LogFileParser.parseDatetime(datetimeText));
    }

    @Test
    void testDatetime() {
        testDatetime("2021-08-20 01:04:01,335", 2021, 8, 20, 1, 4, 1, 335);
        testDatetime("2022-02-28 22:39:19,721", 2022, 2, 28, 22, 39, 19, 721);
    }

    @Test
    void testWrongDataTime() {
        assertThrows(java.lang.IllegalArgumentException.class,
                () -> testDatetime("2016-06-12", 2016, 6, 12, 0, 0, 0, 0));
        assertThrows(java.lang.IllegalArgumentException.class,
                () -> testDatetime("2012-01-31 23:59:59.999", 2012, 1, 31, 23, 59, 59, 999));
    }

    @Test
    void testLogType() {
        assertEquals(LogType.INFO, LogFileParser.parseLogType("INFO "));
        assertEquals(LogType.WARN, LogFileParser.parseLogType("WARN "));
        assertEquals(LogType.ERROR, LogFileParser.parseLogType("ERROR"));
        assertEquals(LogType.DEBUG, LogFileParser.parseLogType("DEBUG"));
        assertEquals(LogType.TRACE, LogFileParser.parseLogType("TRACE"));
    }

    @Test
    void testWrongLogType() {
        assertThrows(scala.MatchError.class, () -> LogFileParser.parseLogType("INFI "));
        assertThrows(scala.MatchError.class, () -> LogFileParser.parseLogType("TRACK"));
    }

    private static void testLocation(String locationText, String thread, String file, int line) {
        assertEquals(new scala.Tuple3<>(thread, file, line), LogFileParser.parseLocation(locationText));
    }

    @Test
    void testLocation() {
        // ZooKeeper
        testLocation("[main:JUnit4ZKTestRunner@47]", "main", "JUnit4ZKTestRunner", 47);
        testLocation("[main:ZKTestCase$1@55]", "main", "ZKTestCase$1", 55);
        testLocation("[main:JUnit4ZKTestRunner$LoggedInvokeMethod@77]", "main", "JUnit4ZKTestRunner$LoggedInvokeMethod", 77);
        testLocation("[Thread-0:QuorumPeerConfig@116]", "Thread-0", "QuorumPeerConfig", 116);
        testLocation("[/127.0.0.1:11225:QuorumCnxManager$Listener@637]", "/127.0.0.1:11225", "QuorumCnxManager$Listener", 637);
        testLocation("[NIOServerCxnFactory.AcceptThread:/0.0.0.0:11222:NIOServerCnxnFactory$AcceptThread@296]",
                "NIOServerCxnFactory.AcceptThread:/0.0.0.0:11222", "NIOServerCnxnFactory$AcceptThread", 296);
        testLocation("[QuorumPeer[myid=1](plain=/0:0:0:0:0:0:0:0:11222)(secure=disabled):MBeanRegistry@128]",
                "QuorumPeer[myid=1](plain=/0:0:0:0:0:0:0:0:11222)(secure=disabled)", "MBeanRegistry", 128);
        testLocation("[main-SendThread(127.0.0.1:11222):ClientCnxn$SendThread@1113]", "main-SendThread(127.0.0.1:11222)", "ClientCnxn$SendThread", 1113);

        // HBase
        testLocation("[Time-limited test:FSNamesystem$SafeModeInfo@5141]", "Time-limited test", "FSNamesystem$SafeModeInfo", 5141);
        testLocation("[FSImageSaver for /home/haoze/flaky-reproduction/experiment/hbase-20492/target/test-data/df65f1f3-b082-4335-8daa-d1218dc08251/cluster_469379d5-9a88-483a-9755-f4341c2be6f4/dfs/name1 of type IMAGE_AND_EDITS:FSImageFormatProtobuf$Saver@413]",
                "FSImageSaver for /home/haoze/flaky-reproduction/experiment/hbase-20492/target/test-data/df65f1f3-b082-4335-8daa-d1218dc08251/cluster_469379d5-9a88-483a-9755-f4341c2be6f4/dfs/name1 of type IMAGE_AND_EDITS",
                "FSImageFormatProtobuf$Saver", 413);
        testLocation("[DataNode: [[[DISK]file:/home/haoze/flaky-reproduction/experiment/hbase-20492/target/test-data/df65f1f3-b082-4335-8daa-d1218dc08251/cluster_469379d5-9a88-483a-9755-f4341c2be6f4/dfs/data/data1/, [DISK]file:/home/haoze/flaky-reproduction/experiment/hbase-20492/target/test-data/df65f1f3-b082-4335-8daa-d1218dc08251/cluster_469379d5-9a88-483a-9755-f4341c2be6f4/dfs/data/data2/]]  heartbeating to localhost/127.0.0.1:36939:DataStorage@362]",
                "DataNode: [[[DISK]file:/home/haoze/flaky-reproduction/experiment/hbase-20492/target/test-data/df65f1f3-b082-4335-8daa-d1218dc08251/cluster_469379d5-9a88-483a-9755-f4341c2be6f4/dfs/data/data1/, [DISK]file:/home/haoze/flaky-reproduction/experiment/hbase-20492/target/test-data/df65f1f3-b082-4335-8daa-d1218dc08251/cluster_469379d5-9a88-483a-9755-f4341c2be6f4/dfs/data/data2/]]  heartbeating to localhost/127.0.0.1:36939",
                "DataStorage", 362);
        testLocation("[IPC Server handler 7 on 36939:EditLogFileOutputStream@200]", "IPC Server handler 7 on 36939","EditLogFileOutputStream", 200);
        testLocation("[IPC Server handler 8 on 36939:FSNamesystem$DefaultAuditLogger@8254]", "IPC Server handler 8 on 36939", "FSNamesystem$DefaultAuditLogger", 8254);
        testLocation("[NIOServerCxn.Factory:0.0.0.0/0.0.0.0:63011:NIOServerCnxnFactory@192]", "NIOServerCxn.Factory:0.0.0.0/0.0.0.0:63011", "NIOServerCnxnFactory", 192);
        testLocation("[ReadOnlyZKClient-localhost:63011@0x0b2b38cf-SendThread(localhost:63011):ClientCnxn$SendThread@1299]", "ReadOnlyZKClient-localhost:63011@0x0b2b38cf-SendThread(localhost:63011)", "ClientCnxn$SendThread", 1299);
        testLocation("[RS_CLOSE_REGION-regionserver/razor8:0-1:HRegion@1665]", "RS_CLOSE_REGION-regionserver/razor8:0-1", "HRegion", 1665);

        // HDFS
        testLocation("[Socket Reader #1 for port 11036:Server$Listener$Reader@1067]", "Socket Reader #1 for port 11036", "Server$Listener$Reader", 1067);
        testLocation("[org.apache.hadoop.util.JvmPauseMonitor$Monitor@168dbf3:JvmPauseMonitor$Monitor@188]", "org.apache.hadoop.util.JvmPauseMonitor$Monitor@168dbf3", "JvmPauseMonitor$Monitor", 188);
        testLocation("[1437941060@qtp-131635550-0:TransferFsImage@65]", "1437941060@qtp-131635550-0", "TransferFsImage", 65);
        testLocation("[asdf\nqwer\nzxcv:Bar$@999]", "asdf\nqwer\nzxcv", "Bar$", 999);
    }

    private static void testLogEntry(String text, String datetime, String type, String location, String msg) {
        final int logLine = random.nextInt(1_000_000) + 1;
        final LogEntry expected = LogEntryBuilders.create(logLine, datetime, type, location, msg).build();
        final LogEntry actual = LogFileParser.parseLogEntry(text, logLine).get()._2.build();
        assertEquals(expected.showtime(), actual.showtime());
        assertEquals(expected.logType(), actual.logType());
        assertEquals(expected.thread(), actual.thread());
        assertEquals(expected.classname(), actual.classname());
        assertEquals(expected.fileLogLine(), actual.fileLogLine());
        assertEquals(expected.msg(), actual.msg());
    }

    @Test
    void testLogEntry() {
        testLogEntry("2022-05-17 19:15:39,381 - INFO  [main:HostsFileReader@85] - Refreshing hosts (include/exclude) list",
                "2022-05-17 19:15:39,381", "INFO ", "[main:HostsFileReader@85]", "Refreshing hosts (include/exclude) list");
        testLogEntry("2022-05-17 19:15:38,888 - INFO  [main:StringUtils@597] - STARTUP_MSG:\n" +
                        "/************************************************************\n" +
                        "STARTUP_MSG: Starting NameNode\n" +
                        "STARTUP_MSG:   host = razor2/10.0.0.202\n" +
                        "STARTUP_MSG:   args = []\n" +
                        "STARTUP_MSG:   version = 0.23.6-SNAPSHOT\n" +
                        "STARTUP_MSG:   classpath = /home/haoze/\n" +
                        "STARTUP_MSG:   build = git://razor2/home/haoze/flaky-reproduction/systems/hdfs-4233/hadoop-common-project/hadoop-common -r d599b2f5ffef7b475e5c9f0a3a39099cc31e2521; compiled by 'haoze' on Tue May 17 18:23:30 EDT 2022\n" +
                        "STARTUP_MSG:   java = 1.8.0_275",
                "2022-05-17 19:15:38,888", "INFO ", "[main:StringUtils@597]", "STARTUP_MSG:\n" +
                        "/************************************************************\n" +
                        "STARTUP_MSG: Starting NameNode\n" +
                        "STARTUP_MSG:   host = razor2/10.0.0.202\n" +
                        "STARTUP_MSG:   args = []\n" +
                        "STARTUP_MSG:   version = 0.23.6-SNAPSHOT\n" +
                        "STARTUP_MSG:   classpath = /home/haoze/\n" +
                        "STARTUP_MSG:   build = git://razor2/home/haoze/flaky-reproduction/systems/hdfs-4233/hadoop-common-project/hadoop-common -r d599b2f5ffef7b475e5c9f0a3a39099cc31e2521; compiled by 'haoze' on Tue May 17 18:23:30 EDT 2022\n" +
                        "STARTUP_MSG:   java = 1.8.0_275");
        testLogEntry("2021-08-20 01:04:01,341 [myid:] - INFO  [main:JUnit4ZKTestRunner@47] - No test.method specified. using default methods.",
                "2021-08-20 01:04:01,341", "INFO ", "[main:JUnit4ZKTestRunner@47]", "No test.method specified. using default methods.");
        testLogEntry("2021-08-20 01:04:01,539 [myid:2] - ERROR [Thread-1:ManagedUtil@114] - Problems while registering log4j jmx beans!\n" +
                        "javax.management.InstanceAlreadyExistsException: log4j:hiearchy=default\n" +
                        "\tat com.sun.jmx.mbeanserver.Repository.addMBean(Repository.java:437)\n" +
                        "\tat com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.registerWithRepository(DefaultMBeanServerInterceptor.java:1898)\n" +
                        "\tat com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.registerDynamicMBean(DefaultMBeanServerInterceptor.java:966)\n" +
                        "\tat com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.registerObject(DefaultMBeanServerInterceptor.java:900)\n" +
                        "\tat com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.registerMBean(DefaultMBeanServerInterceptor.java:324)\n" +
                        "\tat com.sun.jmx.mbeanserver.JmxMBeanServer.registerMBean(JmxMBeanServer.java:522)\n" +
                        "\tat org.apache.zookeeper.jmx.ManagedUtil.registerLog4jMBeans(ManagedUtil.java:75)\n" +
                        "\tat org.apache.zookeeper.server.quorum.QuorumPeerMain.runFromConfig(QuorumPeerMain.java:131)\n" +
                        "\tat org.apache.zookeeper.server.quorum.QuorumPeerMain.initializeAndRun(QuorumPeerMain.java:120)\n" +
                        "\tat org.apache.zookeeper.server.quorum.QuorumPeerTestBase$MainThread.run(QuorumPeerTestBase.java:245)\n" +
                        "\tat java.lang.Thread.run(Thread.java:748)", "2021-08-20 01:04:01,539", "ERROR", "[Thread-1:ManagedUtil@114]",
                "Problems while registering log4j jmx beans!\n" +
                        "javax.management.InstanceAlreadyExistsException: log4j:hiearchy=default\n" +
                        "\tat com.sun.jmx.mbeanserver.Repository.addMBean(Repository.java:437)\n" +
                        "\tat com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.registerWithRepository(DefaultMBeanServerInterceptor.java:1898)\n" +
                        "\tat com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.registerDynamicMBean(DefaultMBeanServerInterceptor.java:966)\n" +
                        "\tat com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.registerObject(DefaultMBeanServerInterceptor.java:900)\n" +
                        "\tat com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.registerMBean(DefaultMBeanServerInterceptor.java:324)\n" +
                        "\tat com.sun.jmx.mbeanserver.JmxMBeanServer.registerMBean(JmxMBeanServer.java:522)\n" +
                        "\tat org.apache.zookeeper.jmx.ManagedUtil.registerLog4jMBeans(ManagedUtil.java:75)\n" +
                        "\tat org.apache.zookeeper.server.quorum.QuorumPeerMain.runFromConfig(QuorumPeerMain.java:131)\n" +
                        "\tat org.apache.zookeeper.server.quorum.QuorumPeerMain.initializeAndRun(QuorumPeerMain.java:120)\n" +
                        "\tat org.apache.zookeeper.server.quorum.QuorumPeerTestBase$MainThread.run(QuorumPeerTestBase.java:245)\n" +
                        "\tat java.lang.Thread.run(Thread.java:748)");
        testLogEntry(".2021-08-20 01:04:01,352 [myid:] - INFO  [main:ZKTestCase$1@55] - STARTING testQuorum",
                "2021-08-20 01:04:01,352", "INFO ", "[main:ZKTestCase$1@55]", "STARTING testQuorum");
        testLogEntry(".2021-08-09 16:30:30,110 - INFO  [main:ZKTestCase$1@58] - STARTING testPZxidUpdatedWhenLoadingSnapshot",
                "2021-08-09 16:30:30,110", "INFO ", "[main:ZKTestCase$1@58]",
                "STARTING testPZxidUpdatedWhenLoadingSnapshot");
        testLogEntry(".2022-06-19 21:59:50,945 - INFO  [RS-EventLoopGroup-1-3:ServerRpcConnection@528] - Auth successful for haoze (auth:SIMPLE)",
                "2022-06-19 21:59:50,945", "INFO ", "[RS-EventLoopGroup-1-3:ServerRpcConnection@528]", "Auth successful for haoze (auth:SIMPLE)");
    }

    @Test
    void testLog() throws IOException {
        final LogFile zookeeper_3157 = LogTestUtil.getLogFile("ground-truth/zookeeper-3157/bad-run-log.txt");
        assertEquals("JUnit version 4.12\n", zookeeper_3157.header().get());
        assertEquals(513, zookeeper_3157.entries().length);
        assertEquals("No test.method specified. using default methods.", zookeeper_3157.entries()[0].msg());
        assertEquals(58, zookeeper_3157.entries()[2].fileLogLine());
        assertEquals(3, zookeeper_3157.entries()[1].logLine());
        assertEquals(111, zookeeper_3157.entries()[72].logLine());

        final LogFile hdfs_12248 = LogTestUtil.getLogFile("ground-truth/hdfs-12248/good-run-log.txt");
        assertTrue(hdfs_12248.header().get().startsWith("JUnit version 4.11\n"));
        assertTrue(hdfs_12248.header().get().endsWith("\nSLF4J: Actual binding is of type [org.slf4j.impl.Log4jLoggerFactory]\n"));

        final LogFile hbase_20492 = LogTestUtil.getLogFile("ground-truth/hbase-20492/good-run-log.txt");
        assertEquals(1468, hbase_20492.entries().length);
        assertEquals(hbase_20492.entries()[1464].logLine(), 1467);
    }

    @Test
    void testLogHeader() {
        final LogFile logFile = LogFileParser.parse(new String[]{
                "asdf",
                "qwer2021-08-20 01:04:01,539 [myid:2] - ERROR [Thread-1:ManagedUtil@114] - Problems",
                "2021-08-20 01:04:01,352 [myid:] - INFO  [main:ZKTestCase$1@55] - STARTING",
                ".2021-08-09 16:30:30,110 - INFO  [main:ZKTestCase$1@58] - STARTING",
        })._1;
        assertEquals("asdf\nqwer", logFile.header().get());
    }

    @Test
    void testLoadFile() throws IOException {
        final String[] zookeeper_3157 = LogTestUtil.getFileLines("ground-truth/zookeeper-3157/bad-run-log.txt");
        assertEquals(1170, zookeeper_3157.length);
        assertEquals("JUnit version 4.12", zookeeper_3157[0]);
    }

    @Test
    void testLogDirId() {
        assertEquals(0, TextParser.parseLogDirId("logs-0").get());
        assertEquals(1, TextParser.parseLogDirId("logs-1").get());
        assertEquals(2, TextParser.parseLogDirId("logs-2").get());
        assertEquals(3, TextParser.parseLogDirId("logs-3").get());
        assertTrue(TextParser.parseLogDirId("logs-0/").isEmpty());
        assertTrue(TextParser.parseLogDirId("/logs-1").isEmpty());
        assertTrue(TextParser.parseLogDirId("./logs-2").isEmpty());
    }

    @Test
    void testExceptionParser() {
        ExceptionParserTestUtil.runAllTests();
    }

    @Test
    void testTestResultParser() {
        TestResultParserUtil.runAllTests();
    }
}
