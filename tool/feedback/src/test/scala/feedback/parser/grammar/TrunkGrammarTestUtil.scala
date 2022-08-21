package feedback.parser.grammar

import feedback.ScalaTestUtil._
import feedback.log.exception.{CallStack, NativeStackTraceElement, NormalStackTrace}
import feedback.parser.{ExceptionParser, TextParser}

import scala.collection.mutable

object TrunkGrammarTestUtil {
  val LOG_MESSAGE: Trunk = MsgTrunk(1, 2)
  val HEAD_EXCEPTION_WITHOUT_MESSAGE: Trunk = HeaderExceptionTrunk(1, "a", None)
  val HEAD_EXCEPTION_WITH_MESSAGE: Trunk = HeaderExceptionTrunk(1, "a", Some("a"))
  val NESTED_EXCEPTION_WITHOUT_MESSAGE: Trunk = NestedExceptionTrunk(1, "a", None)
  val NESTED_EXCEPTION_WITH_MESSAGE: Trunk = NestedExceptionTrunk(1, "a", Some("a"))
  val STACK_TRACE: Trunk =
    StackTraceTrunk(1, 2, NormalStackTrace(new CallStack(Array(NativeStackTraceElement("a", "a")))))

  def testLength(trunks:Array[Trunk], expectedBegin: Int, expectedEnd: Int, expectedLength: Int): Unit = {
    TrunkGrammar.calculate(trunks) match {
      case ((begin, end), exceptions) =>
        assertEquals(expectedBegin, begin)
        assertEquals(expectedEnd, end)
        assertEquals(expectedLength, exceptions.length)
    }
  }

  final class ContentTestHelper(val msgLength: Int, val exceptions: Array[(Int, Int, Int)], val text: Array[String]) {
    def test(): Unit = {
      val trunks = ExceptionParser.getNormalTrunks(text)

      def foldLines(begin: Int, end: Int): String = {
        require(0 <= begin && begin <= end && end < text.length)
        text.slice(begin, end).foldLeft(new mutable.StringBuilder) {
          case (builder, line) => builder append line
        }.toString
      }

      def check(t: ((Int, Int, Int), (Int, Int)),
                actualBegin2: Int,
                exception: String,
                msg: Option[String],
                header: String): Unit =
        t match {
          case ((begin, msgEnd, exceptionEnd), (actualTrunkBegin, actualTrunkEnd)) =>
            require(trunks(actualTrunkBegin).begin == actualBegin2)
            require(0 <= actualTrunkBegin)
            require(actualTrunkBegin + 1 < actualTrunkEnd)
            require(actualTrunkEnd <= trunks.length)
            assertEquals(begin, trunks(actualTrunkBegin).begin)
            assertEquals(msgEnd, trunks(actualTrunkEnd - 2).end)
            assertEquals(msgEnd, trunks(actualTrunkEnd - 1).begin)
            assertEquals(exceptionEnd, trunks(actualTrunkEnd - 1).end)
            msg match {
              case Some(msg) =>
                val position = header.indexOf(':')
                require(0 < position && position < header.length - 1)
                assertEquals(header.substring(0, position), exception)
                assertEquals(header.substring(position + 2, header.length), msg)
              case None =>
                require(header.last == '\n')
                assertEquals(header.substring(0, header.length - 1), exception)
            }
        }

      val t = TrunkGrammar.calculate(trunks)
      (TrunkGrammar.calculate(text, trunks, t), t) match {
        case ((finalMsg, nestedException), ((begin, end), actualExceptions)) =>
          assertEquals(0, begin)
          assertEquals(msgLength, (begin until end).map(trunks(_).length).sum)
          assertEquals(exceptions.length, actualExceptions.length)
          if (actualExceptions.nonEmpty) {
            assertEquals(msgLength, trunks(actualExceptions(0)._1).begin)
            assertEquals(text.length, trunks.last.end)
            finalMsg match {
              case Some(msg) =>
                assertEquals(foldLines(0, msgLength), msg)
              case None =>
                require(msgLength == 0)
            }
            trunks(actualExceptions(0)._1) match {
              case HeaderExceptionTrunk(begin, exception, msg) =>
                check((exceptions(0), actualExceptions(0)), begin, exception, msg, text(begin))
            }
            assertEquals(exceptions.length, nestedException.get.exceptions.length)
            exceptions.zip(actualExceptions).zip(nestedException.get.exceptions).slice(1, exceptions.length) foreach {
              case (t, exceptionRecord) =>
                trunks(t._2._1) match {
                  case NestedExceptionTrunk(begin, exception, msg) =>
                    check(t, begin, exception, msg, ExceptionParser.parseNestedExceptionHeader(text(begin)).get)
                }
            }
          } else {
            assertTrue(nestedException.isEmpty)
          }
      }
    }
  }

