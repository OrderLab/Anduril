package parser

import scala.collection.mutable

object Parser {
  private val year = """\d{4}"""
  private val month = """\d{2}"""
  private val day = """\d{2}"""
  private val hour = """\d{2}"""
  private val minute = """\d{2}"""
  private val second = """\d{2}"""
  private val millisecond = """\d{3}"""
  private val datetimeRegex = s"($year\\-$month\\-$day $hour\\:$minute\\:$second\\,$millisecond)"

  private val datetimeFormatter: org.joda.time.format.DateTimeFormatter =
    org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS")

  def parseDatetime(datetimeText: String): org.joda.time.DateTime =
    org.joda.time.DateTime.parse(datetimeText, datetimeFormatter)

  private val typeRegex = """(INFO |WARN |ERROR|DEBUG|TRACE)"""
  private val TypePattern4 = """(INFO|WARN) """.r
  private val TypePattern5 = """(ERROR|DEBUG|TRACE)""".r

  def parseLogType(logTypeText: String): String = logTypeText match {
    case TypePattern4(logType) => logType
    case TypePattern5(logType) => logType
  }

  private val thread = """.*"""
  private val file = """[^\:\@]+"""
  private val lineNumber = """\d+"""
  private val locationRegex = s"(\\[$thread\\:$file\\@$lineNumber\\])"
  private val LocationPattern = s"(?s)\\[($thread):($file)@($lineNumber)\\]".r   // Must enable dotall mode

  // Must explicit declare java.lang.Integer
  def parseLocation(locationText: String): (String, String, java.lang.Integer) = locationText match {
    case LocationPattern(t, f, n) => (t, f, n.toInt)
  }

  private val msgRegex = """(.*)"""

  // Must enable dotall mode
  private val NormalPattern = s"(?s)$datetimeRegex - $typeRegex $locationRegex - $msgRegex".r
  private val SpecialPattern = s"(?s)[^\n\r]$datetimeRegex - $typeRegex $locationRegex - $msgRegex".r
  private val NormalIdPattern = s"(?s)$datetimeRegex \\[myid\\:(.*)\\] - $typeRegex $locationRegex - $msgRegex".r
  private val SpecialIdPattern = s"(?s)[^\n\r]$datetimeRegex \\[myid\\:(.*)\\] - $typeRegex $locationRegex - $msgRegex".r

  private def parseLogEntryOptional(text: String): Option[LogEntryBuilder] = text match {
    case NormalPattern(datetime, logType, location, msg) => Some(new LogEntryBuilder(datetime, logType, location, msg))
    case SpecialPattern(datetime, logType, location, msg) => Some(new LogEntryBuilder(datetime, logType, location, msg))
    case NormalIdPattern(datetime, _, logType, location, msg) => Some(new LogEntryBuilder(datetime, logType, location, msg))
    case SpecialIdPattern(datetime, _, logType, location, msg) => Some(new LogEntryBuilder(datetime, logType, location, msg))
    case _ => None
  }

  def parseLogEntry(text: String): LogEntryBuilder = parseLogEntryOptional(text) match {
    case Some(logEntry) => logEntry
    case None => throw new RuntimeException(s"mismatch error: $text")
  }

  // TODO: extract the test symptom, e.g., FAILURE
  // TODO: handle the first log entry which can follow the header at the same line
  def parse(text: Array[String]): Log = {
    val logEntries = mutable.ArrayBuffer.empty[LogEntry]
    var headerBuilder: Option[mutable.StringBuilder] = None
    var currentLogEntryBuilder: Option[LogEntryBuilder] = None
    Array.tabulate(text.length) { index =>
      val line = text(index)
      parseLogEntryOptional(line) match {
        // finish the previous appender
        case Some(logEntryBuilder) =>
          logEntryBuilder.setLogLine(index + 1)  // the line number is array index + 1
          currentLogEntryBuilder.map(logEntries += _.build())  // store the previous log entry if any
          currentLogEntryBuilder = Some(logEntryBuilder)
        // append to the current appender
        case None =>
          currentLogEntryBuilder match {
            // the current log entry is not finished
            case Some(logEntryBuilder) =>
              logEntryBuilder.appendNewLine(line)
            // still in the header phase
            case None =>
              headerBuilder match {
                // at the very beginning of the log file
                case None =>
                  headerBuilder = Some(new mutable.StringBuilder(line))
                case Some(builder) =>
                  builder.append("\n").append(line)
              }
          }
      }
    }
    // finish the current log entry if any
    currentLogEntryBuilder.map(logEntries += _.build())
    val header = headerBuilder match {
      // no header
      case None => null
      case Some(builder) => builder.toString
    }
    new Log(header, logEntries.toArray)
  }
}
