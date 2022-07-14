package parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ParserCommandTest {
    @Test
    void testLoadFile() throws IOException {
        final String[] zookeeper_3157 = LogTestUtil.getFileLines("ground-truth/zookeeper-3157/bad-run-log.txt");
        assertEquals(1170, zookeeper_3157.length);
        assertEquals("JUnit version 4.12", zookeeper_3157[0]);
    }
}