  val contentCases: Array[ContentTestHelper] = Array(
    new ContentTestHelper(2, Array(
      (2, 8, 11)), Array(
      "asdfsad\n",
      "as\n",
      "asdf.Exception: asdf; stacktrace =\n",
      " at AssertionUtils.fail(AssertionUtils.java:55)\n",
      " at TestUtils.retryOnExceptionWithTimeout(Native Method)\n",
      " at TestUtils.lambda$waitForCondition$3(TestUtils.java:303)\n",
      " ... 5 more\n",
      "\n",
      " at AssertionUtils.fail(Native Method)\n",
      " at TestUtils.retryOnExceptionWithTimeout(AssertionUtils.java:12)\n",
      " at TestUtils.lambda$waitForCondition$3(TestUtils.java:233)")),
    new ContentTestHelper(1, Array(
      (1, 3, 5)), Array(
      "eRecordAfterFailover (got []) ==> expected: <true> but was: <false>\n",
      "org.opentest4j.AssertionFailedError: Condition\n",
      "asdfasdf\n",
      "  at org.junit.jupiter.api.AssertionUtils.<init>(AssertionUtils.java:55)\n",
      "  ... 2 more")),
    new ContentTestHelper(1, Array(
      (1, 2, 4)), Array(
      "eRecordAfterFailover (got []) ==> expected: <true> but was: <false>\n",
      "org.opentest4j.AssertionFailedError: Condition\n",
      "  at org.junit.jupiter.api.AssertionUtils.<clinit>(AssertionUtils.java:55)\n",
      "  ... 2 more")),
    new ContentTestHelper(4, Array(
      (4, 10, 13)), Array(
      "asdfsad\n",
      "ass\n",
      "qwerqwer\n",
      "qwerqwer\n",
      "asdf.Exception: asdf; stacktrace =\n",
      " at AssertionUtils.fail(AssertionUtils.java:55)\n",
      " at TestUtils.retryOnExceptionWithTimeout(Native Method)\n",
      " at TestUtils.lambda$waitForCondition$3(TestUtils.java:303)\n",
      " ... 5 more\n",
      "\n",
      " at AssertionUtils.fail(Native Method)\n",
      " at TestUtils.retryOnExceptionWithTimeout(AssertionUtils.java:12)\n",
      " at TestUtils.lambda$waitForCondition$3(TestUtils.java:233)")),
    new ContentTestHelper(0, Array(
      (0, 6, 9), (9, 10, 11)), Array(
      "asdf.Exception: asdf; stacktrace =\n",
      " at AssertionUtils.fail(AssertionUtils.java:55)\n",
      " at TestUtils.retryOnExceptionWithTimeout(Native Method)\n",
      " at TestUtils.lambda$waitForCondition$3(TestUtils.java:303)\n",
      " ... 5 more\n",
      "\n",
      " at AssertionUtils.fail(Native Method)\n",
      " at TestUtils.retryOnExceptionWithTimeout(AssertionUtils.java:12)\n",
      " at TestUtils.lambda$waitForCondition$3(TestUtils.java:233)\n",
      "Caused by: qwerException\n",
      " at A.asdf(wqe.s:12)")),
    new ContentTestHelper(0, Array(
      (0, 6, 9), (9, 10, 11)), Array(
      "asdf.Exception: asdf; stacktrace =\n",
      " at AssertionUtils.fail(AssertionUtils.java:55)\n",
      " at TestUtils.retryOnExceptionWithTimeout(Native Method)\n",
      " at TestUtils.lambda$waitForCondition$3(TestUtils.java:303)\n",
      " ... 5 more\n",
      "\n",
      " at AssertionUtils.fail(Native Method)\n",
      " at TestUtils.retryOnExceptionWithTimeout(AssertionUtils.java:12)\n",
      " at TestUtils.lambda$waitForCondition$3(TestUtils.java:233)\n",
      "Caused by: qwerException: asdf\n",
      " at A.asdf(wqe.s:12)")),
  )
}
