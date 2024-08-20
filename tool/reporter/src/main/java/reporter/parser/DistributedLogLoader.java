package reporter.parser;

import feedback.JsonUtil;
import feedback.parser.LogParser;
import feedback.log.Log;

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

    public Log getDistributedLog(int index) throws IOException {
        if (this.distributed) {
            return LogParser.parseLog(rootDir.getPath() + "/" + index);
        }
        //return LogParser.parseLog(rootDir.getPath() + "/output-" + index + ".txt");
        return LogParser.parseLog(rootDir.getPath() + "/" + index + ".out");
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
