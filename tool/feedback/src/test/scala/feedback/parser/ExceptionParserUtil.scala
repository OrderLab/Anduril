package feedback.parser

import feedback.ScalaTestUtil.{assertEquals, assertTrue}

private[parser] object ExceptionParserUtil {
  def runAllTests(): Unit = {
    Parser.parseLogException("sleep interrupted\njava.lang.InterruptedException: sleep interrupted\n\tat java.lang.Thread.sleep(Native Method)\n\tat org.apache.hadoop.hbase.util.Threads.sleep(Threads.java:148)") match {
      case (msg, exceptions) =>
        assertEquals(msg, "sleep interrupted")
        assertTrue(Array(new NestedException("java.lang.InterruptedException", "sleep interrupted", Array("java.lang.Thread.sleep(Native Method)", "org.apache.hadoop.hbase.util.Threads.sleep(Threads.java:148)"))) sameElements exceptions)
    }
  }
}
