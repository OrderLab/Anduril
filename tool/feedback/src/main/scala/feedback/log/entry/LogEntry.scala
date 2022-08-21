package feedback.log.entry

import feedback.log.exception.NestedException
import feedback.time.Timing
import org.joda.time.DateTime

sealed trait LineEntry extends Timing with Serializable {
  val logLine: Int
  require(logLine > 0)
}

sealed trait LogEntry extends LineEntry {
  val logType: LogType
  require(logType != null)

  val thread: String
  require(thread != null)  // TODO: nonEmpty

  val classname: String
  require(classname != null && classname.nonEmpty && !classname.contains('.'))

  val fileLogLine: Int
  require(fileLogLine > 0)

  val msg: String
  require(msg != null)  // TODO: nonEmpty

  override def toString: String = msg // TODO: improve
}

final case class NormalLogEntry(override val logLine: Int,
                                override val showtime: DateTime,
                                override val logType: LogType,
                                override val thread: String,
                                override val classname: String,
                                override val fileLogLine: Int,
                                override val msg: String)
  extends LogEntry

final case class ExceptionLogEntry(override val logLine: Int,
                                   override val showtime: DateTime,
                                   override val logType: LogType,
                                   override val thread: String,
                                   override val classname: String,
                                   override val fileLogLine: Int,
                                   override val msg: String,
                                   nestedException: NestedException)
  extends LogEntry {
  require(nestedException != null)
}

final case class InjectionRecord(override val logLine: Int,
                                 override val showtime: DateTime,
                                 injection: Int) extends LineEntry {
  require(injection >= 0)  // TODO: >0?
}
