package feedback.log.entry

import feedback.parser.{ExceptionParser, LogFileParser}
import org.joda.time.DateTime

import scala.collection.mutable

final class LogEntryBuilder(val logLine: Int,
                            val showtime: DateTime,
                            val logType: LogType,
                            val thread: String,
                            val classname: String,
                            val fileLogLine: Int,
                            private[this] var msg: mutable.StringBuilder) extends Serializable {

  def appendNewLine(text: String): Unit = this.msg.append('\n').append(text)

  def build: LogEntry = {
    require(logLine > 0)
    val text = msg.toString
    require(!text.equals("\n"))
    text match {
      case LogEntryBuilders.PaddingPattern(partialText, _) =>
        ExceptionParser.parseNormalNestedException(partialText) match {
          case (Some(msg), None) =>
            require(msg equals partialText)
            NormalLogEntry(logLine, showtime, logType, thread, classname, fileLogLine, text)
        }
      case LogEntryBuilders.EmptyPattern() =>
        NormalLogEntry(logLine, showtime, logType, thread, classname, fileLogLine, text)
      case _ =>
        ExceptionParser.parseNormalNestedException(text) match {
          case (Some(msg), Some(nestedException)) =>
            ExceptionLogEntry(logLine, showtime, logType, thread, classname, fileLogLine, msg, nestedException)
          case (Some(msg), None) =>
            require(msg equals text)
            NormalLogEntry(logLine, showtime, logType, thread, classname, fileLogLine, text)
        }
    }
  }

//  else LogEntryBuilder.build(showtime, logType, thread, classname, fileLogLine, msg.toString, logLine)

  // WARN: test only
//  def buildLogLine: LogEntry = {
//    LogEntryBuilders.create(showtime, logType, thread, classname, fileLogLine, msg.toString, -1)
//  }

  def getMsg: String = msg.toString

  def resetMsg(msg: String): Unit = this.msg = new mutable.StringBuilder(msg)
}

object LogEntryBuilders {
  private[entry] val PaddingPattern = raw"(?s)(.*[^\n])(\n+)".r
  private[entry] val EmptyPattern = raw"(?s)".r

  def create(logLine: Int,
             datetimeText: String,
             logTypeText: String,
             locationText: String,
             msg: String): LogEntryBuilder =
    LogFileParser.parseLocation(locationText) match {
      case (thread, file, fileLine) =>
        new LogEntryBuilder(
          logLine,
          LogFileParser.parseDatetime(datetimeText),
          LogFileParser.parseLogType(logTypeText),
          thread,
          file,
          fileLine,
          new mutable.StringBuilder(msg))
    }
}
