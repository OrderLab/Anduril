package feedback.parser;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LogTestUtil {
    public static String[] getFileLines(final String path) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                LogTestUtil.class.getClassLoader().getResourceAsStream(path), StandardCharsets.UTF_8));
        final List<String> text = new ArrayList<>();
        while (reader.ready()) {
            text.add(reader.readLine());
        }
        return text.toArray(new String[0]);
    }

    public static Log getLog(String path) throws IOException {
        return Parser.parseLog(getFileLines(path));
    }

    public static void initTempFile(final String resource, final Path tempFilePath) throws IOException {
        final File tempFile = tempFilePath.toFile();
        tempFile.getParentFile().mkdirs();
        FileUtils.copyURLToFile(LogTestUtil.class.getClassLoader().getResource(resource), tempFile);
    }
}
