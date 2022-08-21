package feedback

import feedback.diff.{DiffDump, LogFileDiff, ThreadDiff}
import feedback.log.{DistributedWorkloadLog, Log, UnitTestLog}
import feedback.parser.TextParser
import feedback.symptom.Symptoms

import java.util.function.Consumer
import javax.json.JsonObject

object Algorithms {

  def computeLocationFeedback(good: Log, bad: Log, trial: Log, spec: JsonObject, action: Consumer[Integer]): Unit = {
    val expectedDiff = computeDiff(good, bad)
    val actualDiff = computeDiff(good, trial)
    val wanted = new java.util.HashMap[ThreadDiff.CodeLocation, Integer]
    expectedDiff foreach { _.dumpBadDiff { e => wanted.put(e, 0) } }
    val eventNumber = spec.getInt("start")
    require(eventNumber == wanted.size + (if (Symptoms.isResultEventLogged(spec)) 0 else 1))
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
    if (Symptoms.isResultEventLogged(spec)) {
      wanted.values.forEach { i =>
        if (i != 0) {
          action.accept(i)
        }
      }
    } else wanted.values.forEach(action)
    if (Symptoms.findResultEvent(trial, spec).isEmpty) {
      action.accept(0)
    }
  }

  def computeTimeFeedback(good: Log, bad: Log, spec: JsonObject): Serializable = {
    throw new RuntimeException
  }

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

  def computeDiff(good: Log, bad: Log, action: Consumer[ThreadDiff.CodeLocation]): Unit = {
    val distinct = new LogFileDiff.DistinctConsumer(action)
    computeDiff(good, bad) foreach { _.dumpBadDiff(distinct) }
  }
}
