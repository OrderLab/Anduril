package feedback

import feedback.common.ActionMayThrow
import feedback.log.entry.ExceptionLogEntry
import feedback.log.exception.{NativeStackTraceElement, NormalStackTrace, NormalStackTraceElement, StackTraceRecord, UnknownStackTraceElement}
import feedback.log.{DistributedWorkloadLog, Log, LogFile, NormalLogFile, TraceLogFile, UnitTestLog}
import feedback.parser.InjectionPoint

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object LogStatistics {
  val prefix: String = System.getProperty("analysis.prefix", "org.apache.zookeeper")

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

  final case class StackTraceInjection( exception: String,
                                        className: String,
                                        methodName: String,
                                        fileName: String,
                                        lineNumber: Int,
                                        var stackTrace: Array[String]) extends Serializable {
    require(className != null && className.nonEmpty)
    require(methodName != null && methodName.nonEmpty)
    require(fileName != null && fileName.nonEmpty)
    require(lineNumber > 0)
    override def equals(o: Any) = o match {
      case that: StackTraceInjection => that.exception == exception &&
        that.className == className &&
        that.methodName == methodName &&
        that.fileName == fileName &&
        that.lineNumber == lineNumber
      case _ => false
    }

  }

  def collectExceptionStackTrace(log: Log): Array[StackTraceInjection] = log match {
    case UnitTestLog(logFile, _) => collectExceptionStackTrace(logFile)
  }


  def collectExceptionStackTrace(log: LogFile): Array[StackTraceInjection] = log match {
    case NormalLogFile(header, entries) =>
      val injections = mutable.ArrayBuffer.empty[StackTraceInjection]
      var set = Set[StackTraceInjection]()
      entries.foreach {
        case ExceptionLogEntry(_, _, logType, thread, classname, fileLogLine, msg, nestedException) =>
          val mostInner = nestedException.exceptions.last
          var found = false
          var si : StackTraceInjection = null
          val st = mutable.ArrayBuffer.empty[String]
          var halt = false
          //nestedException.exceptions.foreach { ex =>
          //  println(ex.stacktrace)
          //}
          for (es <- mostInner.stacktrace.stack.stack) {
            if (!halt) {
              if (!found) {
                es match {
                  case NormalStackTraceElement(className, methodName, fileName, line) =>
                    if (className.contains(prefix)) {
                      //println(mostInner.exception)
                      //println(className + " " + methodName + " " + line)
                      if (!set.contains(StackTraceInjection(mostInner.exception, className, methodName, fileName, line, null))) {
                        set += StackTraceInjection(mostInner.exception, className, methodName, fileName, line, null)
                        found = true
                        si = StackTraceInjection(mostInner.exception, className, methodName, fileName, line, null)
                        st += es.literal
                      } else {
                        halt = true
                      }
                    }
                  case _ => None
                }
              } else {
                st += es.literal
              }
            }
          }
          if (found) {
            si.stackTrace = st.toArray
            if (si.stackTrace.size != 0) {
              injections += si
            }
          }
        case _ => None
      }
      injections.toArray
  }
}
