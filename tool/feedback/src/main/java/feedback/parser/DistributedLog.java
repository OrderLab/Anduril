package feedback.parser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

public final class DistributedLog implements Serializable {
    public final File[] dirs;
    public final Log[] logs;
    public final boolean distributed;

    public DistributedLog(final File rootDir) throws IOException {
        this.distributed = rootDir.isDirectory();
        if (this.distributed) {
            this.dirs = rootDir.listFiles(file -> file.isDirectory() && file.getName().startsWith("logs-"));
            Arrays.sort(this.dirs, Comparator.comparing(File::getName));
            this.logs = new Log[this.dirs.length];
            for (int i = 0; i < this.dirs.length; i++) {
                final File[] files = this.dirs[i].listFiles((file, name) -> name.endsWith(".log"));
                if (files.length != 1) {
                    throw new IOException("multiple log files for a single process");
                }
                this.logs[i] = Log.load(files[0]);
            }
        } else {
            this.dirs = new File[]{rootDir};
            this.logs = new Log[]{Log.load(rootDir)};
        }
    }

    public DistributedLog(final String path) throws IOException {
        this(new File(path));
    }

    public DistributedLog(final Path path) throws IOException {
        this(path.toFile());
    }
}
