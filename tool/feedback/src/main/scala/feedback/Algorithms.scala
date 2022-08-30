package feedback

import feedback.common.ActionMayThrow
import feedback.diff.{DiffDump, LogFileDiff, ThreadDiff}
import feedback.log.entry.InjectionRecord
import feedback.log.{DistributedWorkloadLog, Log, NormalLogFile, TraceLogFile, UnitTestLog}
import feedback.parser.TextParser
import feedback.symptom.{DistributedWorkloadLogEvent, Symptoms, UnitTestLogEvent}
import feedback.time.{TimeDifference, Timeline, Timing}
import org.joda.time.DateTime
import runtime.graph.PriorityGraph
import runtime.time.TimePriorityTable

import javax.json.JsonObject
import scala.util.Sorting

object Algorithms {

  def computeLocationFeedback(good: Log, bad: Log, trial: Log, spec: JsonObject, action: ActionMayThrow[Integer]): Unit = {
    val expectedDiff = computeDiff(good, bad)
    val actualDiff = computeDiff(good, trial)
    val wanted = new java.util.HashMap[ThreadDiff.CodeLocation, Integer]
    expectedDiff foreach { _.dumpBadDiff { e => wanted.put(e, -1) } }
    val eventNumber = spec.getInt("start")
    require(eventNumber <= wanted.size + (if (Symptoms.isResultEventLogged(spec)) 0 else 1))
    val array = spec.getJsonArray("nodes")
    (0 until array.size) foreach { i =>
      val node = array.getJsonObject(i)
      val id = node.getInt("id")
      if (id < eventNumber && id != 0) {
        require(node.getString("type") equals "location_event")
        val location = node.getJsonObject("location")
        val entry = new ThreadDiff.CodeLocation(
          TextParser.getSimpleClassName(location.getString("class")), location.getInt("line_number"))
        require(wanted.put(entry, id) != null)
      }
    }
    actualDiff foreach { _.dumpBadDiff { e => wanted.remove(e) } }
    def filter(i: Int): Unit = if (i != -1) action.accept(i)
    if (Symptoms.isResultEventLogged(spec)) {
      wanted.values.forEach { i =>
        if (i != 0) {
          filter(i)
        }
      }
    } else wanted.values.forEach { filter(_) }
    if (Symptoms.findResultEvent(trial, spec).isEmpty) {
      filter(0)
    }
  }

  def computeTimeFeedback(good: Log, bad: Log, spec: JsonObject): Serializable = {
    val eventNumber = spec.getInt("start")
    val isResultEventLogged = Symptoms.isResultEventLogged(spec)
    val array = spec.getJsonArray("nodes")
    val events = Array.fill[Option[ThreadDiff.CodeLocation]](eventNumber)(None)
    (0 until array.size) foreach { i =>
      val node = array.getJsonObject(i)
      val id = node.getInt("id")
      if (id < eventNumber && (id != 0 || isResultEventLogged)) {
        require(node.getString("type") equals "location_event")
        val location = node.getJsonObject("location")
        val entry = new ThreadDiff.CodeLocation(
          TextParser.getSimpleClassName(location.getString("class")), location.getInt("line_number"))
        events(id) = Some(entry)
      }
    }
    val eventList = (if (isResultEventLogged) events.toList.zipWithIndex else events.toList.zipWithIndex.drop(1)) map {
      case (Some(location), event) => (location, event)
    }
    val difference = new TimeDifference(good, bad)
    val occurrences = new java.util.TreeMap[Integer, Integer]
    val injections = (good match {
      case UnitTestLog(log, _) => Iterator((log, -1))
      case DistributedWorkloadLog(logs) => logs.iterator.zipWithIndex
    }) flatMap {
      case (TraceLogFile(_, _, injections), pid) => injections.iterator map {
        case InjectionRecord(_, showtime, injection) =>
          InjectionTiming(difference.good2bad(showtime), pid, injection,
            occurrences.merge(injection, 1, _ + _))
      }
    }
    val entries = (bad match {
      case UnitTestLog(log, _) => Iterator(log)
      case DistributedWorkloadLog(logs) => logs.iterator.zipWithIndex
    }) flatMap {
      case NormalLogFile(_, entries) => entries.iterator map { entry =>
        eventList find {
          case (location, _) =>
            location.classname.equals(entry.classname) &&
              location.fileLogLine == entry.fileLogLine
        } map {
          case (_, event) => CriticalLogTiming(entry.showtime, event)
        } getOrElse NormalLogTiming(entry.showtime)
      }
    }
    val symptom = if (isResultEventLogged) Iterator() else
      Symptoms.findResultEvent(bad, spec).iterator flatMap {
        case UnitTestLogEvent(entry) =>
          Iterator(CriticalLogTiming(entry.showtime, -1))
        case DistributedWorkloadLogEvent(node, entry) =>
          Iterator(CriticalLogTiming(entry.showtime, node))
      }
    val timeline = (injections ++ entries ++ symptom).toArray[Timing]
    Sorting.stableSort(timeline)
    val table = good match {
      case UnitTestLog(_, _) => new TimePriorityTable(false, 1)
      case DistributedWorkloadLog(logs) => new TimePriorityTable(true, logs.length)
    }
    Timeline.computeTimeFeedback(timeline, events.length, new PriorityGraph(spec), table)
  }

  final case class InjectionTiming(override val showtime: DateTime, pid: Int, injection: Int, occurrence: Int) extends Timing

  final case class NormalLogTiming(override val showtime: DateTime) extends Timing

  final case class CriticalLogTiming(override val showtime: DateTime, id: Int) extends Timing

  def computeDiff(good: Log, bad: Log): List[DiffDump] = (good, bad) match {
    case (UnitTestLog(goodLogFile, _), UnitTestLog(badLogFile, _)) =>
      List(new LogFileDiff(goodLogFile, badLogFile))
    case (DistributedWorkloadLog(goodLogs), DistributedWorkloadLog(badLogs)) =>
      require(goodLogs.length == badLogs.length)
      goodLogs.zip(badLogs).toList map {
        case (goodLogFile, badLogFile) =>
          new LogFileDiff(goodLogFile, badLogFile)
      }
  }

  def computeDiff(good: Log, bad: Log, action: ActionMayThrow[ThreadDiff.CodeLocation]): Unit = {
    val set = new java.util.HashSet[ThreadDiff.CodeLocation]
    computeDiff(good, bad) foreach { _.dumpBadDiff(e =>
      if (set.add(e)) {
        action.accept(e)
      }
    )}
  }
}
