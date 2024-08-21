package driver;

import feedback.common.ActionMayThrow;
import feedback.common.CallMayThrow;
import feedback.common.Env;
import feedback.common.RunMayThrow;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.config.Config;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;

public final class Driver {
    private static final Logger LOG = LoggerFactory.getLogger(Driver.class);

    private final static long FILE_SIZE_LIMIT = 100_000_000;  // 100 MB
    private final static int TRIAL_LIMIT = 1_000_000;

    public static void main(final String[] args) throws IOException, InterruptedException {
        LOG.info("Running driver with process id {}", Env.pid());
        Env.enter();
        try {
            final Spec spec = new Spec(args);
            final Properties properties = new Properties();
            try (final FileInputStream input = new FileInputStream(spec.configFile)) {
                properties.load(input);
            }
            if (spec.baseline) {
                Config.checkBaselineConfig(properties);
            } else {
                Config.checkExperimentConfig(properties);
            }
            for (int trialId = findStart(spec.experimentPath); trialId < spec.trial_limit; trialId++) {
                final File injectionFile = spec.experimentPath.resolve("injection-" + trialId + ".json").toFile();
                final Path outputDir = spec.currentDir.resolve("trial.out");
                final Path trialDir = spec.experimentPath.resolve(trialId + ".out");
                final List<String> trial = new ArrayList<>();
                trial.add("bash");
                trial.add(spec.currentDir + "/single-trial.sh");
                if (spec.distributed) {
                    trial.add(String.valueOf(trialId));
                    trial.add(String.valueOf(spec.processNumber));
                    trial.add(spec.experimentPath.toString());
                    trial.add(spec.specFile.getPath());
                    trial.add(injectionFile.getPath());
                } else {
	            if (!spec.baseline) {
                      trial.add(String.valueOf(trialId));
                      trial.add(spec.experimentPath.toString());
                      trial.add(spec.specFile.getPath());
                      trial.add(injectionFile.getPath());
                      trial.add("_");
		     } else {
                      trial.add(String.valueOf(trialId));
                      trial.add(spec.experimentPath.toString());
                      trial.add("_");
                      trial.add(injectionFile.getPath());
                      trial.add("_");
		     }
                }
                for (final String name: properties.stringPropertyNames()) {
                    trial.add("-D" + name + "=" + properties.getProperty(name));
                }
                final int timeout = Config.getTimeout(properties);

		List<String> feedback;
		if (!spec.baseline) {
                  feedback =
                        Arrays.asList("java", "-jar", spec.currentDir.resolve("feedback.jar").toString(),
                                "--location-feedback",
                                "-g", spec.currentDir.resolve("good-run-log").toString(),
                                "-b", spec.currentDir.resolve("bad-run-log").toString(),
                                "-t", outputDir.toString(),
                                "-s", spec.specFile.getPath(),
                                "-a", injectionFile.getPath());
		} else {
		  feedback = null;
		}
                loop(() -> {
                    final List<Future<Void>> consoles = new ArrayList<>();
                    try {
                        delete(injectionFile);
                        delete(trialDir.toFile());
                        final Process trialProcess = start(trial);
                        consoles.add(console(trialProcess, LOG::debug));
                        if (monitor(timeout, () -> {
                            long size = 0;
                            if (spec.distributed) {
                                for (int i = 0; i < spec.processNumber; i++) {
                                    size += fileSize(spec.currentDir.resolve("cluster").resolve("logs-" + i).toFile());
                                }
                            } else {
                                size += fileSize(outputDir.toFile());
                            }
                            if (size < FILE_SIZE_LIMIT && trialProcess.isAlive()) {
                                return true;
                            } else if (!trialProcess.isAlive()) {
                                return false;
                            }
                            throw new RuntimeException();
                        })) {
                            killall();
                        }
                        if (spec.distributed) {
                            for (int i = 0; i < spec.processNumber; i++) {
                                move(spec.currentDir.resolve("cluster").resolve("logs-" + i).toFile(),
                                        outputDir.resolve("logs-" + i).toFile());
                                // Remove gc.log in distributed cassandra case
                                delete(outputDir.resolve("logs-" + i).resolve("gc.log").toFile());
                            }
                            move(spec.currentDir.resolve("output.txt").toFile(),
                                    outputDir.resolve("output.txt").toFile());
                        }
                        // Keep the stack_trace file if there is any
                        File stacktrace = spec.currentDir.resolve("stack_trace.txt").toFile();
                        if (stacktrace.exists()) {
                            move(stacktrace, outputDir.resolve("stack_trace.txt").toFile());
                        }
                        Thread.sleep(3000);

			if (!spec.baseline) {
                          final Process feedbackProcess = start(feedback);
                          consoles.add(console(feedbackProcess, LOG::debug));
                          if (monitor(180, feedbackProcess::isAlive)) {
                              kill(feedbackProcess);
                              return false;
                          }
                          if (feedbackProcess.exitValue() != 0) {
                              throw new RuntimeException("feedback exit value = " + feedbackProcess.exitValue());
                          }
			}
                        move(outputDir.toFile(), trialDir.toFile());
                    } finally {
                        killall();
                        for (final Future<Void> console : consoles) {
                            console.get();
                        }
                    }
                    return true;
                }, () -> {
                    delete(injectionFile);
                    delete(trialDir.toFile());
                });
                LOG.info("finish trial {}", trialId);
            }
        } finally {
            Env.exit();
            LOG.info("Driver exits...");
        }
    }

