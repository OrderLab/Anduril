package feedback.time

import feedback.common.ActionMayThrow
import feedback.log.entry.InjectionRecord
import feedback.log.{Log, LogTestUtil, NormalLogFile, TraceLogFile, UnitTestLog}
import feedback.parser.{LogFileParser, LogParser}
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import java.nio.file.Path
import javax.json.JsonObject
import scala.util.Sorting

private object TimelineTestUtil {
  private def getDateTimeLiteral(time: DateTime): String = time.toString(LogFileParser.datetimeFormatter)

  private val injectionLimit       = 1_000_000
  private val injectionDigitLimit  = 7
  private val occurrenceLimit      = 1_000_000
  private val occurrenceDigitLimit = 7

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
    s"%-${msgLimit}s".format(StringUtils.abbreviate(msg, msgLimit))

  def logEntryTimingFormat(datetime: DateTime, logType: LogType, msg: String): String = {
    val literal = s"${getDateTimeLiteral(datetime)} ${msgFormat(msg)}"
    val padding = " " * literal.length
    logType match {
      case LogType.TRIAL => s"$literal   $padding   $padding"
      case LogType.GOOD  => s"$padding   $literal   $padding"
      case LogType.BAD   => s"$padding   $padding   $literal"
    }
  }

  private val timeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm:ss,SSS")

  private def getTimeLiteral(time: DateTime): String = time.toString(timeFormatter)

  def getInjectionInterval(count: Int, begin: DateTime, end: DateTime): String = {
    require(0 < count)
    require(count <= occurrenceLimit)
    require(!begin.isAfter(end))
    s"${s"%${occurrenceDigitLimit}d".format(count)} injections from ${getTimeLiteral(begin)} to ${getTimeLiteral(end)}"
  }
}

sealed trait Timestamp extends Timing {
  def literal: String
}

sealed abstract class BugCase(val name: String, tempDir: Path) {
  this.prepareTempFiles()
  private[this] val dir = tempDir.resolve(this.name)
  val good: Log = LogParser.parseLog(dir.resolve("good-run-log"))
  val bad: Log = LogParser.parseLog(dir.resolve("bad-run-log"))
  val trial: Log = LogParser.parseLog(dir.resolve("trial-run-log"))
  val spec: JsonObject = LogTestUtil.loadJson(s"record-inject/$name/tree.json")

  def prepareTempFiles(): Unit
  def print(printer: ActionMayThrow[String]): Unit
}

final case class Injection(override val showtime: DateTime, injection: Int, occurrence: Int) extends Timestamp {
  override def literal: String = TimelineTestUtil.injectionTimingFormat(showtime, injection, occurrence)
}

final case class LogEntry(override val showtime: DateTime, logType: LogType, msg: String) extends Timestamp {
  override def literal: String = TimelineTestUtil.logEntryTimingFormat(showtime, logType, msg)
}

final class TestCase(override val name: String, tempDir: Path) extends BugCase(name, tempDir) {
  override def prepareTempFiles(): Unit = {
    LogTestUtil.initTempFile(s"ground-truth/$name/good-run-log.txt",
      tempDir.resolve(s"$name/good-run-log"))
    LogTestUtil.initTempFile(s"ground-truth/$name/bad-run-log.txt",
      tempDir.resolve(s"$name/bad-run-log"))
    LogTestUtil.initTempFile(s"record-inject/$name/good-run-log.txt",
      tempDir.resolve(s"$name/trial-run-log"))
  }

  def print(printer: ActionMayThrow[String]): Unit = {
    val trial2bad = new TimeDifference(this.trial, this.bad)
    val good2bad = new TimeDifference(this.good, this.bad)
    val occurrences = new java.util.TreeMap[Integer, Integer];
    val injections = this.trial match {
      case UnitTestLog(TraceLogFile(_, _, injections), _) =>
        injections.iterator map {
          case InjectionRecord(_, showtime, injection) =>
            Injection(trial2bad.good2bad(showtime), injection, occurrences.merge(injection, 1, _ + _))
        }
    }
    val trial = this.trial match {
      case UnitTestLog(TraceLogFile(_, entries, _), _) =>
        entries.iterator map { entry =>
          LogEntry(trial2bad.good2bad(entry.showtime), LogType.TRIAL, entry.msg)
        }
    }
    val good = this.good match {
      case UnitTestLog(NormalLogFile(_, entries), _) =>
        entries.iterator map { entry =>
          LogEntry(good2bad.good2bad(entry.showtime), LogType.GOOD, entry.msg)
        }
    }
    val bad = this.bad match {
      case UnitTestLog(NormalLogFile(_, entries), _) =>
        entries.iterator map { entry =>
          LogEntry(entry.showtime, LogType.BAD, entry.msg)
        }
    }
    val timeline = (injections ++ trial ++ good ++ bad).toArray[Timestamp]
    Sorting.stableSort(timeline)
    this.print(timeline, printer)
  }

  private[this] def print(timeline: Array[Timestamp], printer: ActionMayThrow[String]): Unit = {
    // each line:  trial log   good log   bad log
    var count = 0
    var begin: Option[DateTime] = None
    var end: Option[DateTime] = None
    for (timing <- timeline) {
      if (timing.isInstanceOf[Injection]) {
        if (count == 0) {
          begin = Some(timing.showtime)
        }
        end = Some(timing.showtime)
        count += 1
      }
      else {
        if (count != 0) {
          printer.accept(TimelineTestUtil.getInjectionInterval(count, begin.get, end.get))
          count = 0
        }
        printer.accept(timing.literal)
      }
    }
    if (count != 0) {
      printer.accept(TimelineTestUtil.getInjectionInterval(count, begin.get, end.get))
    }
  }
}
