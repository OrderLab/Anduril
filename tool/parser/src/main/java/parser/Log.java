package parser;

import java.io.Serializable;

public final class Log implements Serializable {
    public final String header;   // header might be null
    public final LogEntry[] entries;

    public Log(final String header, final LogEntry[] entries) {
        this.header = header;
        this.entries = entries;
    }
}
