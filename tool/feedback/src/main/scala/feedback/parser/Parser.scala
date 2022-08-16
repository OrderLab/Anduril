package feedback.parser

import feedback.time.InjectionRequestRecord
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable

private[feedback] object Parser {
  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)

  private val year = raw"\d{4}"
  private val month = raw"\d{2}"
  private val day = raw"\d{2}"
  private val hour = raw"\d{2}"
  private val minute = raw"\d{2}"
  private val second = raw"\d{2}"
  private val millisecond = raw"\d{3}"
  val datetimeRegex = raw"($year-$month-$day $hour:$minute:$second,$millisecond)"

  val datetimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS")

  def parseDatetime(datetimeText: String): DateTime = DateTime.parse(datetimeText, datetimeFormatter)

  val typeRegex = raw"(INFO |WARN |ERROR|DEBUG|TRACE)"

  def parseLogType(logTypeText: String): LogType = logTypeText match {
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
  private val LocationPattern = raw"(?s)\[($thread):($file)@($lineNumber)\]".r   // Must enable DOTALL mode

  // Must explicitly declare java.lang.Integer
  def parseLocation(locationText: String): (String, String, java.lang.Integer) = locationText match {
    case LocationPattern(t, f, n) => (t, f, n.toInt)
  }

  private val msgRegex = raw"(.*)"

  // Must enable DOTALL mode
  private val normalLogRegex = raw"$datetimeRegex - $typeRegex $locationRegex - $msgRegex"
  private val specialLogRegex = raw"[E\.]$datetimeRegex - $typeRegex $locationRegex - $msgRegex"
  private val normalIdLogRegex = raw"$datetimeRegex \[myid:(.*)\] - $typeRegex $locationRegex - $msgRegex"
  private val specialIdLogRegex = raw"[E\.]$datetimeRegex \[myid:(.*)\] - $typeRegex $locationRegex - $msgRegex"

  private val NormalPattern = raw"(?s)$normalLogRegex".r
  private val SpecialPattern = raw"(?s)$specialLogRegex".r
  private val NormalIdPattern = raw"(?s)$normalIdLogRegex".r
  private val SpecialIdPattern = raw"(?s)$specialIdLogRegex".r

  private val AttachedNormalPattern = raw"(?s)(.+.)$normalLogRegex".r
  private val AttachedNormalIdPattern = raw"(?s)(.+.)$normalIdLogRegex".r

  private def parseLogEntryOptional(text: String): Option[(Option[String], LogEntryBuilder)] = text match {
    case NormalPattern(datetime, logType, location, msg) =>
      Some(None, LogEntryBuilder.create(datetime, logType, location, msg))
    case SpecialPattern(datetime, logType, location, msg) =>
      Some(None, LogEntryBuilder.create(datetime, logType, location, msg))
    case NormalIdPattern(datetime, _, logType, location, msg) =>
      Some(None, LogEntryBuilder.create(datetime, logType, location, msg))
    case SpecialIdPattern(datetime, _, logType, location, msg) =>
      Some(None, LogEntryBuilder.create(datetime, logType, location, msg))
    case AttachedNormalPattern(header, datetime, logType, location, msg) =>
      Some(Some(header), LogEntryBuilder.create(datetime, logType, location, msg))
    case AttachedNormalIdPattern(header, datetime, _, logType, location, msg) =>
      Some(Some(header), LogEntryBuilder.create(datetime, logType, location, msg))
    case _ => None
  }

  def parseLogEntry(text: String): LogEntryBuilder = parseLogEntryOptional(text) match {
    case Some((None, logEntryBuilder)) => logEntryBuilder
    case None => throw new RuntimeException(s"mismatch error: $text")
  }

  private val InjectionRequestRecordPattern = raw"flaky record injection (\d+)".r

  private def recognizeInjection(logEntryBuilder: LogEntryBuilder): Option[InjectionRequestRecord] =
    if (logEntryBuilder.file.equals("TraceAgent") || logEntryBuilder.file.equals("BaselineAgent")) {
      logEntryBuilder.getMsg match {
        case InjectionRequestRecordPattern(injection) =>
          Some(new InjectionRequestRecord(
            logEntryBuilder.datetime, logEntryBuilder.thread, injection.toInt, logEntryBuilder.getLogLine))
        case _ => None
      }
    } else None

  def parseLog(text: Array[String]): Log = {
    val logEntries = mutable.ArrayBuffer.empty[LogEntry]
    val injectionRecords = mutable.ArrayBuffer.empty[InjectionRequestRecord]
    var header: Option[mutable.StringBuilder] = None
    var testResult: Option[TestResultBuilder] = None

    def updateLogEntries(logEntryBuilder: LogEntryBuilder): Unit = {
      parseTestResultOptional(logEntryBuilder.getMsg) match {
        case Some((msg, testResultBuilder)) =>
          require(testResult.isEmpty)
          testResult = Some(testResultBuilder)
          logEntryBuilder.resetMsg(msg)
        case None =>
      }
      logEntries += logEntryBuilder.build
    }

    var headerPhase = true

    def updateHeader(s: String): Unit = {
      require(headerPhase)
      header match {
        // at the very beginning of the log file
        case None =>
          header = Some(new mutable.StringBuilder(s))
        case Some(builder) =>
          builder.append(s)
      }
    }

    var currentLogEntryBuilder: Option[LogEntryBuilder] = None

    Array.tabulate(text.length) { index =>
      val line = text(index)
      parseLogSetOptional(line) match {
        case Some(_) =>  // ignore the location feedback log from our tool
        case None =>
          parseLogEntryOptional(line) match {
            // finish the previous appender
            case Some((headerOptional, logEntryBuilder)) =>
              headerOptional.foreach(updateHeader)
              headerPhase = false
              logEntryBuilder.setLogLine(index + 1)  // the line number is array index + 1
              recognizeInjection(logEntryBuilder) match {
                case Some(injectionRecord) =>
                  injectionRecords += injectionRecord  // record an injection request
                case None =>
                  // store the previous log entry if any
                  currentLogEntryBuilder.foreach(updateLogEntries)
                  currentLogEntryBuilder = Some(logEntryBuilder)
              }
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
    }
    // finish the current log entry if any
    currentLogEntryBuilder.foreach(updateLogEntries)
    new Log(
      header.map(_.toString).orNull,
      logEntries.toArray,
      injectionRecords.toArray,
      testResult.map(builder => builder.build(logEntries(0).datetime.plus(builder.duration))).orNull)
  }

  private val SingleTestFailureWithMsgPattern =
    raw"(?s)\n*(\d+)\)[ \t]*([^\(\)\n]+)\(([^\n]+)\)\n([^\:\n]+): ([^\n]*)\n(.*)".r
  private val SingleTestFailurePattern =
    raw"(?s)\n*(\d+)\)[ \t]*([^\(\)\n]+)\(([^\n]+)\)\n([^\:\n]+)\n(.*)".r

  // TODO: remove the limitation of only accepting one line of log before parsing an exception
  private val classNameRegex = raw"[a-zA-Z_0-9\.$$]+"
  private val exceptionNameRegex = raw"(${classNameRegex}Exception|${classNameRegex}Error|${classNameRegex}Throwable)"
  private val ExceptionLogWithMsgPattern = raw"(?s)([^\n]+)\n$exceptionNameRegex: ([^\n]*)\n(.*)".r
  private val ExceptionLogPattern = raw"(?s)([^\n]+)\n$exceptionNameRegex\n(.*)".r

  private val StackTracePattern = raw"(?s)[ \t]+at +([^\(\)\n]+\([^\(\)\n]+\))".r

  private val StackTraceWithRemainderPattern = raw"(?s)([ \t]+at +[^\(\)\n]+\([^\(\)\n]+\))\n(.*)".r
  private val StackTraceWithoutRemainderPattern = raw"(?s)([ \t]+at +[^\(\)\n]+\([^\(\)\n]+\))".r
  private val MoreStackTraceWithRemainderPattern = raw"(?s)[ \t]+(\.\.\. \d+ more)\n(.*)".r
  private val MoreStackTraceWithoutRemainderPattern = raw"(?s)[ \t]+(\.\.\. \d+ more)".r

  private val NestedExceptionWithMsgPattern = raw"(?s)Caused by: ([^\:\n]+): ([^\n]*)\n(.*)".r
  private val NestedExceptionPattern = raw"(?s)Caused by: ([^\:\n]+)\n(.*)".r

  private val ExceptionPaddingPattern = raw"(?s)\n*".r

  private class NestedExceptionBuilder(exception: String, exceptionMsg: String) {
    private[parser] val stacktrace = mutable.ArrayBuffer.empty[String]
    private[parser] def build = new NestedException(exception, exceptionMsg, stacktrace.toArray)
  }

  private def parseExceptionOptional(exceptions: mutable.ArrayBuffer[NestedException],
                                     nestedTestFailureBuilder: NestedExceptionBuilder,
                                     remainder: String): Option[Array[NestedException]] = {

    def parseRemainder(remainder: String): Option[Array[NestedException]] = remainder match {
      case ExceptionPaddingPattern() =>
        Some(exceptions.toArray)
      case NestedExceptionWithMsgPattern(exception, exceptionMsg, padding) =>
        parseExceptionOptional(exceptions, new NestedExceptionBuilder(exception, exceptionMsg), padding)
      case NestedExceptionPattern(exception, padding) =>
        parseExceptionOptional(exceptions, new NestedExceptionBuilder(exception, ""), padding)
      case _ => None
    }

    remainder match {
      case StackTraceWithRemainderPattern(level, padding) =>
        level match {
          case StackTracePattern(trace) =>
            nestedTestFailureBuilder.stacktrace += trace
            parseExceptionOptional(exceptions, nestedTestFailureBuilder, padding)
        }
      case StackTraceWithoutRemainderPattern(level) =>
        level match {
          case StackTracePattern(trace) =>
            nestedTestFailureBuilder.stacktrace += trace
            exceptions += nestedTestFailureBuilder.build
            Some(exceptions.toArray)
        }
      case MoreStackTraceWithRemainderPattern(level, padding) =>
        nestedTestFailureBuilder.stacktrace += level
        exceptions += nestedTestFailureBuilder.build
        parseRemainder(padding)
      case MoreStackTraceWithoutRemainderPattern(level) =>
        nestedTestFailureBuilder.stacktrace += level
        exceptions += nestedTestFailureBuilder.build
        Some(exceptions.toArray)
      case _ =>
        exceptions += nestedTestFailureBuilder.build
        parseRemainder(remainder)
    }
  }

  private def parseException(text: String, exception: String, exceptionMsg: String, remainder: String): Array[NestedException] = {
    val exceptions = parseExceptionOptional(mutable.ArrayBuffer.empty[NestedException],
      new NestedExceptionBuilder(exception, exceptionMsg), remainder)
    exceptions match {
      case Some(exceptions) => exceptions
      case None =>
        LOG.trace(s"Failing when parsing: $text")
        Array.empty[NestedException]
    }
  }

  private[parser] def parseSingleTestFailure(text: String): (String, String, Array[NestedException]) = text match {
    case SingleTestFailureWithMsgPattern(_, testMethod, testClass, exception, exceptionMsg, remainder) =>
      (testMethod, testClass, parseException(text, exception, exceptionMsg, remainder))
    case SingleTestFailurePattern(_, testMethod, testClass, exception, remainder) =>
      (testMethod, testClass, parseException(text, exception, "", remainder))
  }

  private[parser] def parseLogException(text: String): (String, Array[NestedException]) = {
    text match {
      case ExceptionLogWithMsgPattern(msg, exception, exceptionMsg, remainder) =>
        (msg, parseException(text, exception, exceptionMsg, remainder))
      case ExceptionLogPattern(msg, exception, remainder) =>
        (msg, parseException(text, exception, "", remainder))
      case _ => (text, Array.empty[NestedException])
    }
  }

  private val testDurationRegex = raw"\d+\.\d+"

  // millisecond
  private[parser] def parseDuration(text: String): Int = (text.toDouble * 1000).toInt

  // Must enable DOTALL mode
  private val OK_JUnit4 = raw"(?s)(.*)\nTime: ($testDurationRegex)\n\nOK \(\d+ tests?\)\n".r
  private val FAIL_JUnit4 =
    raw"(?s)(.*)\nTime: ($testDurationRegex)\nThere [a-z]+ (\d+) failures?:\n(.+)\nFAILURES!!!\nTests run: *(\d+), *Failures: *(\d+)\n".r

  private val preambleJUnit5 =
    raw"(.*)\n\nThanks for using JUnit! Support its development at https://junit.org/sponsoring\n"

  private val suffixJUnit5 =
    raw"\nTests? run finished after (\d+) ms\n\[.*\[ *(\d+) tests successful *\]\n\[ *(\d+) tests failed *\]\n"

  private val JUnit5Pattern = raw"(?s)$preambleJUnit5\n.*$suffixJUnit5".r

  private[parser] trait TestResultBuilder {
    val duration: Int
    def build(datetime: DateTime): TestResult
  }

  private case class OK(duration: Int) extends TestResultBuilder {
    def build(datetime: DateTime): TestOK = TestOK(datetime, duration)
  }

  private case class FAIL(duration: Int, testMethod: String, testClass: String, exceptions: Array[NestedException])
    extends TestResultBuilder {
    def build(datetime: DateTime): TestFail = TestFail(datetime, duration, testMethod, testClass, exceptions)
  }

  def parseTestResultOptional(text: String): Option[(String, TestResultBuilder)] = text match {
    case OK_JUnit4(msg, time) =>
      Some(msg, OK(parseDuration(time)))
    case FAIL_JUnit4(msg, time, _, failures, _, _) =>
      parseSingleTestFailure(failures) match {
        case (testMethod, testClass, exceptions) =>
          Some(msg, FAIL(parseDuration(time), testMethod, testClass, exceptions))
      }
    case JUnit5Pattern(msg, time, successful, failed) =>  // TODO: extract the failure content
      if (successful.toInt == 1) Some(msg, OK(time.toInt))
      else if (failed.toInt == 1) Some(msg, FAIL(time.toInt, "", "", Array.empty[NestedException]))
      else throw new RuntimeException(s"can't recognize test result: ${text.substring(msg.length, text.length)}")
    case _ => None
  }

  private val numbersRegex = raw"([0-9 ,]*)"
  private val FeedbackLogPattern1 = raw"$datetimeRegex - $typeRegex \[.*\] - injection allow set: \[$numbersRegex\]".r
  private val FeedbackLogPattern2 = raw"$datetimeRegex \[myid:\] - $typeRegex \[.*\] - injection allow set: \[$numbersRegex\]".r

  private def parseLogSetOptional(text: String): Option[Array[Int]] = {
    def parseNumbers(numbers: String): Array[Int] =
      if (numbers.isEmpty) Array.empty[Int] else numbers.split(raw", ").map(_.toInt)
    text match {
      case FeedbackLogPattern1(_, _, numbers) => Some(parseNumbers(numbers))
      case FeedbackLogPattern2(_, _, numbers) => Some(parseNumbers(numbers))
      case _ => None
    }
  }

  def parseLogSet(text: String): Array[Int] = parseLogSetOptional(text) match {
    case Some(numbers) => numbers
    case None => throw new RuntimeException(s"mismatch error: $text")
  }

  private val LogDirPattern = raw"logs-(\d+)".r

  def parseLogDirId(text: String): Int = text match {
    case LogDirPattern(id) => id.toInt
  }
}
