package feedback.log;

import feedback.JsonUtil;
import feedback.parser.LogFileParser;
import org.apache.commons.io.FileUtils;

import javax.json.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class LogTestUtil {
    private static final ClassLoader classloader = LogTestUtil.class.getClassLoader();

    public static String[] getFileLines(final String path) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                classloader.getResourceAsStream(path), StandardCharsets.UTF_8));
        final List<String> text = new ArrayList<>();
        while (reader.ready()) {
            text.add(reader.readLine());
        }
        return text.toArray(new String[0]);
    }

    public static String[] getDistinctFileLines(final String path) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                classloader.getResourceAsStream(path), StandardCharsets.UTF_8));
        final Set<String> text = new HashSet<>();
        while (reader.ready()) {
            text.add(reader.readLine());
        }
        return text.toArray(new String[0]);
    }

    public static JsonObject loadJson(final String path) throws IOException {
        return JsonUtil.loadJson(classloader.getResourceAsStream(path));
    }

    public static LogFile getLogFile(String path) throws IOException {
        return LogFileParser.parse(getFileLines(path))._1;
    }

    public static void initTempFile(final String resource, final Path tempFilePath) throws IOException {
        final File tempFile = tempFilePath.toFile();
        tempFile.getParentFile().mkdirs();
        FileUtils.copyURLToFile(classloader.getResource(resource), tempFile);
    }
}
