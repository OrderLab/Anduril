package feedback.cases

import feedback.log.entry.LogType

import scala.annotation.unused

@unused
final class HDFS_12070 extends UnitTestWorkload {
  override def checkClassName(testClass: String): Boolean =
    testClass.equals("org.apache.hadoop.hdfs.TestLeaseRecovery")

  override def checkMethodName(testMethod: String): Boolean =
    testMethod.equals("testBlockRecoveryRetryAfterFailedRecovery")

  override def checkExceptionName(exception: String): Boolean =
    exception.equals("java.lang.AssertionError")

  override def checkExceptionMsg(msg: Option[String]): Boolean =
    msg exists { _ equals "File should be closed\n" }

  override val targetStackTracePrefix: List[String] = List(
    "org.junit.Assert.fail(Assert.java:88)",
    "org.apache.hadoop.hdfs.TestLeaseRecovery.testBlockRecoveryRetryAfterFailedRecovery(TestLeaseRecovery.java:272)",
  )
}

@unused
final class HDFS_12248 extends UnitTestWorkload {
  override def checkClassName(testClass: String): Boolean =
    testClass.equals("org.apache.hadoop.hdfs.TestRollingUpgrade")

  override def checkMethodName(testMethod: String): Boolean =
    testMethod.equals("testRollBackImage")

  override def checkExceptionName(exception: String): Boolean =
    exception.equals("java.lang.AssertionError")

  override def checkExceptionMsg(msg: Option[String]): Boolean =
    msg exists { _ equals "Query return false\n" }

  override val targetStackTracePrefix: List[String] = List(
    "org.junit.Assert.fail(Assert.java:88)",
    "org.apache.hadoop.hdfs.TestRollingUpgrade.queryForPreparation(TestRollingUpgrade.java:155)",
    "org.apache.hadoop.hdfs.TestRollingUpgrade.testRollBackImage(TestRollingUpgrade.java:115)",
  )
}

@unused
final class HDFS_4233 extends DistributedWorkload {
  override val targetNode: Int = 1

  override def checkLogClassName(classname: String): Boolean =
    classname equals "Server$Handler"

  override def checkFileLogLine(fileLogLine: Int): Boolean =
    fileLogLine == 1538

  override def checkLogType(logType: LogType): Boolean =
    logType == LogType.INFO

  override val targetStackTracePrefix: List[String] = List(
    "org.apache.hadoop.hdfs.server.namenode.FSEditLog.startLogSegment(FSEditLog.java:835)",
    "org.apache.hadoop.hdfs.server.namenode.FSEditLog.rollEditLog(FSEditLog.java:797)",
    "org.apache.hadoop.hdfs.server.namenode.FSImage.rollEditLog(FSImage.java:911)",
    "org.apache.hadoop.hdfs.server.namenode.FSNamesystem.rollEditLog(FSNamesystem.java:3503)",
    "org.apache.hadoop.hdfs.server.namenode.NameNodeRpcServer.rollEditLog(NameNodeRpcServer.java:645)",
  )
}

@unused
final class HDFS_13039 extends DistributedWorkload {
  override val targetNode: Int = 4

  override def checkLogClassName(classname: String): Boolean =
    classname equals "StripedBlockReader"

  override def checkFileLogLine(fileLogLine: Int): Boolean =
    fileLogLine == 137

  override def checkLogType(logType: LogType): Boolean =
    logType == LogType.INFO

  override val targetStackTracePrefix: List[String] = List(
    "at org.apache.hadoop.hdfs.server.datanode.erasurecode.StripedBlockReader.<init>(StripedBlockReader.java:83)",
    "at org.apache.hadoop.hdfs.server.datanode.erasurecode.StripedReader.createReader(StripedReader.java:169)",
    "at org.apache.hadoop.hdfs.server.datanode.erasurecode.StripedReader.initReaders(StripedReader.java:150)",
    "at org.apache.hadoop.hdfs.server.datanode.erasurecode.StripedReader.init(StripedReader.java:133)",
    "at org.apache.hadoop.hdfs.server.datanode.erasurecode.StripedBlockReconstructor.run(StripedBlockReconstructor.java:56)",
    "at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)",
    "at java.util.concurrent.FutureTask.run(FutureTask.java:266)",
    "at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)",
    "at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)",
    "at java.lang.Thread.run(Thread.java:748)",
  )
}
