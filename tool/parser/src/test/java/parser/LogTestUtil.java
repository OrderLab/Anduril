package parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
}
