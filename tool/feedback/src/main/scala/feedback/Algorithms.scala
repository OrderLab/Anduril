package feedback

import feedback.common.ActionMayThrow
import feedback.diff.{DiffDump, LogFileDiff, ThreadDiff}
import feedback.log.{DistributedWorkloadLog, Log, UnitTestLog}
import feedback.parser.TextParser
import feedback.symptom.Symptoms
import feedback.time.TimeAlgorithms

import javax.json.JsonObject

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
    TimeAlgorithms.computeTimeFeedback(good, bad, spec, events)
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

  def computeDoubleDiff1(good: Log, bad: Log, trial: Log, action: ActionMayThrow[ThreadDiff.CodeLocation]): Unit = {
    val set = new java.util.HashSet[ThreadDiff.CodeLocation]
    computeDiff(good, bad) foreach { _.dumpBadDiff(e =>
      set.add(e)
    )}
    computeDiff(good, trial) foreach { _.dumpBadDiff(e =>
      set.remove(e)
    )}
    set.forEach { i =>
      action.accept(i);
    }
  }

  def computeDoubleDiff(good: Log, bad: Log, trial: Log, action: ActionMayThrow[ThreadDiff.CodeLocation]): Unit = {
    val set = new java.util.HashSet[ThreadDiff.CodeLocation]
    val expected = computeDiff(good, bad)
    val actual = computeDiff(good, trial)
    val result = (actual zip expected).map{ case (a, e) =>
      new ThreadDiff(a.sortCodeLocationInThreadOrder(),e.sortCodeLocationInThreadOrder())}
    result foreach {
      _.dumpBadDiff(e =>
        set.add(e)
      )
    }
    set.forEach { i =>
      action.accept(i);
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
