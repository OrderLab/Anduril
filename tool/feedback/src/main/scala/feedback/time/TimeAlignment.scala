package feedback.time

import feedback.diff.{LogFileDiff, ThreadDiff}
import feedback.log.entry.{InjectionRecord, LogEntry}
import feedback.log.{LogFile, NormalLogFile, TraceLogFile}
import feedback.parser.LogFileParser
import org.joda.time.DateTime

import scala.jdk.CollectionConverters.SeqHasAsJava

sealed trait Timestamp extends Timing

final case class Injection(override val showtime: DateTime, injection: InjectionRecord, occurrence: Int) extends Timestamp

final case class LogLine(override val showtime: DateTime, entry: LogEntry, logType: LogType) extends Timestamp

final class TimeRuler(good: LogFile, bad: LogFile,
                      goodEntries: java.util.Map[Integer, DateTime], badEntries: java.util.Map[Integer, DateTime]) {
  def this(good: LogFile, bad: LogFile) = this(good, bad,
    TimeAlignment.getEntryMapping(good), TimeAlignment.getEntryMapping(bad))

//  private val intervals = new LogFileDiff(good, bad).getIntervals.toArray(new Array[(java.lang.Integer, java.lang.Integer)](0))
  private val intervals = new ThreadDiff(null,
    new java.util.ArrayList[LogEntry](good.entries.toList.asJava),
    new java.util.ArrayList[LogEntry](bad.entries.toList.asJava)).common

  private var k = 0
  private var difference = new TimeDifference(good, bad)

  def forward(logLine: Int, showtime: DateTime): DateTime = {
    if (k < intervals.size) {
      intervals(k) match {
        case (x, y) =>
          require(logLine <= x)
          if (logLine == x) {
            difference = new TimeDifference(showtime, badEntries.get(y))
            do k += 1 while (k < intervals.size && intervals(k)._2 < y)
          }
      }
    }
    if (k < intervals.size) {
      val futureDifference = new TimeDifference(goodEntries.get(intervals(k)._1), badEntries.get(intervals(k)._2))
      good.entries.find(!_.showtime.isBefore(showtime)).map(e => futureDifference.good2bad(e.showtime))
        .getOrElse(difference.good2bad(showtime))
    } else difference.good2bad(showtime)
  }
}

object TimeAlignment {
  def tracedAlign(good: LogFile, ruler: TimeRuler, tag: LogType): Iterator[Timestamp] = {
    good match {
      case TraceLogFile(_, entries, injections) =>
        val occurrences = new java.util.TreeMap[Integer, Integer]
        var i = 0
        var j = 0
        new Iterator[Timestamp] {
          override def hasNext: Boolean = i < entries.length || j < injections.length
          override def next(): Timestamp =
            if (i == entries.length || (j < injections.length && entries(i).logLine > injections(j).logLine)) {
              val injection = injections(j)
              j += 1
              val time = ruler.forward(injection.logLine, injection.showtime)
              Injection(time, injection, occurrences.merge(injection.injection, 1, _ + _))
            } else {
              val entry = entries(i)
              i += 1
              val time = ruler.forward(entry.logLine, entry.showtime)
              LogLine(time, entry, tag)
            }
        }
    }
  }

  def tracedAlign(good: LogFile, bad: LogFile, tag: LogType): Iterator[Timestamp] =
    tracedAlign(good, new TimeRuler(good, bad), tag)

  def normalAlign(good: LogFile, ruler: TimeRuler, tag: LogType): Iterator[Timestamp] = {
    good match {
      case NormalLogFile(_, entries) =>
        entries.iterator map { entry =>
          LogLine(ruler.forward(entry.logLine, entry.showtime), entry, tag)
        }
    }
  }

  def getEntryMapping(bad: LogFile): java.util.Map[Integer, DateTime] = {
    val badEntries = new java.util.TreeMap[Integer, DateTime]
    bad match {
      case NormalLogFile(_, entries) =>
        entries foreach { entry =>
          badEntries.put(entry.logLine, entry.showtime)
        }
      case TraceLogFile(header, entries, injections) =>
        entries foreach { entry =>
          badEntries.put(entry.logLine, entry.showtime)
        }
    }
    badEntries
  }
}
