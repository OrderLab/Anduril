package feedback.parser

import feedback.JavaTestUtil.assertMismatch
import feedback.ScalaTestUtil.{assertEquals, assertTrue}
import feedback.parser.Parser.{parseSingleTestFailure, parseTestResultOptional}

private[parser] object TestResultParserUtil {
  private def parseTestResult(text: String): Unit = require(parseTestResultOptional(text).nonEmpty)

  // the only public entry, called by TestResultParserTest
  def runAllTests(): Unit = {
    testResult()
    testDuration()
    testSingleFailure()
  }

  private def testResult(): Unit = {
    parseTestResult("\nTime: 40.922\n\nOK (1 test)\n")
    parseTestResult("\nTime: 40.922\n\nOK (1 tests)\n")
    parseTestResult("\nTime: 46.636\n\nOK (2 tests)\n")
    assertMismatch(() => parseTestResult("\nTime: 40.922\n\nOK (1 tets)\n"))
    assertMismatch(() => parseTestResult("\nTime: 40.922\nOK (1 tests)\n"))
    parseTestResult("Minicluster is down\n\nTime: 46.636\nThere was 1 failure:\n1) testEmptyWALRecovery(org.apache.hadoop.hbase.replication.TestReplicationSmallTests)\njava.lang.AssertionError: Waiting timed out after [10,000] msec\n\tat org.junit.Assert.fail(Assert.java:88)\n\nFAILURES!!!\nTests run: 1,  Failures: 1\n")
  }

  private def testDuration(): Unit = {
    assertEquals(46636, Parser.parseDuration("46.636"))
  }

  private def testSingleFailure(): Unit = {
    parseSingleTestFailure("1) testEmptyWALRecovery(org.apache.hadoop.hbase.replication.TestReplicationSmallTests)\njava.lang.AssertionError: Waiting timed out after [10,000] msec\n\tat org.junit.Assert.fail(Assert.java:88)\n\tat org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:209)\n\tat org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:143)\n") match {
      case (testMethod, testClass, exceptions) =>
        assertEquals("testEmptyWALRecovery", testMethod)
        assertEquals("org.apache.hadoop.hbase.replication.TestReplicationSmallTests", testClass)
        assertEquals("java.lang.AssertionError", exceptions(0).exception)
        assertEquals("Waiting timed out after [10,000] msec", exceptions(0).exceptionMsg)
        assertTrue(Array("org.junit.Assert.fail(Assert.java:88)", "org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:209)", "org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:143)") sameElements exceptions(0).stacktrace)
        assertTrue(Array(new NestedException("java.lang.AssertionError", "Waiting timed out after [10,000] msec", Array("org.junit.Assert.fail(Assert.java:88)", "org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:209)", "org.apache.hadoop.hbase.Waiter.waitFor(Waiter.java:143)"))) sameElements exceptions)
    }
  }
}
