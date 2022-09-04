package feedback.log

import feedback.log.exception.NestedException
import feedback.symptom.SymptomEvent
import org.joda.time.DateTime

sealed trait TestResult {
  val duration: Int
}

final case class TestOK(override val showtime: DateTime, override val duration: Int) extends TestResult with SymptomEvent

// Assume there is only one failure
final case class TestFail(override val showtime: DateTime,
                          override val duration: Int,
                          testMethod: String,
                          testClass: String,
                          exceptions: NestedException) extends TestResult with SymptomEvent

final case class NoResult(override val duration: Int) extends TestResult
