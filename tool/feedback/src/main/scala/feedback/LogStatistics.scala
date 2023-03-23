package feedback

import feedback.common.ActionMayThrow
import feedback.diff.{DiffDump, LogFileDiff, ThreadDiff}
import feedback.log.{DistributedWorkloadLog, Log, UnitTestLog}
import feedback.parser.TextParser
import feedback.symptom.Symptoms
import feedback.time.TimeAlgorithms
import javax.json.JsonObject

object LogStatistics {

  def countLines(log: Log, action: ActionMayThrow[String]): Unit = log match {
    case UnitTestLog(logFile, _) =>
        action.accept(logFile.entries.length.toString)
    case DistributedWorkloadLog(logFiles) =>
      logFiles foreach {
        logFile => action.accept(logFile.entries.length.toString)
      }
  }

}
