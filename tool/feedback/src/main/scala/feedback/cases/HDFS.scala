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
    classname equals "DataXceiver"

  override def checkFileLogLine(fileLogLine: Int): Boolean =
    fileLogLine == 323

  override def checkLogType(logType: LogType): Boolean =
    logType == LogType.ERROR

  override val targetStackTracePrefix: List[String] = List(
    "org.apache.hadoop.io.IOUtils.readFully(IOUtils.java:212)",
    "org.apache.hadoop.hdfs.protocol.datatransfer.PacketReceiver.doReadFully(PacketReceiver.java:211)",
    "org.apache.hadoop.hdfs.protocol.datatransfer.PacketReceiver.doRead(PacketReceiver.java:134)",
    "org.apache.hadoop.hdfs.protocol.datatransfer.PacketReceiver.receiveNextPacket(PacketReceiver.java:109)",
    "org.apache.hadoop.hdfs.server.datanode.BlockReceiver.receivePacket(BlockReceiver.java:528)",
    "org.apache.hadoop.hdfs.server.datanode.BlockReceiver.receiveBlock(BlockReceiver.java:971)",
    "org.apache.hadoop.hdfs.server.datanode.DataXceiver.writeBlock(DataXceiver.java:902)",
    "org.apache.hadoop.hdfs.protocol.datatransfer.Receiver.opWriteBlock(Receiver.java:173)",
    "org.apache.hadoop.hdfs.protocol.datatransfer.Receiver.processOp(Receiver.java:107)",
    "org.apache.hadoop.hdfs.server.datanode.DataXceiver.run(DataXceiver.java:290)",
    "java.lang.Thread.run(Thread.java:748)",
  )
}
