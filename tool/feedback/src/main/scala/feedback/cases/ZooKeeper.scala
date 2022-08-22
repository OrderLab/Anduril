package feedback.cases

import feedback.log.entry.{ExceptionLogEntry, LogType}
import feedback.log.exception.PlainExceptionRecord
import feedback.log.{Log, LogFile, TestResult}
import feedback.symptom.{SymptomEvent, UnitTestLogEvent}

import scala.annotation.unused

@unused
final class ZooKeeper_2247 extends UnitTestWorkload {
  override def checkClassName(testClass: String): Boolean =
    testClass equals "org.apache.zookeeper.server.quorum.QuorumPeerMainTest"

  override def checkMethodName(testMethod: String): Boolean =
    testMethod equals "testQuorum"

  override def checkExceptionName(exception: String): Boolean =
    exception equals "org.apache.zookeeper.KeeperException$ConnectionLossException"

  override def checkExceptionMsg(msg: Option[String]): Boolean =
    msg exists { _ startsWith "KeeperErrorCode = ConnectionLoss" }

  // TODO: double-check this
  override val targetStackTracePrefix: List[String] = List(
    "org.apache.zookeeper.KeeperException.create(KeeperException.java:99)",
    "org.apache.zookeeper.KeeperException.create(KeeperException.java:51)",
  )

  override def findSymptom(log: LogFile, result: TestResult): Option[List[SymptomEvent]] =
    log.entries.find {
      case ExceptionLogEntry(_, _, logType, thread, classname, fileLogLine, msg, nestedException) =>
        logType == LogType.ERROR &&
          thread.startsWith("SyncThread") &&
          classname.equals("ZooKeeperCriticalThread") &&
          fileLogLine == 48 &&
          msg.startsWith("Severe unrecoverable error") &&
        nestedException.exceptions(0).stacktrace.stack.stack(0).literal.equals(
          "org.apache.zookeeper.server.SyncRequestProcessor.flush(SyncRequestProcessor.java:178)")
      case _ => false
    } map { logEntry => List(UnitTestLogEvent(logEntry)) }
}

@unused
final class ZooKeeper_3006 extends UnitTestWorkload {
  override def checkClassName(testClass: String): Boolean =
    testClass equals "org.apache.zookeeper.test.ZkDatabaseCorruptionTest"

  override def checkMethodName(testMethod: String): Boolean =
    testMethod equals "testAbsentRecentSnapshot"

  override def checkExceptionName(exception: String): Boolean =
    exception equals "java.lang.NullPointerException"

  override def checkExceptionMsg(msg: Option[String]): Boolean =
    msg.isEmpty

  override val targetStackTracePrefix: List[String] = List(
    "org.apache.zookeeper.server.ZKDatabase.calculateTxnLogSizeLimit(ZKDatabase.java:359)",
    "org.apache.zookeeper.test.ZkDatabaseCorruptionTest.testAbsentRecentSnapshot(ZkDatabaseCorruptionTest.java:94)",
    "sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)",
  )
}

@unused
final class ZooKeeper_4203 extends UnitTestWorkload {
  override def checkClassName(testClass: String): Boolean =
    testClass equals "org.apache.zookeeper.server.quorum.LeaderLeadingStateTest"

  override def checkMethodName(testMethod: String): Boolean =
    testMethod equals "leadingStateTest"

  override def checkExceptionName(exception: String): Boolean =
    exception equals "java.lang.IllegalStateException"

  override def checkExceptionMsg(msg: Option[String]): Boolean =
    msg exists { _ equals "State error\n" }

  override val targetStackTracePrefix: List[String] = List(
    "org.apache.zookeeper.server.quorum.LeaderLeadingStateTest.leadingStateTest(LeaderLeadingStateTest.java:87)",
  )
}

@unused
final class ZooKeeper_3157 extends UnitTestWorkload {
  override def checkClassName(testClass: String): Boolean =
    testClass equals "org.apache.zookeeper.server.quorum.FuzzySnapshotRelatedTest"

  override def checkMethodName(testMethod: String): Boolean =
    testMethod equals "testPZxidUpdatedWhenLoadingSnapshot"

  override def checkExceptionName(exception: String): Boolean =
    exception equals "org.apache.zookeeper.KeeperException$ConnectionLossException"

  override def checkExceptionMsg(msg: Option[String]): Boolean =
    msg exists { _ startsWith "KeeperErrorCode = ConnectionLoss" }

  // TODO: this condition is too strict, relax it
  override val targetStackTracePrefix: List[String] = List(
    "org.apache.zookeeper.KeeperException.create(KeeperException.java:102)",
    "org.apache.zookeeper.KeeperException.create(KeeperException.java:54)",
    "org.apache.zookeeper.ZooKeeper.getData(ZooKeeper.java:2046)",
    "org.apache.zookeeper.server.quorum.FuzzySnapshotRelatedTest.compareStat(FuzzySnapshotRelatedTest.java:258)",
  )
}
