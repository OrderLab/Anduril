package driver;

import feedback.common.Env;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

final class ProcessController {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);

    private final static long FILE_SIZE_LIMIT = 100_000_000;  // 100 MB
    private final static int granularity = 100;  // wait per 100 millisecond

    static int run(final List<String> cmd,
                    final int timeout,
                    final boolean killall,
                    final List<File> files) throws IOException, InterruptedException, ExecutionException {
        final ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmd);
        pb.redirectErrorStream(true);  // print the error output
        final Process process = pb.start();
        final Future<IOException> console = Env.submit(() -> {
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ( (line = in.readLine()) != null) {
                    LOG.debug(line);
                }
            } catch (final IOException e) {
                return e;
            }
            return null;
        });
        try {
            boolean finish = false;
            final long expectedEndTime;
            if (timeout < 0) {
                expectedEndTime = System.currentTimeMillis() + 10_000L;  // 10 seconds for feedback computation
            } else {
                expectedEndTime = System.currentTimeMillis() + timeout * 1_000L;
            }
            while (System.currentTimeMillis() < expectedEndTime) {
                Thread.sleep(granularity);
                if (!process.isAlive()) {
                    finish = true;
                    break;
                }
                long size = 0;
                for (final File file : files) {
                    if (file.exists()) {
                        if (file.isDirectory()) {
                            size += FileUtils.sizeOfDirectory(file);
                        } else {
                            size += FileUtils.sizeOf(file);
                        }
                    }
                }
                if (size > FILE_SIZE_LIMIT) {
                    throw new RuntimeException("exceed file size limit");
                }
            }
            if (!finish && timeout < 0) {
                throw new RuntimeException("process times out");
            }
            return process.exitValue();
        } finally {
            forceKill(process, killall);
            final IOException e = console.get();
            if (e != null) {
                LOG.error("console printing error", e);
            }
        }
    }

    static void forceKill(final Process process, final boolean killall) throws InterruptedException {
        if (killall) {
            boolean success = false;
            int backoff = 1_000;
            while (!success) {
                try {
                    final ProcessBuilder pb = new ProcessBuilder();
                    pb.command(Collections.singletonList("jps"));
                    final Process jps = pb.start();
                    jps.waitFor();
                    final List<Integer> ids = new ArrayList<>();
                    try (final BufferedReader in = new BufferedReader(new InputStreamReader(jps.getInputStream()))) {
                        String line;
                        while ((line = in.readLine()) != null) {
                            if (!line.isEmpty()) {
                                final int blank = line.indexOf(' ');
                                final int id = Integer.parseInt(line.substring(0, blank));
                                final String name = line.substring(blank + 1, line.length());
                                boolean found = false;
                                for (final String service : keyServices) {
                                    if (name.startsWith(service)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    ids.add(id);
                                }
                            }
                        }
                    }
                    for (final int id : ids) {
                        kill(id);
                    }
                    success = true;
                } catch (final Exception e) {
                    LOG.warn("retry due to error when killing processes", e);
                    Thread.sleep(backoff);
                    backoff *= 2;
                }
            }
        }
        process.destroyForcibly();
        int backoff = 1_000;
        while (process.isAlive()) {
            process.destroyForcibly();
            Thread.sleep(backoff);
            backoff *= 2;
        }
    }

    static void kill(final int pid) throws InterruptedException {
        if (pid == Env.pid()) {
            return;
        }
        boolean success = false;
        int backoff = 1_000;
        while (!success) {
            try {
                final ProcessBuilder pb = new ProcessBuilder();
                pb.command(Arrays.asList("kill", "-9", String.valueOf(pid)));
                final Process process = pb.start();
                process.waitFor();
                success = true;
            } catch (final Exception e) {
                LOG.warn("retry due to error when killing process {}", pid, e);
                Thread.sleep(backoff);
                backoff *= 2;
            }
        }
    }

    private static final String[] keyServices = {
            "Launcher",
            "Jps",
            "jps",
            "RemoteMavenServer",
            "NailgunRunner",
            "Main",
            "GradleDaemon",
    };
}
