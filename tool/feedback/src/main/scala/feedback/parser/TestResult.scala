package feedback.parser

import feedback.time.Timing
import org.joda.time.DateTime

private[feedback] abstract class TestResult(time: DateTime) extends Timing(time) {
  val duration: Int
}

private[feedback] case class TestOK(time: DateTime, duration: Int) extends TestResult(time)

// Assume there is only one failure
private[feedback] case class TestFail(time: DateTime,
                                      duration: Int,
                                      testMethod: String,
                                      testClass: String,
                                      exceptions: Array[NestedException]) extends TestResult(time)
