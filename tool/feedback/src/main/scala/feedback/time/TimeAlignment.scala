package feedback.time

import feedback.diff.{LogFileDiff, ThreadDiff}
import feedback.log.entry.{InjectionRecord, LogEntry}
import feedback.log.{DistributedWorkloadLog, LogFile, NormalLogFile, TraceLogFile, UnitTestLog}
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
  //private var difference = new TimeDifference(good, bad)
  private var good_base = good.showtime
  private var bad_base = bad.showtime
  private var good_end = good.showtime
  private var bad_end = bad.showtime
  private var scale : Double = 1

  def forward(logLine: Int, showtime: DateTime): DateTime = {
    if (k < intervals.size) {
      intervals(k) match {
        case (x, y) =>
          require(logLine <= x)
          if (logLine == x) {
            //difference = new TimeDifference(showtime, badEntries.get(y))
            good_base = showtime
            bad_base = badEntries.get(y)
            do k += 1 while (k < intervals.size && intervals(k)._2 < y)
            if (k < intervals.size && (goodEntries.get(intervals(k)._1).getMillis - good_base.getMillis) > 0) {
              scale = (badEntries.get(intervals(k)._2).getMillis - bad_base.getMillis).toDouble / (goodEntries.get(intervals(k)._1).getMillis - good_base.getMillis).toDouble
              require(scale >= 0)
            } else scale = 1
          }
      }
    }
    bad_base.plus((scale*(showtime.getMillis - good_base.getMillis)).toLong)
    /**
    if (k < intervals.size) {
      val futureDifference = new TimeDifference(goodEntries.get(intervals(k)._1), badEntries.get(intervals(k)._2))
      good.entries.find(!_.showtime.isBefore(showtime)).map(e => futureDifference.good2bad(e.showtime))
        .getOrElse(difference.good2bad(showtime))
    } else difference.good2bad(showtime)
     **/
  }

  def forward(showtime: DateTime): DateTime = {
    if (k < intervals.size && good_end.getMillis < showtime.getMillis) {
      good_base = good_end
      bad_base = bad_end
      do {
        if (goodEntries.get(intervals(k)._1).getMillis > good_base.getMillis) {
          good_base = goodEntries.get(intervals(k)._1)
          bad_base = badEntries.get(intervals(k)._2)
        }
        k += 1
      } while (k < intervals.size && goodEntries.get(intervals(k)._1).getMillis < showtime.getMillis)
      if (k < intervals.size) {
        good_end = goodEntries.get(intervals(k)._1)
        bad_end = badEntries.get(intervals(k)._2)
        scale = (bad_end.getMillis - bad_base.getMillis).toDouble / (good_end.getMillis - good_base.getMillis).toDouble
        require(scale >= 0)
      } else scale = 1
    }
    bad_base.plus((scale*(showtime.getMillis - good_base.getMillis)).toLong)
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

  def tracedAlign(good: LogFile, ruler: TimeRuler, tag: LogType, injections: Array[InjectionTiming]): Iterator[InjectionTiming] = {
    var i = 0
    new Iterator[InjectionTiming] {
      override def hasNext: Boolean = (i < injections.length)
      override def next(): InjectionTiming = {
        val time = ruler.forward(injections(i).showtime)
        i += 1
        InjectionTiming(time, injections(i).pid, injections(i).injection, injections(i).occurrence)
      }
    }
  }

  def tracedAlign(good: LogFile, bad: LogFile, tag: LogType): Iterator[Timestamp] =
    tracedAlign(good, new TimeRuler(good, bad), tag)

  def tracedAlign(good: LogFile, bad: LogFile, tag: LogType, injections: Array[InjectionTiming]): Iterator[InjectionTiming] =
    tracedAlign(good, new TimeRuler(good, bad), tag, injections)

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
