package feedback.parser

import feedback.log.entry.{InjectionRecord, LogEntry, LogEntryBuilder, LogEntryBuilders, LogType}
import feedback.log.{LogFile, NormalLogFile, TestResult, TraceLogFile}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.slf4j.LoggerFactory

import java.io.File
import java.nio.file.Path
import scala.collection.mutable

object LogFileParser {
  private val LOG = LoggerFactory.getLogger(getClass)

  private val year = raw"\d{4}"
  private val month = raw"\d{2}"
  private val day = raw"\d{2}"
  private val hour = raw"\d{2}"
  private val minute = raw"\d{2}"
  private val second = raw"\d{2}"
  private val millisecond = raw"\d{3}"
  private[parser] val datetimeRegex = raw"($year-$month-$day $hour:$minute:$second,$millisecond)"

  val datetimeFormatter: DateTimeFormatter =
    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS")

  def parseDatetime(datetimeText: String): DateTime =
    DateTime.parse(datetimeText, datetimeFormatter)

  private val typeRegex = raw"(INFO |WARN |ERROR|DEBUG|TRACE)"

  def parseLogType(logTypeText: String): LogType =
    logTypeText match {
      case "INFO " => LogType.INFO
      case "WARN " => LogType.WARN
      case "ERROR" => LogType.ERROR
      case "DEBUG" => LogType.DEBUG
      case "TRACE" => LogType.TRACE
    }

  private val thread = raw".*"
  private val file = raw"[^\:\@]+"
  private val lineNumber = raw"\d+"
  private val locationRegex = raw"(\[$thread:$file@$lineNumber\])"
  private val LocationPattern = raw"(?s)\[($thread):($file)@($lineNumber)\]".r // Must enable DOTALL mode

  // Must explicitly declare java.lang.Integer
  def parseLocation(locationText: String): (String, String, Integer) =
    locationText match {
      case LocationPattern(t, f, n) => (t, f, n.toInt)
    }

  private val msgRegex = raw"(.*)"

  private val normalLogRegex = raw"$datetimeRegex - $typeRegex $locationRegex - $msgRegex"
  private val specialLogRegex = raw"[E\.]$datetimeRegex - $typeRegex $locationRegex - $msgRegex"
  private val normalIdLogRegex = raw"$datetimeRegex \[myid:(.*)\] - $typeRegex $locationRegex - $msgRegex"
  private val specialIdLogRegex = raw"[E\.]$datetimeRegex \[myid:(.*)\] - $typeRegex $locationRegex - $msgRegex"

  // Must enable DOTALL mode

  private val NormalPattern = raw"(?s)$normalLogRegex".r
  private val SpecialPattern = raw"(?s)$specialLogRegex".r
  private val NormalIdPattern = raw"(?s)$normalIdLogRegex".r
  private val SpecialIdPattern = raw"(?s)$specialIdLogRegex".r

  private val AttachedNormalPattern = raw"(?s)(.+.)$normalLogRegex".r
  private val AttachedNormalIdPattern = raw"(?s)(.+.)$normalIdLogRegex".r

  def parseLogEntry(previousDataTime: Option[DateTime], text: String, logLine: Int): Option[(Option[String], LogEntryBuilder)] = {
    def correct(datetimeText: String): DateTime = {
      val datetime = LogFileParser.parseDatetime(datetimeText)
      previousDataTime map { previousDataTime =>
        if (datetime isBefore(previousDataTime)) {
          LOG.warn(s"Log time ${datetime.toString(datetimeFormatter)} is replaced with ${previousDataTime.toString(datetimeFormatter)}")
          previousDataTime
        } else datetime
      } getOrElse datetime
    }
    text match {
      case NormalPattern(datetime, logType, location, msg) =>
        Some(None, LogEntryBuilders.create(logLine, correct(datetime), logType, location, msg))
      case SpecialPattern(datetime, logType, location, msg) =>
        Some(None, LogEntryBuilders.create(logLine, correct(datetime), logType, location, msg))
      case NormalIdPattern(datetime, _, logType, location, msg) =>
        Some(None, LogEntryBuilders.create(logLine, correct(datetime), logType, location, msg))
      case SpecialIdPattern(datetime, _, logType, location, msg) =>
        Some(None, LogEntryBuilders.create(logLine, correct(datetime), logType, location, msg))
      case AttachedNormalPattern(header, datetime, logType, location, msg) =>
        Some(Some(header), LogEntryBuilders.create(logLine, correct(datetime), logType, location, msg))
      case AttachedNormalIdPattern(header, datetime, _, logType, location, msg) =>
        Some(Some(header), LogEntryBuilders.create(logLine, correct(datetime), logType, location, msg))
      case _ => None
    }
  }

