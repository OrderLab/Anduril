package reporter.parser;

import feedback.JsonUtil;
import feedback.parser.DistributedLog;

import javax.json.JsonObject;
import java.io.File;
import java.io.IOException;

public class DistributedLogLoader {
    public final File rootDir;
    public final boolean distributed;

    public DistributedLogLoader(final String rootDir, final boolean distributed) throws IOException {
        this.rootDir = new File(rootDir);
        this.distributed = distributed;
    }

    public DistributedLog getDistributedLog(int index) throws IOException {
        if (this.distributed) {
            return new DistributedLog(rootDir.getPath() + "/" + index);
        }
        return new DistributedLog(rootDir.getPath() + "/output-" + index + ".txt");
    }

    public int getInjectionId(int index) throws IOException {
        final JsonObject injectionJson = JsonUtil.loadJson(rootDir.getPath() + "/injection-"
                + index + ".json");
        if (!injectionJson.containsKey("id")) {
            return -1;
        }
        return injectionJson.getInt("id");
    }
}
