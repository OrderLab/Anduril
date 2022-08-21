package feedback.parser

import feedback.ScalaTestUtil._

object ExceptionParserTestUtil {
  def runAllTests(): Unit = {
    assertArrayEquals(Array("a\n", "b\n", "\n", "d"), TextParser.unfold("a\nb\n\nd"))
    assertArrayEquals(Array("a\n", "b\n", "\n", "d\n", "\n"), TextParser.unfold("a\nb\n\nd\n\n"))
  }
}
