package feedback.parser;

import feedback.time.InjectionRequestRecord;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Log implements Serializable {
    public final String header;   // header might be null
    public final LogEntry[] entries;
    public final InjectionRequestRecord[] injections;

    Log(final String header, final LogEntry[] entries, final InjectionRequestRecord[] injections) {
        this.header = header;
        this.entries = entries;
        this.injections = injections;
    }

    public static Log load(final Path path) throws IOException {
        return Parser.parseLog(ParserUtil.getFileLines(path));
    }

    public static Log load(final File file) throws IOException {
        return load(file.toPath());
    }

    public static Log load(final String path) throws IOException {
        return load(Paths.get(path));
    }
}
