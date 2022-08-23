package feedback.parser

import feedback.ScalaTestUtil._
import feedback.log.exception._

private[parser] object TestResultParserUtil {
  private def parseTestResult(text: String): Unit = require(TestResultParser.parseTestResult(text).nonEmpty)

  // the only public entry, called by TestResultParserTest
  def runAllTests(): Unit = {
    testResult()
    testDuration()
    testSingleFailure()
    testJUnit5Result()
  }

  private def testResult(): Unit = {
    parseTestResult("\n\nTime: 40.922\n\nOK (1 test)\n")
    parseTestResult("\n\nTime: 40.922\n\nOK (1 tests)\n")
    parseTestResult("\n\nTime: 46.636\n\nOK (2 tests)\n")
    assertRequireFail(() => parseTestResult("\n\nTime: 40.922\n\nOK (1 tets)\n"))
    assertRequireFail(() => parseTestResult("\n\nTime: 40.922\nOK (1 tests)\n"))
    parseTestResult("Minicluster is down\n\nTime: 46.636\nThere was 1 failure:\n1) testEmptyWALRecovery(org.apache.hadoop.hbase.replication.TestReplicationSmallTests)\njava.lang.AssertionError: Waiting timed out after [10,000] msec\n\tat org.junit.Assert.fail(Assert.java:88)\n\nFAILURES!!!\nTests run: 1,  Failures: 1\n")
  }

  private def testDuration(): Unit = {
    assertEquals(46636, TestResultParser.parseDuration("46.636"))
  }

  private def testSingleFailure(): Unit = {
    ExceptionParser.parseJunit4TestFailures("1) testEmptyWALRecovery(org.apache.hadoop.hbase.replication.TestReplicationSmallTests)\njava.lang.AssertionError: Waiting timed out after [10,000] msec\nasdf\n\nas\n\tat org.junit.Assert.fail(Assert.java:88)\n\tat org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:209)\n\tat org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:143)") match {
      case (testMethod, testClass, nestedException) =>
        assertEquals("testEmptyWALRecovery", testMethod)
        assertEquals("org.apache.hadoop.hbase.replication.TestReplicationSmallTests", testClass)
        assertEquals("java.lang.AssertionError", nestedException.exceptions(0).exception)
        assertEquals("Waiting timed out after [10,000] msec\nasdf\n\nas\n", nestedException.exceptions(0) match {
          case MsgExceptionRecord(_, msg, _) => msg
        })
//        assertTrue(Array("org.junit.Assert.fail(Assert.java:88)", "org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:209)", "org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:143)") sameElements nestedException.exceptions(0).stacktrace.stack.stack)
//        assertTrue(Array(new NestedException("java.lang.AssertionError", "Waiting timed out after [10,000] msec", Array("org.junit.Assert.fail(Assert.java:88)", "org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:209)", "org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:143)"))) sameElements exceptions)
    }
  }

  private def testJUnit5Result(): Unit = {
    val text = "Failures (1):\n  JUnit Vintage:EmitOnChangeIntegrationTest:shouldEmitSameRecordAfterFailover\n    MethodSource [className = 'org.apache.kafka.streams.integration.EmitOnChangeIntegrationTest', methodName = 'shouldEmitSameRecordAfterFailover', methodParameterTypes = '']\n    => org.opentest4j.AssertionFailedError: Condition not met within timeout 60000. Did not receive all [KeyValue(1, A), KeyValue(1, B)] records from topic outputEmitOnChangeIntegrationTestshouldEmitSameRecordAfterFailover (got []) ==> expected: <true> but was: <false>\n       org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:55)\n       org.junit.jupiter.api.AssertTrue.assertTrue(AssertTrue.java:40)"
    ExceptionParser.parseJUnit5Failure(text, 0)
  }
}
