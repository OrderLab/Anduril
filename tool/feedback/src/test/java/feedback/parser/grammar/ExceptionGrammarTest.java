package feedback.parser.grammar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ExceptionGrammarTest {
    @Test
    void testMore() {
        final String trace = " at org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:55)\n\tat org.junit.jupiter.api.AssertTrue.assertTrue(AssertTrue.java:40)\n\t  ... 3 more";

        assertEquals(trace.length(), ExceptionGrammar.parseStackTrace(trace).get()._2());

        assertTrue(ExceptionGrammar.parseStackTrace(trace + trace).isEmpty());

        assertTrue(ExceptionGrammar.parseStackTrace(trace + "-" + trace).isEmpty());
        assertTrue(ExceptionGrammar.parseStackTrace(trace + "\t" + trace).isEmpty());
        assertTrue(ExceptionGrammar.parseStackTrace(trace + "\n" + trace).nonEmpty());

        final String trace2 = trace + "\n\n" + trace;
        final int len2 = ExceptionGrammar.parseStackTrace(trace2).get()._2();
        assertEquals(trace.length() + "\n\n".length(), len2);
        assertEquals(trace.length(), ExceptionGrammar.parseStackTrace(trace2.substring(len2)).get()._2());
    }

    @Test
    void testNormal() {
        final String trace = " at org.junit.jupiter.api.AssertionUtils.fail(Native Method)\n\tat org.junit.jupiter.api.AssertTrue.assertTrue(AssertTrue.java:40)\n\t    at org.apache.kafka.test.TestUtils.retryOnExceptionWithTimeout(TestUtils.java:351)";

        assertEquals(trace.length(), ExceptionGrammar.parseStackTrace(trace).get()._2());

        assertTrue(ExceptionGrammar.parseStackTrace(trace + "-" + trace).isEmpty());
        assertTrue(ExceptionGrammar.parseStackTrace(trace + "\t" + trace).isEmpty());

        assertTrue(ExceptionGrammar.parseStackTrace(trace + trace).isEmpty());

        assertEquals(trace.length() * 2 + 1, ExceptionGrammar.parseStackTrace(trace + "\n" + trace).get()._2());

        final String trace2 = trace + "\n\n\n" + trace;
        final int len2 = ExceptionGrammar.parseStackTrace(trace2).get()._2();
        assertEquals(trace.length() + "\n\n\n".length(), len2);
        assertEquals(trace.length(), ExceptionGrammar.parseStackTrace(trace2.substring(len2)).get()._2());
    }

    @Test
    void testJUnit5More() {
        final String trace = " org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:55)\n\t org.junit.jupiter.api.AssertTrue.assertTrue(Native Method)\n\t  [...]";

        assertEquals(trace.length(), ExceptionGrammar.parseStackTraceJUnit5(trace).get()._2());

        assertTrue(ExceptionGrammar.parseStackTraceJUnit5(trace + trace).isEmpty());

        assertTrue(ExceptionGrammar.parseStackTraceJUnit5(trace + "-" + trace).isEmpty());
        assertTrue(ExceptionGrammar.parseStackTraceJUnit5(trace + "\t" + trace).isEmpty());
        assertTrue(ExceptionGrammar.parseStackTraceJUnit5(trace + "\n" + trace).nonEmpty());

        final String trace2 = trace + "\n\n" + trace;
        final int len2 = ExceptionGrammar.parseStackTraceJUnit5(trace2).get()._2();
        assertEquals(trace.length() + "\n\n".length(), len2);
        assertEquals(trace.length(), ExceptionGrammar.parseStackTraceJUnit5(trace2.substring(len2)).get()._2());
    }

    @Test
    void testJUnit5Normal() {
        final String trace = " org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:55)\n\t org.junit.jupiter.api.AssertTrue.assertTrue(AssertTrue.java:40)\n\t  \torg.apache.kafka.streams.integration.utils.IntegrationTestUtils.waitUntilFinalKeyValueRecordsReceived(IntegrationTestUtils.java:744)";

        assertEquals(trace.length(), ExceptionGrammar.parseStackTraceJUnit5(trace).get()._2());

        assertTrue(ExceptionGrammar.parseStackTraceJUnit5(trace + trace).isEmpty());

        assertTrue(ExceptionGrammar.parseStackTraceJUnit5(trace + "-" + trace).isEmpty());
        assertTrue(ExceptionGrammar.parseStackTraceJUnit5(trace + "\t" + trace).isEmpty());

        assertEquals(trace.length() * 2 + 1, ExceptionGrammar.parseStackTraceJUnit5(trace + "\n" + trace).get()._2());

        final String trace2 = trace + "\n\n\n" + trace;
        final int len2 = ExceptionGrammar.parseStackTraceJUnit5(trace2).get()._2();
        assertEquals(trace.length() + "\n\n\n".length(), len2);
        assertEquals(trace.length(), ExceptionGrammar.parseStackTraceJUnit5(trace2.substring(len2)).get()._2());
    }

    @Test
    void testExceptionWithMsg() {
        scala.Option<scala.Tuple2<String, String>> result =
                ExceptionGrammar.parseExceptionWithMsg("org.opentest4j.AssertionFailedError: Condition not met within timeout 60000. Did not receive all [KeyValue(1, A), KeyValue(1, B)] records from topic outputEmitOnChangeIntegrationTestshouldEmitSameRecordAfterFailover (got []) ==> expected: <true> but was: <false>");
        assertEquals("org.opentest4j.AssertionFailedError",
                result.get()._1);
        assertEquals("Condition not met within timeout 60000. Did not receive all [KeyValue(1, A), KeyValue(1, B)] records from topic outputEmitOnChangeIntegrationTestshouldEmitSameRecordAfterFailover (got []) ==> expected: <true> but was: <false>",
                result.get()._2);

        assertEquals("asdf", ExceptionGrammar.parseExceptionWithMsg("aException: asdf").get()._2);
        assertEquals("bError", ExceptionGrammar.parseExceptionWithMsg("bError: asdf").get()._1);
        assertTrue(ExceptionGrammar.parseExceptionWithMsg("cError: zxcv").nonEmpty());
        assertTrue(ExceptionGrammar.parseExceptionWithMsg("dError: zxcv").nonEmpty());
        assertTrue(ExceptionGrammar.parseExceptionWithMsg("eExceptionbException: zxcv").nonEmpty());
        assertTrue(ExceptionGrammar.parseExceptionWithMsg("eExceptionbException: ").isEmpty());
        assertTrue(ExceptionGrammar.parseExceptionWithMsg("eExceptionbException: \n").nonEmpty());
    }

    @Test
    void testExceptionWithoutMsg() {
        assertEquals("org.opentest4j.AssertionFailedError",
                ExceptionGrammar.parseExceptionWithoutMsg("org.opentest4j.AssertionFailedError\n").get());
        assertTrue(ExceptionGrammar.parseExceptionWithoutMsg("org.opentest4j.AssertionFailedErro\n").isEmpty());
        assertTrue(ExceptionGrammar.parseExceptionWithoutMsg("org.opentest4j.AssertionFailedError").isEmpty());
        assertTrue(ExceptionGrammar.parseExceptionWithoutMsg("org.open.AssertionFailedError: s\n").isEmpty());
    }
}