  def parseLogEntry(text: String, logLine: Int): Option[(Option[String], LogEntryBuilder)] = parseLogEntry(None, text, logLine)

  def parse(text: Array[String]): (LogFile, Option[TestResult]) = parse(text.iterator)

  def parse(text: Iterator[String]): (LogFile, Option[TestResult]) = {
    val entries = mutable.ArrayBuffer.empty[LogEntry]
    var headerBuilder: Option[mutable.StringBuilder] = None
    var testResultBuilder: Option[TestResultParser.TestResultBuilder] = None

    def updateLogEntries(logEntryBuilder: LogEntryBuilder): Unit = {
      TestResultParser.parseTestResult(logEntryBuilder.getMsg) match {
        case Some((msg, builder)) =>
          require(testResultBuilder.isEmpty)
          testResultBuilder = Some(builder)
          logEntryBuilder.resetMsg(msg)
        case None =>
      }
      entries += logEntryBuilder.build
    }

    var headerPhase = true

    def updateHeader(text: String): Unit = {
      require(headerPhase)
      headerBuilder match {
        // at the very beginning of the log file
        case None =>
          headerBuilder = Some(new mutable.StringBuilder(text))
        case Some(builder) =>
          builder append text
      }
    }

    var currentLogEntryBuilder: Option[LogEntryBuilder] = None

    var index = -1
    while (text.hasNext) {
      index += 1
      val line = text.next()
      parseLogEntry(currentLogEntryBuilder map { _.showtime }, line, index + 1) match {
        // finish the previous appender
        case Some((headerOptional, logEntryBuilder)) =>
          if (headerPhase) {
            headerOptional foreach updateHeader
          } else {
            headerOptional foreach currentLogEntryBuilder.get.appendNewLine
          }
          headerPhase = false
          // store the previous log entry if any
          currentLogEntryBuilder foreach updateLogEntries
          currentLogEntryBuilder = Some(logEntryBuilder)
        // append to the current appender
        case None =>
          currentLogEntryBuilder match {
            // the current log entry is not finished
            case Some(logEntryBuilder) => logEntryBuilder.appendNewLine(line)
            // still in the header phase
            case None => updateHeader(s"$line\n")
          }
      }
    }
    // finish the current log entry if any
    currentLogEntryBuilder foreach updateLogEntries

    val logEntries = mutable.ArrayBuffer.empty[LogEntry]
    val injections = mutable.ArrayBuffer.empty[InjectionRecord]

    val partitioner: Array[LogEntry => Option[_]] = Array(
      {TextParser.recognizeInjection _} andThen { _ map { injections += _ } },
      TextParser.recognizeFeedbackSet,
      Some(_) map { logEntries += _ }
    )

    entries foreach { entry => partitioner find { _.apply(entry).nonEmpty } }

    val header = headerBuilder map { _.toString }

    val logFile =
      if (injections.isEmpty) NormalLogFile(header, logEntries.toArray)
      else TraceLogFile(header, logEntries.toArray, injections.toArray)

    (logFile, testResultBuilder map {result =>
      result.build(logFile.entries(0).showtime.plus(result.duration))})
  }

  def parseLogFile(file: File): (LogFile, Option[TestResult]) = {
    parse(ParserUtil.getFileLinesAsync(file))
  }

  def parseLogFile(path: Path): (LogFile, Option[TestResult]) = {
    parse(ParserUtil.getFileLinesAsync(path))
  }
}
