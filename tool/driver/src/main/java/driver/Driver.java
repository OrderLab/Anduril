package driver;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.config.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;

public final class Driver {
    private static final Logger LOG = LoggerFactory.getLogger(Driver.class);

    public static final int driverPid;

    static {
        final String name = ManagementFactory.getRuntimeMXBean().getName();
        driverPid = Integer.parseInt(name.substring(0, name.indexOf('@')));
    }

    private static void run(final int trialId, final Spec spec, final Properties properties) throws Exception {
        final List<String> cmd = new ArrayList<>();
        cmd.add("bash");
        cmd.add(spec.currentDir + "/single-trial.sh");
        final String injectionFile = spec.experimentPath.getPath() + "/injection-" + trialId + ".json";
        if (spec.distributed) {
            cmd.add(String.valueOf(trialId));
            cmd.add(String.valueOf(spec.processNumber));
            cmd.add(spec.experimentPath.getPath());
            cmd.add(spec.specPath.getPath());
            cmd.add(injectionFile);
        } else {
            cmd.add(String.valueOf(trialId));
            cmd.add(spec.experimentPath.getPath());
            cmd.add(spec.specPath.getPath());
            cmd.add(injectionFile);
            cmd.add("_");
        }
        for (final String name: properties.stringPropertyNames()) {
            cmd.add("-D" + name + "=" + properties.getProperty(name));
        }
        final int timeout = Config.getTimeout(properties);
        final List<File> files = new ArrayList<>();
        if (spec.distributed) {
            for (int i = 0; i < spec.processNumber; i++) {
                files.add(new File(spec.currentDir + "/cluster/logs-" + i));
            }
        } else {
            files.add(new File(spec.currentDir + "/trial.out"));
        }
        if (timeout == -1) {
            ProcessController.run(cmd, spec.distributed, files);
        } else {
            ProcessController.run(cmd, timeout + 10, spec.distributed, files);   // preserve 10s
        }
        final List<String> feedback =
                Arrays.asList("java", "-jar", spec.currentDir + "/feedback.jar", "--location-feedback",
                        "-g", spec.currentDir + "/good-run-log", "-b", spec.currentDir + "/bad-run-log",
                        "-t", spec.currentDir + "/trial.out", "-s", spec.specPath.getPath(), "-a", injectionFile);
        final int code = ProcessController.run(feedback, 10, false, new LinkedList<>());
        if (code != 0) {
            throw new Exception("feedback process error return code " + code);
        }
    }

    public static void main(final String[] args) throws Exception {
        LOG.info("Running driver with process id {}", driverPid);
        try {
            final Spec spec = new Spec(args);
            final Properties properties = new Properties();
            try (final FileInputStream input = new FileInputStream(spec.configPath)) {
                properties.load(input);
            }
            if (spec.baseline) {
                Config.checkBaselineConfig(properties);
            } else {
                Config.checkExperimentConfig(properties);
            }
            int trialId = spec.start;
            int backoff = 1_000;
            while (trialId < spec.end) {
                try {
                    // 1) run the script
                    // 2) set timeout for the script
                    // 3) monitor the file size
                    // 4) kill the script and JVM processes if any
                    // 5) run feedback
                    // 6) monitor the feedback result (by process exit code)
                    run(trialId, spec, properties);
                    final File src = new File(spec.currentDir + "/trial.out");
                    final File dst = new File(spec.experimentPath.getPath() + "/" + trialId + ".out");
                    if (src.isDirectory()) {
                        FileUtils.moveDirectory(src, dst);
                    } else {
                        FileUtils.moveFile(src, dst);
                    }
                    trialId++;
                    backoff = 1_000;
                    LOG.info("finish trial {}", trialId);
                } catch (final Exception e) {
                    LOG.warn("retry trial {} due to error", trialId, e);
                    backoff *= 2;
                    final String injectionFile = spec.experimentPath.getPath() + "/injection-" + trialId + ".json";
                    try {
                        FileUtils.delete(new File(injectionFile));
                    } catch (final IOException ex) {
                        LOG.error("not able to delete {}", injectionFile, ex);
                    }
                }
                Thread.sleep(backoff);
            }
        } finally {
            ProcessController.shutdown();
            LOG.info("Driver exits...");
        }
    }
}
