package feedback.time

import feedback.diff.ThreadDiff
import feedback.log.entry.LogEntry
import feedback.log.{DistributedWorkloadLog, Log, NormalLogFile, UnitTestLog}
import feedback.parser.{DistributedInjectionTraces, InjectionPoint, InjectionRecordsReader, InjectionTrace, UnitTestInjectionTrace}
import feedback.symptom.Symptoms
import org.joda.time.DateTime
import runtime.graph.PriorityGraph
import runtime.time.TimePriorityTable
import javax.json.JsonObject

import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.jdk.CollectionConverters._
import scala.util.Sorting

sealed trait TimelineType extends Timing

final case class InjectionTiming(override val showtime: DateTime, pid: Int, injection: Int, occurrence: Int) extends TimelineType

final case class NormalLogTiming(override val showtime: DateTime) extends TimelineType

final case class CriticalLogTiming(override val showtime: DateTime, id: Int) extends TimelineType

object TimeAlgorithms {
  def computeTimeFeedback(good: Log, bad: Log, spec: JsonObject,
                          events: Array[Option[ThreadDiff.CodeLocation]], traceCSV:Option[InjectionTrace]): Serializable = {
    val isResultEventLogged = Symptoms.isResultEventLogged(spec)
    val eventList = (if (isResultEventLogged) events.toList.zipWithIndex else events.toList.zipWithIndex.drop(1)) map {
      case (Some(location), event) => (location, event)
    }
    val goodTimeline = (good, bad) match {
        // Unit test workload
      case (UnitTestLog(good, _), UnitTestLog(bad, _)) =>
        traceCSV match {
            // Injection Trace in log file
          case None => TimeAlignment.tracedAlign (good, bad, LogType.GOOD) flatMap {
            case Injection (showtime, injection, occurrence) =>
              Some (InjectionTiming (showtime, - 1, injection.injection, occurrence) )
            case _ => None
          }
            // Injection Trace in separate CSV
          case Some(injectionArrayWrapper) => injectionArrayWrapper match {
            case UnitTestInjectionTrace(injectionArray) =>
              val sorted : Array[Timestamp] = injectionArray.map {
                case InjectionPoint(_, id, occurrence, time, thread) => RecordedInjection( - 1, id, occurrence,time,thread)
              }
              Sorting.stableSort(sorted)
              TimeAlignment.tracedAlign(good,bad,LogType.GOOD,sorted) flatMap  {
                case RecordedInjection( _, id, occurrence,time,_) => Some (InjectionTiming (time, - 1, id, occurrence))
                case _ => None
              }
          }
        }
        // Distributed workload
      case (DistributedWorkloadLog(goodLogs), DistributedWorkloadLog(badLogs)) =>
        traceCSV match {
            // Injection Trace in log file
          case None =>
            goodLogs.zipAll(badLogs, null, null).zipWithIndex map {
              case ((g, b), pid) => TimeAlignment.tracedAlign(g, b, LogType.GOOD) flatMap {
                case Injection(showtime, injection, occurrence) =>
                  Some(InjectionTiming(showtime, pid, injection.injection, occurrence))
                case _ => None
              }
            } reduce { _ ++ _ }
            // Injection Trace in separate CSV
          case Some(injectionArrayWrapper) => injectionArrayWrapper match {
            case DistributedInjectionTraces(injectionArrayArray) =>
              goodLogs.zipAll(badLogs, null, null).zipAll(injectionArrayArray, null, null).zipWithIndex map {
                case (((g, b), a), pid) =>
                  val sorted : Array[Timestamp] = a.map {
                    case InjectionPoint(_, id, occurrence, time, thread) => RecordedInjection( pid, id, occurrence,time,thread)
                  }
                  Sorting.stableSort(sorted)
                  TimeAlignment.tracedAlign(g,b,LogType.GOOD,sorted) flatMap  {
                    case RecordedInjection( _, id, occurrence,time,_) => Some (InjectionTiming (time, pid, id, occurrence))
                    case _ => None
                  }
              } reduce { _ ++ _ }
          }
        }
    }

    var limit = 0
    var badTimeline = (good, bad) match {
      case (UnitTestLog(good, _), UnitTestLog(bad, _)) =>
        bad match {
          case NormalLogFile(_, entries) =>
            limit += entries.length
            val badOnlySet =
                new ThreadDiff(null,
                  new java.util.ArrayList[LogEntry](good.entries.toList.asJava),
                  new java.util.ArrayList[LogEntry](bad.entries.toList.asJava)).badOnlyList.asScala.toSet
            entries.iterator map { entry =>
              eventList find {
                case (location, _) =>
                  location.classname.equals(entry.classname) &&
                    location.fileLogLine == entry.fileLogLine &&
                    badOnlySet.contains(entry.logLine)
              } map {
                case (_, event) => CriticalLogTiming(entry.showtime, event)
              } getOrElse NormalLogTiming(entry.showtime)
            }
        }
      case (DistributedWorkloadLog(goodLogs), DistributedWorkloadLog(badLogs)) =>
        goodLogs.zipAll(badLogs, null, null).zipWithIndex map {
          case ((g, b), _) =>
            b match {
              case NormalLogFile(_, entries) =>
                limit += entries.length
                val badOnlySet =
                  new ThreadDiff(null,
                    new java.util.ArrayList[LogEntry](g.entries.toList.asJava),
                    new java.util.ArrayList[LogEntry](b.entries.toList.asJava)).badOnlyList.asScala.toSet
                entries.iterator map { entry =>
                  eventList find {
                    case (location, _) =>
                      location.classname.equals(entry.classname) &&
                        location.fileLogLine == entry.fileLogLine &&
                          badOnlySet.contains(entry.logLine)
                  } map {
                    case (_, event) => CriticalLogTiming(entry.showtime, event)
                  } getOrElse NormalLogTiming(entry.showtime)
                }
            }
        } reduce { _ ++ _ }
    }

    if (!isResultEventLogged) {
      Symptoms.findResultEvent(bad, spec) foreach { timing =>
        badTimeline ++= Iterator(CriticalLogTiming(timing.showtime, 0))
      }
    }
    val timeline = (goodTimeline ++ badTimeline).toArray[Timing]
    Sorting.stableSort(timeline)
    val table = good match {
      case UnitTestLog(_, _) => new TimePriorityTable(false, 1)
      case DistributedWorkloadLog(logs) => new TimePriorityTable(true, logs.length)
    }
    Timeline.computeTimeFeedback(timeline, events.length, new PriorityGraph(spec), table, limit)
  }
}