    static int findStart(final Path experiment) {
        for (int id = 0; ; id++) {
            if (!experiment.resolve(id + ".out").toFile().exists()) {
                return id;
            }
        }
    }

    static void move(final File src, final File dst) throws IOException {
        if (!src.exists()) {
            throw new RuntimeException("file not exists: " + src);
        }
        dst.getParentFile().mkdirs();
        if (dst.exists()) {
            throw new RuntimeException("file already exists: " + dst);
        }
        if (src.isDirectory()) {
            FileUtils.moveDirectory(src, dst);
        } else {
            FileUtils.moveFile(src, dst);
        }
    }

    static void delete(final File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else {
                FileUtils.delete(file);
            }
        }
    }

    static long fileSize(final File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                return FileUtils.sizeOfDirectory(file);
            }
            return FileUtils.sizeOf(file);
        }
        return 0;
    }

    static Process start(final List<String> cmd) throws IOException {
        final ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmd);
        pb.redirectErrorStream(true);  // print the error output
        return pb.start();
    }

    static Future<Void> console(final Process process, final ActionMayThrow<String> action) {
        return Env.submit(() -> {
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    action.accept(line);
                }
            }
        });
    }

    private static final int granularity = 100;  // wait per 100 millisecond

    static boolean monitor(final CallMayThrow<Boolean> predicate)
            throws InterruptedException {
        try {
            while (predicate.call()) {
                Thread.sleep(granularity);
            }
            return false;
        } catch (final RuntimeException e) {
            LOG.warn("File size exceeds the limit or Trial is timed out!");
            Thread.sleep(granularity);
        } catch (final Exception e) {
            LOG.warn("error when monitoring", e);
            Thread.sleep(granularity);
        }
        return true;
    }

    static boolean monitor(final int timeout, final CallMayThrow<Boolean> predicate) throws InterruptedException {
        final long end = System.currentTimeMillis() + timeout * 1_000L;
        return monitor(() -> {
            if (System.currentTimeMillis() > end) {
                throw new RuntimeException("time out after " + timeout + " ms");
            }
            return predicate.call();
        });
    }

    static void loop(final CallMayThrow<Boolean> callable, final RunMayThrow exception) throws InterruptedException {
        for (long backoff = 1_000; ; backoff *= 2) {
            try {
                if (callable.call()) {
                    return;
                }
            } catch (final Exception e) {
                LOG.warn("error in loop", e);
                exception.run();
            }
            Thread.sleep(backoff);
        }
    }

    static void kill(final int pid) throws InterruptedException {
        loop(() -> {
            start(Arrays.asList("kill", "-9", String.valueOf(pid))).waitFor();
            return true;
        }, () -> { });
    }

    static void kill(final Process process) throws InterruptedException {
        loop(() -> {
            process.destroyForcibly();
            return !process.isAlive();
        }, () -> { });
    }

    static void killall() throws InterruptedException {
        loop(() -> {
            final Process jps = start(Collections.singletonList("jps"));
            final List<Integer> processes = new ArrayList<>();
            final Future<Void> print = console(jps, line -> {
                final String[] args = line.split(" ");
                final int pid = Integer.parseInt(args[0]);
                if (pid != Env.pid() && keyServices.stream().noneMatch(args[1]::startsWith)) {
                    processes.add(pid);
                }
            });
            jps.waitFor();
            print.get();
            for (final int pid : processes) {
                kill(pid);
            }
            return true;
        }, () -> { });
    }

    private static final List<String> keyServices = Arrays.asList(
            "Launcher",
            "Jps",
            "jps",
            "RemoteMavenServer",
            "NailgunRunner",
            "Main",
            "GradleDaemon"
    );
}
