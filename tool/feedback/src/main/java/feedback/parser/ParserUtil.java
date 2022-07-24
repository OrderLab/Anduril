package feedback.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ParserUtil {
    public static String[] getFileLines(final Path path) throws IOException {
        final List<String> lines = new ArrayList<>();
        try (final Stream<String> stream = Files.lines(path)) {
            stream.forEach(lines::add);
        }
        return lines.toArray(new String[0]);
    }

    public static String[] getFileLines(final String path) throws IOException {
        return getFileLines(Paths.get(path));
    }
}
