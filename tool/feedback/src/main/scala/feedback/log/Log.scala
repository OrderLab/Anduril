package feedback.log

import feedback.log.entry.{InjectionRecord, LogEntry}
import feedback.time.Timing
import org.joda.time.DateTime

sealed trait LogFile extends Timing with Serializable {
  val header: Option[String]
  require(!header.contains(null) && !header.exists(_.isEmpty))

  val entries: Array[LogEntry]
  require(entries != null && entries.nonEmpty && !entries.contains(null))

  override final val showtime: DateTime = entries(0).showtime
}

final case class NormalLogFile(override val header: Option[String],
                               override val entries: Array[LogEntry]) extends LogFile

final case class TraceLogFile(override val header: Option[String],
                              override val entries: Array[LogEntry],
                              injections: Array[InjectionRecord]) extends LogFile {
  require(injections != null && injections.nonEmpty && !injections.contains(null))
}

sealed trait Log extends Timing with Serializable

final case class UnitTestLog(log: LogFile, result: TestResult) extends Log {
  require(log != null)
  require(result != null)
  override val showtime: DateTime = log.showtime
}

final case class DistributedWorkloadLog(logs: Array[LogFile]) extends Log {
  require(logs != null && logs.nonEmpty && !logs.contains(null))
  override val showtime: DateTime = logs.min.showtime
}
