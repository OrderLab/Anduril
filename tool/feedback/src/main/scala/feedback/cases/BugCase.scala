package feedback.cases

import feedback.common.ReflectionUtil
import feedback.log._
import feedback.log.entry.{ExceptionLogEntry, LogEntry, LogType, NormalLogEntry}
import feedback.log.exception._
import feedback.parser.TextParser
import feedback.symptom.{DistributedWorkloadLogEvent, SymptomEvent}
import org.joda.time.DateTime

import scala.jdk.CollectionConverters.CollectionHasAsScala

sealed trait BugCase {
  // docs for the bug description, symptom, reproduction, etc.
  final lazy val description: String = "##"

  final lazy val name: String = TextParser.parseCaseDirName(this.getClass.getSimpleName).get

  val isResultEventLogged: Boolean

  def findResultEvent(log: Log): Option[SymptomEvent]

  // by default (in most cases) the result event is exactly the bug symptom
  // if symptom and result are different events, override this instead of findResultEvent,
  // so that we can still use lots of utility functions here
  def findSymptom(log: Log): Option[List[SymptomEvent]]

  // for test:

  // by default, check and only check the first exception, including the message;
  // notice: the exception checking is shared by result check and symptom check,
  // so we need to write the complete symptom check in the cases
  // where the symptom and the result are different things,
  // e.g., ZooKeeper-2247
  def checkNestedException(nestedException: NestedException): Boolean =
    checkException(nestedException.exceptions(0))

  def checkException(exception: ExceptionRecord): Boolean =
    exception match {
      case MsgExceptionRecord(exception, msg, stacktrace) =>
        checkExceptionName(exception) &&
          checkExceptionMsg(Some(msg)) &&
          checkStackTrace(stacktrace)
      case PlainExceptionRecord(exception, stacktrace) =>
        checkExceptionName(exception) &&
          checkExceptionMsg(None) &&
          checkStackTrace(stacktrace)
    }

  // no default string provided
  def checkExceptionMsg(msg: Option[String]): Boolean = true

  def checkExceptionName(exception: String): Boolean = true

  // TODO: check the "... more" exception
  def checkStackTrace(stacktrace: StackTrace): Boolean = {
    val stack = stacktrace.stack.stack
    if (stack.length < targetStackTracePrefix.length) false
    else !targetStackTracePrefix.to(LazyList).zip(stack).exists {
      case (string, record) => !string.equals(record.literal)
    }
  }

  val targetStackTracePrefix: List[String] = List()

  // for symptom:

  // by default find a log entry with exception
  val exceptionalTargetLog = true

  def checkLogType(logType: LogType): Boolean = true

  def checkLogThread(thread: String): Boolean = true

  def checkLogClassName(classname: String): Boolean = true

  def checkFileLogLine(fileLogLine: Int): Boolean = true

  def checkLogMsg(msg: String): Boolean = true

  // reject all logs by default
  def checkLogEntry(logEntry: LogEntry): Boolean = (logEntry, exceptionalTargetLog) match {
    case (ExceptionLogEntry(_, _, logType, thread, classname, fileLogLine, msg, nestedException), true) =>
      checkLogType(logType) &&
        checkLogThread(thread) &&
        checkLogClassName(classname) &&
        checkFileLogLine(fileLogLine) &&
        checkLogMsg(msg) &&
        checkNestedException(nestedException)
    case (NormalLogEntry(_, _, logType, thread, classname, fileLogLine, msg), false) =>
      checkLogType(logType) &&
        checkLogThread(thread) &&
        checkLogClassName(classname) &&
        checkFileLogLine(fileLogLine) &&
        checkLogMsg(msg)
    case _ => false
  }
}

trait UnitTestWorkload extends BugCase {
  override final val isResultEventLogged = false  // JUnit test (4 & 5) always print the result

  // in most test cases, bug symptom appears only if the test fails
  val ok_is_good = true

  // the default check (all are true unless otherwise specified)
  def findTestResultEvent(showtime: DateTime, duration: Int, testMethod: String, testClass: String,
                          nestedException: NestedException): Boolean =
    checkClassName(testClass) &&
      checkMethodName(testMethod) &&
      checkNestedException(nestedException)

  def checkClassName(testClass: String): Boolean = true

  def checkMethodName(testMethod: String): Boolean = true

  def findTestResultEvent(log: LogFile, result: TestResult): Option[SymptomEvent] = (result, ok_is_good) match {
    case (TestFail(showtime, duration, testMethod, testClass, exceptions), true) =>
        if (findTestResultEvent(showtime, duration, testMethod, testClass, exceptions))
          Some(TestFail(showtime, duration, testMethod, testClass, exceptions)) else None
    case (TestOK(showtime, duration), false) => Some(TestOK(showtime, duration))
    case _ => None
  }

  def findSymptom(log: LogFile, result: TestResult): Option[List[SymptomEvent]] =
    findTestResultEvent(log, result) map { List(_) }

  override def findSymptom(log: Log): Option[List[SymptomEvent]] = log match {
    case UnitTestLog(logFile, testResult) => findSymptom(logFile, testResult)
  }

  // decline distributed workloads
  override def findResultEvent(log: Log): Option[SymptomEvent] = log match {
    case UnitTestLog(logFile, testResult) => findTestResultEvent(logFile, testResult)
  }
}

trait DistributedWorkload extends BugCase {
  override val isResultEventLogged = true  // true by default, but some cases might get false value

  def findDistributedResultEvent(logs: Array[LogFile]): Option[DistributedWorkloadLogEvent] =
    logs(targetNode).entries.find(checkLogEntry).map { DistributedWorkloadLogEvent(targetNode, _) }

  val targetNode: Int

  def findSymptom(logs: Array[LogFile]): Option[List[SymptomEvent]] =
    findDistributedResultEvent(logs) map { List(_) }

  // decline unit test workloads
  override final def findSymptom(log: Log): Option[List[SymptomEvent]] = log match {
    case DistributedWorkloadLog(logs) => findSymptom(logs)
  }

  // decline unit test workloads
  override final def findResultEvent(log: Log): Option[SymptomEvent] = log match {
    case DistributedWorkloadLog(logs) => findDistributedResultEvent(logs)
  }
}

object BugCase {
  private def createInstance[Bug <: BugCase](bug: Class[Bug]): Bug = bug.newInstance()

  lazy val cases: Map[String, BugCase] =
    ReflectionUtil.getClasses(this.getClass.getPackage.getName, classOf[BugCase]).asScala
    .flatten(c => TextParser.parseCaseDirName(c.getSimpleName) map { (_, createInstance(c)) }).toMap
}
