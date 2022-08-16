package feedback.parser

import org.joda.time.DateTime

import scala.collection.mutable

private[parser] final class LogEntryBuilder(val datetime: DateTime,
                                            val logType: LogType,
                                            val thread: String,
                                            val file: String,
                                            val fileLine: Int,
                                            private var logLine: Int,
                                            private var msg: mutable.StringBuilder) extends java.io.Serializable {

  private def this(datetime: DateTime, logType: LogType, thread: String, file: String, fileLine: Int, msg: String) =
    this(datetime, logType, thread, file, fileLine, -1, new mutable.StringBuilder(msg))

  private def this(datetime: String, logType: String, thread: String, file: String, fileLine: Int, msg: String) =
    this(Parser.parseDatetime(datetime), Parser.parseLogType(logType), thread, file, fileLine, msg)

  def setLogLine(logLine: Int): Unit = this.logLine = logLine
  def getLogLine: Int = this.logLine

  def appendNewLine(text: String): Unit = this.msg.append('\n').append(text)

  def build: LogEntry =
    if (logLine == -1) throw new RuntimeException("Bad log line")
    else LogEntryBuilder.build(datetime, logType, thread, file, fileLine, msg.toString, logLine)

  def buildWithoutLogLine: LogEntry = LogEntryBuilder.build(datetime, logType, thread, file, fileLine, msg.toString, -1)

  def getMsg: String = this.msg.toString
  def resetMsg(msg: String): mutable.StringBuilder = {
    this.msg = new mutable.StringBuilder(msg)
    this.msg
  }
}

private[parser] object LogEntryBuilder {
  def create(datetimeText: String, logTypeText: String, locationText: String, msg: String): LogEntryBuilder =
    Parser.parseLocation(locationText) match {
      case (thread, file, fileLine) =>
        new LogEntryBuilder(datetimeText, logTypeText, thread, file, fileLine, msg)
    }

  private def build(datetime: DateTime, logType: LogType, thread: String, file: String, fileLine: Int, text: String, logLine: Int): LogEntry =
    Parser.parseLogException(text) match {
      case (msg, exceptions) => new LogEntry(datetime, logType, thread, file, fileLine, msg, logLine, exceptions)
    }
}
