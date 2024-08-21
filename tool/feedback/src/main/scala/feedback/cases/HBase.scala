package feedback.cases

import scala.annotation.unused

@unused
final class HBase_18137 extends UnitTestWorkload {
  override def checkClassName(testClass: String): Boolean =
    testClass.equals("org.apache.hadoop.hbase.replication.TestReplicationSmallTests")

  override def checkMethodName(testMethod: String): Boolean =
    testMethod.equals("testEmptyWALRecovery")

  override def checkExceptionName(exception: String): Boolean =
    exception.equals("java.lang.AssertionError")

  override def checkExceptionMsg(msg: Option[String]): Boolean =
    msg exists { _ startsWith "Waiting timed out after" }

  override val targetStackTracePrefix: List[String] = List(
    "org.junit.Assert.fail(Assert.java:88)",
    "at org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:209)",
    "at org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:143)",
  )
}

@unused
final class HBase_19608 extends UnitTestWorkload {
  override val ok_is_good: Boolean = false
}

@unused
final class HBase_19876 extends UnitTestWorkload {
  override def checkClassName(testClass: String): Boolean =
    testClass.equals("org.apache.hadoop.hbase.client.TestMalformedCellFromClient")

  override def checkMethodName(testMethod: String): Boolean =
    testMethod.equals("testNonAtomicOperations")

  override def checkExceptionName(exception: String): Boolean =
    exception.equals("java.lang.AssertionError")

  override def checkExceptionMsg(msg: Option[String]): Boolean =
    msg exists { _ equals "They should be equal\n" }

  override val targetStackTracePrefix: List[String] = List(
    "org.junit.Assert.fail(Assert.java:88)",
    "org.apache.hadoop.hbase.client.TestMalformedCellFromClient.testNonAtomicOperations(TestMalformedCellFromClient.java:205)",
  )
}
