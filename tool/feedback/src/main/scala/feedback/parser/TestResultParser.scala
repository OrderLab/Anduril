package feedback.parser

import feedback.log.exception.{NestedException, PlainExceptionRecord}
import feedback.log.{TestFail, TestOK, TestResult}
import org.joda.time.DateTime

object TestResultParser {
  private val testDurationRegex = raw"\d+\.\d+"

  // millisecond
  def parseDuration(text: String): Int = (text.toDouble * 1_000).toInt

  // Must enable DOTALL mode
  private val OK_JUnit4 = raw"(?s)(.*)\n+Time: ($testDurationRegex)\n\nOK \(\d+ tests?\)\n".r
  private val FAIL_JUnit4 =
    raw"(?s)(.*)\n+Time: ($testDurationRegex)\nThere [a-z]+ (\d+) failures?:\n(.+)\n\nFAILURES!!!\nTests run: *(\d+), *Failures: *(\d+)\n".r

  private val preambleJUnit5 =
    raw"(.*)\n[E|\n]Thanks for using JUnit! Support its development at https://junit.org/sponsoring\n[E|\n]"

  private val suffixJUnit5 =
    raw"\n[E|\n]Tests? run finished after (\d+) ms\n\[.*\[ *(\d+) tests successful *\]\n\[ *(\d+) tests failed *\]\n"

  private val JUnit5Pattern = raw"(?s)$preambleJUnit5(.*)$suffixJUnit5".r

  private[parser] sealed trait TestResultBuilder {
    private[parser] val duration: Int
    private[parser] def build(datetime: DateTime): TestResult
  }

  private[parser] final case class OK(override private[parser] val duration: Int) extends TestResultBuilder {
    private[parser] def build(datetime: DateTime) = TestOK(datetime, duration)
  }

  private[parser] final case class FAIL(override private[parser] val duration: Int,
                                        private[parser] val testMethod: String,
                                        private[parser] val testClass: String,
                                        private[parser] val nestedException: NestedException) extends TestResultBuilder {
    private[parser] def build(datetime: DateTime) = TestFail(datetime, duration, testMethod, testClass, nestedException)
  }

  def parseTestResult(text: String): Option[(String, TestResultBuilder)] = {
    text match {
      case OK_JUnit4(msg, duration) =>
        Some(msg, OK(parseDuration(duration)))
      case FAIL_JUnit4(msg, duration, _, failures, _, _) =>
        ExceptionParser.parseJunit4TestFailures(failures) match {
          case (testMethod, testClass, nestedException) =>
            Some(msg, FAIL(parseDuration(duration), testMethod, testClass, nestedException))
        }
      case JUnit5Pattern(msg, failures, duration, successful, failed) =>
        // TODO: extract the failure content
        (successful.toInt, failed.toInt) match {
          case (1, 0) => Some(msg, OK(duration.toInt))
          case (0, 1) => Some(msg, ExceptionParser.parseJUnit5Failure(failures, duration.toInt))
        }
      case _ => None
    }
  }
}
