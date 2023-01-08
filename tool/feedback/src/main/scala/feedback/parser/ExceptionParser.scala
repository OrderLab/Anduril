package feedback.parser

import feedback.log.exception.{NestedException, StackTrace}
import feedback.parser.grammar._

import scala.collection.mutable

object ExceptionParser {
  private val classnameRegex = raw"[a-zA-Z_0-9\.$$]+"
  private val methodNameRegex = raw"[a-zA-Z_0-9$$]+|<init>|<clinit>"

  private val LocationPattern = raw"($classnameRegex)\.($methodNameRegex)".r

  def parseClassMethod(text: String): (String, String) = text match {
    case LocationPattern(className, methodName) => (className, methodName)
  }

  private val Junit4TestFailurePattern1 = raw"(?s)(\d+)\)[ \t]*($methodNameRegex)\(($classnameRegex)\)\n".r
  private val Junit4TestFailurePattern2 = raw"(?s)(\d+)\)[ \t]*($classnameRegex)\n".r

  // TODO: remove the limitation of only accepting one line of log before parsing an exception
  // TODO: accept names like "Exception"?

  private val NestedExceptionHeaderPattern = raw"(?s)[ \t]*Caused by: ([^\n]+\n)".r

  private def parseExceptionHeader(text: String): Option[(String, Option[String])] =
    ExceptionGrammar.parseExceptionWithoutMsg(text) match {
      case Some(exception) => Some(exception, None)
      case _ => ExceptionGrammar.parseExceptionWithMsg(text) match {
        case Some((exception, msg)) => Some(exception, Some(msg))
        case _ => None
      }
    }

  def parseNestedExceptionHeader(text: String): Option[String] = text match {
    case NestedExceptionHeaderPattern(header) => Some(header)
    case _ => None
  }

  // each line (including the last one) HAS AND ONLY HAS ONE '\n', which should be EXACTLY at the end of the line
  def getTrunks[StackTraceType <: StackTrace](parse: Iterator[String] => Option[(StackTraceType, Integer)],
                                              text: Array[String]): Array[Trunk] = {
    if (text.nonEmpty) {
      require(text forall { _.nonEmpty })
    }
    var endIndex = text.length
    var totalLength = 0
    var currentStackTrace: Option[StackTrace] = None
    val trunks = mutable.ArrayBuffer.empty[Trunk]
    text.indices.reverse foreach { index =>
      val line = text(index)
      totalLength += line.length
      parse(LazyList.range(index, endIndex).map(text(_)).iterator) match {
        case Some((stacktrace, position)) =>
          currentStackTrace = Some(stacktrace)
          require(position <= totalLength)
          if (position < totalLength) {
            // some remaining lines are beyond the current stack trace
            // find the range of the current stack trace [index, trunkIndex)
            var trunkLength = 0
            var trunkIndex = index
            while (trunkLength < position) {
              trunkLength += text(trunkIndex).length
              trunkIndex += 1
            }
            require(trunkIndex < endIndex)
            trunks += MsgTrunk(trunkIndex, endIndex)
            endIndex = trunkIndex
            totalLength = position
          }
        case None =>
          currentStackTrace foreach { stacktrace =>
            trunks += StackTraceTrunk(index + 1, endIndex, stacktrace)
            endIndex = index + 1
            totalLength = line.length
          }
          currentStackTrace = None
          (parseNestedExceptionHeader(line) match {
            case Some(header) => (true, parseExceptionHeader(header))
            case None => (false, parseExceptionHeader(line))
          }) match {
            case (nested, Some((exception, msg))) =>
              if (index + 1 < endIndex) {
                trunks += MsgTrunk(index + 1, endIndex)
              }
              trunks += {
                if (nested) NestedExceptionTrunk(index, exception, msg) else HeaderExceptionTrunk(index, exception, msg)
              }
              endIndex = index
              totalLength = 0
            case _ =>
          }
      }
    }
    // do not need to handle the current stack here, because it can't construct a valid exception now
    if (0 < endIndex) {
      trunks += MsgTrunk(0, endIndex)
    }
    trunks.reverse.toArray
  }

  def parseJUnit5ResultTrunks(text: Array[String]): Array[Trunk] =
    getTrunks(ExceptionGrammar.parseStackTraceJUnit5, text)

  // text ==> ("...\n", "...\n", ...)
  private def parseJUnit5ResultNestedException(text: Array[String]): (Option[String], Option[NestedException]) =
    TrunkGrammar.calculate(text, parseJUnit5ResultTrunks(text))

  private def parseJUnit5ResultNestedException(text: String): (Option[String], Option[NestedException]) =
    parseJUnit5ResultNestedException(TextParser.unfoldWithNewLine(text))

  def getNormalTrunks(text: Array[String]): Array[Trunk] =
    getTrunks(ExceptionGrammar.parseStackTrace, text)

  // text ==> ("...\n", "...\n", ...)
  private def parseNormalNestedException(text: Array[String]): (Option[String], Option[NestedException]) =
    TrunkGrammar.calculate(text, getNormalTrunks(text))

  def parseNormalNestedException(text: String): (Option[String], Option[NestedException]) =
    parseNormalNestedException(TextParser.unfoldWithNewLine(text))

  def parseJunit4TestFailures(text: Array[String]): (String, String, NestedException) = {
    val failures = text.zipWithIndex flatMap {
      case (line, index) => line match {
        case Junit4TestFailurePattern1(_, testMethod, testClass) =>
          Some(testMethod, testClass, index)
        case Junit4TestFailurePattern2(_, testClass) =>
          Some("", testClass, index)
        case _ => None
      }
    }
    require(failures.nonEmpty)
    // TODO: check more than 1 failures
    val (testMethod, testClass, index) = failures(0)
    require(index == 0)
    val exception = text.slice(1, text.length)
    (testMethod, testClass, parseNormalNestedException(exception) match {
      case (None, Some(nestedException)) => nestedException
      case (_, None) => // for HBase
        parseNormalNestedException(exception :+ "\tat Foo.bar(Baz.java:9)") match {
          case (None, Some(nestedException)) => nestedException
        }
    })
  }

  def parseJunit4TestFailures(text: String): (String, String, NestedException) =
    parseJunit4TestFailures(TextParser.unfoldWithNewLine(text))

  // TODO: accept more than 1 exception
  private val Junit5FailurePattern =
    raw"(?s).*Failures? \(1\):\n[^\n]*:($classnameRegex):($methodNameRegex)\n[ \t]*MethodSource +\[className = '($classnameRegex)', *methodName = '($methodNameRegex)', *[^\n]*\]\n[ \t]*=> (.*)".r

  def parseJUnit5Failure(text: String, duration: Int): TestResultParser.FAIL = text match {
    case Junit5FailurePattern(classname, methodName, completeClassname, completeMethodName, exception) =>
      TestResultParser.FAIL(duration, completeMethodName, completeClassname,
        parseJUnit5ResultNestedException(exception) match {
          case (None, Some(nestedException)) => nestedException
          case (_, None) => // for Kafka-12508
            parseJUnit5ResultNestedException(s"$exception\n       Foo.bar(Baz.java:9)") match {
              case (None, Some(nestedException)) => nestedException
            }
        })
  }
}
