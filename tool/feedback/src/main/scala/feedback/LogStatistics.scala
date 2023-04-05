package feedback

import feedback.common.ActionMayThrow
import feedback.log.entry.ExceptionLogEntry
import feedback.log.{DistributedWorkloadLog, Log, LogFile, TraceLogFile, UnitTestLog}

object LogStatistics {
  private def countExceptions(log: LogFile): Int = log.entries.count(_ match {
    case ExceptionLogEntry(logLine, showtime, logType, thread, classname, fileLogLine, msg, nestedException) => true
    case _ => false
  })

  def countLog(log: Log, printer: ActionMayThrow[String]): Unit = log match {
    case UnitTestLog(logFile, _) =>
      printer.accept(s"The number of log entries: ${logFile.entries.length}")
      printer.accept(s"The number of exceptions: ${countExceptions(logFile)}")
    case DistributedWorkloadLog(logFiles) =>
      printer.accept(s"The number of log entries: ${logFiles.map(_.entries.length).sum}")
      printer.accept(s"The number of exceptions: ${logFiles.map(countExceptions).sum}")
  }

  def countUniqueFaults(log: Log, printer: ActionMayThrow[String]): Unit = log match {
    case UnitTestLog(logFile, _) => logFile match {
      case TraceLogFile(header, entries, injections) =>
        var occurred = Set[Int]()
        injections.foreach(inRec =>
          occurred += inRec.injection
        )
        printer.accept(s"The number of dynamic unique inferred faults: ${occurred.size}")
    }
    case DistributedWorkloadLog(logFiles) =>
      var occurred = Set[Int]()
      logFiles.foreach {
        case TraceLogFile(header, entries, injections) =>
          injections.foreach(inRec =>
            occurred += inRec.injection
          )
      }
      printer.accept(s"The number of dynamic unique inferred faults: ${occurred.size}")
  }
}
