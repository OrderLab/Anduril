package feedback.parser

import feedback.ScalaTestUtil._
import feedback.parser.grammar.ExceptionGrammar

object ExceptionParserTestUtil {
  def runAllTests(): Unit = {
    assertArrayEquals(Array("a\n", "b\n", "\n", "d"), TextParser.unfoldWithNewLine("a\nb\n\nd"))
    assertArrayEquals(Array("a\n", "b\n", "\n", "d\n", "\n"), TextParser.unfoldWithNewLine("a\nb\n\nd\n\n"))
    assertArrayEquals(Array("a", "b", "", "d"), TextParser.unfoldWithoutNewLine("a\nb\n\nd"))
    assertArrayEquals(Array("a", "b", "", "d", "", ""), TextParser.unfoldWithoutNewLine("a\nb\n\nd\n\n"))
    assertTrue(ExceptionGrammar.parseExceptionWithoutMsg("java.lang.ExceptionInInitializerError\n").isDefined)
    ExceptionParser.parseNormalNestedException(Array("java.lang.ExceptionInInitializerError\n",
      "        at org.junit.runner.JUnitCore.runMainAndExit(JUnitCore.java:47)\n",
      "        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n",
      "        at runtime.TraceAgent.main(TraceAgent.java:262)\n",
      "Caused by: java.lang.NumberFormatException: flaky test exception injection of TraceAgent\n",
      "        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)\n",
      "        at java.lang.reflect.Constructor.newInstance(Constructor.java:423)\n",
      "        at runtime.exception.ExceptionBuilder.createException(ExceptionBuilder.java:16)\n").mkString("", "", ""))
    match {
      case (None, Some(_)) =>
    }
  }
}
