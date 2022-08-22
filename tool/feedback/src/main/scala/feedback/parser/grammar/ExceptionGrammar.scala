package feedback.parser.grammar

import fastparse._
import NoWhitespace._
import feedback.log.exception._
import feedback.parser.{ExceptionParser, TextParser}

import scala.collection.mutable

// Docs of the APIs used: https://com-lihaoyi.github.io/fastparse/
// [_: P] triggers warning: Top-level wildcard is not allowed and will error under -Xsource:3
object ExceptionGrammar {

  private def number[_: P]: P[Int] = CharIn("0-9").rep(1).!.map {_.toInt}

  private def className[_: P]: P[String] = CharIn("a-z", "A-Z", "0-9", "_", "$", ".").rep(1).!

  private def exceptionMethodName[_: P]: P[String] = (className ~ ("<init>".! | "<clinit>".!).?) map {
    case (name, Some(suffix)) => s"$name$suffix"
    case (name, None) => name
  }

  // only a sanity check for an exception
  private def exceptionName[_: P]: P[Unit] =
    StringIn("Exception", "Error", "Throwable") | ( CharIn("a-z", "A-Z", "0-9", "_", "$", ".") ~ exceptionName )

  // only a sanity check for an exception
  private def exceptionNameWithSuffix[_: P]: P[Unit] =
    StringIn("Exception: ", "Error: ", "Throwable: ") |
      (CharIn("a-z", "A-Z", "0-9", "_", "$", ".") ~ exceptionNameWithSuffix)

  private def fileName[_: P]: P[String] =
    ( CharIn("a-z", "A-Z", "0-9", "_").rep(1).! ~ "." ~ CharIn("a-z", "A-Z", "0-9").rep(1).! ) map {
      case (filename, extension) => s"$filename.$extension"
    }

  private def more[_: P]: P[Int] = CharIn(" ", "\t").rep(1) ~ "... " ~ number ~ " more"

  private def prefix_JUnit5[_: P]: P[Boolean] =
    ("\n" ~ CharIn(" ", "\t").rep(1) ~ "[...]".!).? map { _.nonEmpty }

  private def stackTraceElement[_: P]: P[StackTraceRecord] =
    ( CharIn(" ", "\t").rep(1) ~ "at " ~ (exceptionMethodName map ExceptionParser.parseClassMethod) ~ "(" ~ (
      ( P( "Native Method" ) map {_ => None} )
        | ( ( fileName ~ ":" ~ number ) map { Some(_) } )
    ) ~ ")" )map {
      case (classname, method, Some((filename, line))) =>
        NormalStackTraceElement(classname, method, filename, line)
      case (classname, method, None) =>
        NativeStackTraceElement(classname, method)
    }

  private def stackTraceElement_JUnit5[_: P]: P[JUnit5StackTraceRecord] =
    (CharIn(" ", "\t").rep(1) ~ (exceptionMethodName map ExceptionParser.parseClassMethod) ~ "(" ~ (
      (P("Native Method") map { _ => None })
        | ((fileName ~ ":" ~ number) map {
        Some(_)
      })
      ) ~ ")") map {
      case (classname, method, Some((filename, line))) =>
        JUnit5NormalStackTraceElement(classname, method, filename, line)
      case (classname, method, None) =>
        JUnit5NativeStackTraceElement(classname, method)
    }

  private def stackTraceElements[_: P]: P[mutable.ArrayBuffer[StackTraceRecord]] =
    ( ( stackTraceElement ~ "\n" ~ stackTraceElements ) map {
      case (record, records) => records += record
    } ) | ( stackTraceElement map { mutable.ArrayBuffer(_) } )

  private def stackTraceElements_JUnit5[_: P]: P[mutable.ArrayBuffer[JUnit5StackTraceRecord]] =
    ( ( stackTraceElement_JUnit5 ~ "\n" ~ stackTraceElements_JUnit5 ) map {
      case (record, records) => records += record
    } ) | ( stackTraceElement_JUnit5 map { mutable.ArrayBuffer(_) } )

  // "\n".rep(2) implies that if there are more than 2 '\n's, they MUST be consumed by the parser
  private def stackTrace[_: P]: P[StackTrace] =
    (stackTraceElements ~ ("\n" ~ more).? ~ ("\n".rep(1) | End)) map {
      case (records, Some(more)) => MoreStackTrace(new CallStack(records.reverse.toArray), more)
      case (records, None) => NormalStackTrace(new CallStack(records.reverse.toArray))
    }

  private def stackTrace_JUnit5[_: P]: P[JUnit5StackTrace] =
    (stackTraceElements_JUnit5 ~ prefix_JUnit5 ~ ("\n".rep(1) | End)) map {
      case (records, true) => JUnit5MoreStackTrace(new CallStack(records.reverse.toArray))
      case (records, false) => JUnit5NormalStackTrace(new CallStack(records.reverse.toArray))
    }

  def parseStackTrace(text: Iterator[String]): Option[(StackTrace, Integer)] =
    fastparse.parse(text.iterator, stackTrace(_)) match {
      case Parsed.Success(result, position) => Some(result, position)
      case Parsed.Failure(_, _, _) => None
    }

  def parseStackTrace(text: String): Option[(StackTrace, Integer)] = parseStackTrace(Array(text).iterator)

  def parseStackTraceJUnit5(text: Iterator[String]): Option[(JUnit5StackTrace, Integer)] =
    fastparse.parse(text.iterator, stackTrace_JUnit5(_)) match {
      case Parsed.Success(result, position) => Some(result, position)
      case Parsed.Failure(_, _, _) => None
    }

  private def exceptionWithMsg[_: P]: P[(String, String)] = className ~ ": " ~ AnyChar.rep(1).! ~ End

  def parseExceptionWithMsg(text: String): Option[(String, String)] = {
    fastparse.parse(text, exceptionNameWithSuffix(_)) match {
      case Parsed.Success(_, _) =>
        fastparse.parse(text, exceptionWithMsg(_)) match {
          case Parsed.Success(result, _) => Some(result)
          case Parsed.Failure(_, _, _) => None
        }
      case Parsed.Failure(_, _, _) => None
    }
  }

  // must have the ending '\n'
  private def exceptionWithoutMsg[_: P]: P[Unit] = exceptionName ~ "\n" ~ End

  def parseExceptionWithoutMsg(text: String): Option[String] = {
    fastparse.parse(text, exceptionWithoutMsg(_)) match {
      case Parsed.Success(_, _) => Some(text.substring(0, text.length - 1))
      case Parsed.Failure(_, _, _) => None
    }
  }

  def parseStackTraceJUnit5(text: String): Option[(JUnit5StackTrace, Integer)] =
    parseStackTraceJUnit5(Array(text).iterator)
}
