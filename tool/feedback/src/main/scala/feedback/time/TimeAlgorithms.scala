package feedback.time

import feedback.diff.ThreadDiff
import feedback.log.entry.LogEntry
import feedback.log.{DistributedWorkloadLog, Log, UnitTestLog}
import feedback.symptom.Symptoms
import org.joda.time.DateTime
import runtime.graph.PriorityGraph
import runtime.time.TimePriorityTable

import javax.json.JsonObject
import scala.util.Sorting

final case class InjectionTiming(override val showtime: DateTime, pid: Int, injection: Int, occurrence: Int) extends Timing

sealed trait LogTiming extends Timing {
  val entry: LogEntry
}

final case class NormalLogTiming(override val showtime: DateTime, override val entry: LogEntry) extends LogTiming

final case class CriticalLogTiming(override val showtime: DateTime, override val entry: LogEntry, id: Int) extends LogTiming

object TimeAlgorithms {
  def computeTimeFeedback(good: Log, bad: Log, spec: JsonObject, events: Array[Option[ThreadDiff.CodeLocation]]): Serializable = {
    val isResultEventLogged = Symptoms.isResultEventLogged(spec)
    val eventList = (if (isResultEventLogged) events.toList.zipWithIndex else events.toList.zipWithIndex.drop(1)) map {
      case (Some(location), event) => (location, event)
    }
    val iterator = (good, bad) match {
      case (UnitTestLog(good, _), UnitTestLog(bad, result)) =>
        TimeAlignment.tracedAlign(good, bad, LogType.GOOD) map {
          case Injection(showtime, injection, occurrence) =>
            InjectionTiming(showtime, -1, injection.injection, occurrence)
          case LogLine(showtime, entry, _) =>
            eventList find {
              case (location, _) =>
                location.classname.equals(entry.classname) &&
                  location.fileLogLine == entry.fileLogLine
            } map {
              case (_, event) => CriticalLogTiming(showtime, entry, event)
            } getOrElse NormalLogTiming(showtime, entry)
        }
    }
    val timeline = iterator.toArray[Timing]
    Sorting.stableSort(timeline)
    val table = good match {
      case UnitTestLog(_, _) => new TimePriorityTable(false, 1)
      case DistributedWorkloadLog(logs) => new TimePriorityTable(true, logs.length)
    }
    Timeline.computeTimeFeedback(timeline, events.length, new PriorityGraph(spec), table)
  }
}
