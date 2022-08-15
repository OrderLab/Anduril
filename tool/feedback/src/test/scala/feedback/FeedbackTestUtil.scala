package feedback

import feedback.time.LogType
import org.joda.time.DateTime

private[feedback] object FeedbackTestUtil {
  private val CaseDirPattern = """([a-zA-Z]+)_(\d+)""".r

  def parseCaseDirName(text: String): String = text match {
    case CaseDirPattern(system, number) => s"${system.toLowerCase}-${number}"
  }

  private def getDateTimeLiteral(time: DateTime): String = time.toString(feedback.parser.Parser.datetimeFormatter)

  private val injectionLimit       = 1000000;
  private val injectionDigitLimit  = 7;
  private val occurrenceLimit      = 1000000;
  private val occurrenceDigitLimit = 7;

  private def injectionFormat(injection: Int, occurrence: Int): String = {
    require(0 <= injection)
    require(injection < injectionLimit)
    require(0 < occurrence)
    require(occurrence <= occurrenceLimit)
    s"injection=%-${injectionDigitLimit}d, occurrence=%-${occurrenceDigitLimit}d".format(injection, occurrence)
  }

  def injectionTimingFormat(datetime: DateTime, injection: Int, occurrence: Int): String =
    s"${getDateTimeLiteral(datetime)} ${injectionFormat(injection, occurrence)}"

  private val msgLimit = 10

  private def msgFormat(msg: String): String =
    s"%-${msgLimit}s".format(org.apache.commons.lang3.StringUtils.abbreviate(msg, msgLimit))

  def logEntryTimingFormat(datetime: DateTime, logType: LogType.Value, msg: String): String = {
    val literal = s"${getDateTimeLiteral(datetime)} ${msgFormat(msg)}"
    val padding = " " * literal.length
    logType match {
      case LogType.TRIAL => s"${literal}   ${padding}   ${padding}"
      case LogType.GOOD  => s"${padding}   ${literal}   ${padding}"
      case LogType.BAD   => s"${padding}   ${padding}   ${literal}"
    }
  }

  private val timeFormatter: org.joda.time.format.DateTimeFormatter =
    org.joda.time.format.DateTimeFormat.forPattern("HH:mm:ss,SSS")

  private def getTimeLiteral(time: DateTime): String = time.toString(timeFormatter)

  def getInjectionInterval(count: Int, begin: DateTime, end: DateTime): String = {
    require(0 < count)
    require(count <= occurrenceLimit)
    require(!begin.isAfter(end))
    s"${s"%${occurrenceDigitLimit}d".format(count)} injections from ${getTimeLiteral(begin)} to ${getTimeLiteral(end)}"
  }
}
