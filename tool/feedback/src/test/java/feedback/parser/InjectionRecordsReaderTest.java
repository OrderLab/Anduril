package feedback.parser;

import feedback.common.ThreadTestBase;
import feedback.log.LogTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;


final class InjectionRecordsReaderTest extends ThreadTestBase {

    Path prepareTempFilesInDistributedCase(final String dirPrefix, final String csvPrefix,
                                           final int num, final Path tempDir) throws IOException {
        final Path CSVDir = tempDir.resolve("distributedCSVDir");
        for (int i = 0; i < num; i++) {
            String file = csvPrefix + i + ".csv";
            LogTestUtil.initTempFile(dirPrefix+"/"+file, CSVDir.resolve(file));
        }
        return CSVDir;
    }

    @Test
    void testDistributedCase(final @TempDir Path tempDir) throws Exception {
        Path dir = prepareTempFilesInDistributedCase("injectionTraceCSVcheck/cassandra-6415",
                "InjectionTimeRecord-",3,tempDir);
        InjectionTrace res = InjectionRecordsReader.readRecordCSVs(dir.toFile());
    }

}
