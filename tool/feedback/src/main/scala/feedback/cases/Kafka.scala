package feedback.cases

import scala.annotation.unused

@unused
final class Kafka_12508 extends UnitTestWorkload {
  override def checkClassName(testClass: String): Boolean =
    testClass.equals("org.apache.kafka.streams.integration.EmitOnChangeIntegrationTest")

  override def checkMethodName(testMethod: String): Boolean =
    testMethod.equals("shouldEmitSameRecordAfterFailover")

  override def checkExceptionName(exception: String): Boolean =
    exception.equals("org.opentest4j.AssertionFailedError")

  override def checkExceptionMsg(msg: Option[String]): Boolean =
    msg exists { _ startsWith "Condition not met within timeout " }

  // TODO: this is a timeout event, double check the stack trace?
  override val targetStackTracePrefix: List[String] = List(
    "org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:55)",
    "org.junit.jupiter.api.AssertTrue.assertTrue(AssertTrue.java:40)",
    "org.junit.jupiter.api.Assertions.assertTrue(Assertions.java:193)",
    "org.apache.kafka.test.TestUtils.lambda$waitForCondition$3(TestUtils.java:303)",
    "org.apache.kafka.test.TestUtils.retryOnExceptionWithTimeout(TestUtils.java:351)",
  )
}

@unused
final class Kafka_9374 extends UnitTestWorkload {
  override def checkClassName(testClass: String): Boolean =
    testClass.equals("org.apache.kafka.connect.integration.BlockingConnectorTest")

  override def checkMethodName(testMethod: String): Boolean =
    testMethod.equals("testBlockInConnectorConfig")

  override def checkExceptionName(exception: String): Boolean =
    exception.equals("java.lang.AssertionError")

  override def checkExceptionMsg(msg: Option[String]): Boolean =
    msg exists { _ equals "Reproduce the fault\n" }

  override val targetStackTracePrefix: List[String] = List(
    "org.junit.Assert.fail(Assert.java:89)",
    "org.apache.kafka.connect.integration.BlockingConnectorTest.testBlockInConnectorConfig(BlockingConnectorTest.java:123)",
  )
}
