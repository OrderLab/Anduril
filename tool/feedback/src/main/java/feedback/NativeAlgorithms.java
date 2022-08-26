package feedback;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public final class NativeAlgorithms {
	private static final Logger LOG = LoggerFactory.getLogger(NativeAlgorithms.class);

	private native int load();

	private native int[] diff(final int[] good, final int goodLength, final int[] bad, final int badLength);

	private final static NativeAlgorithms instance;

	/**
	 * Source: <a href="https://github.com/gkubisa/jni-maven">jni-maven</a>
	 * Loads a native shared library. It tries the standard System.loadLibrary
	 * method first and if it fails, it looks for the library in the current
	 * class path. It will handle libraries packed within jar files, too.
	 *
	 * @param name name of the library to load
	 * @throws IOException if the library cannot be extracted from a jar file
	 * into a temporary file
	 */
	static {
		try {
			final String name = "feedback";
			try {
				System.loadLibrary(name);
			} catch (final UnsatisfiedLinkError e) {
				final String filename = System.mapLibraryName(name);
				final int pos = filename.lastIndexOf('.');
				final File file = File.createTempFile(filename.substring(0, pos), filename.substring(pos));
				file.deleteOnExit();
				//Hard-coded here or library can not be found
				try (final InputStream in = NativeAlgorithms.class.getClassLoader().getResourceAsStream(filename.substring(0, pos) + ".so")) {
					if (in == null) {
						throw new RuntimeException("can't find native library");
					}
					try (final OutputStream out = Files.newOutputStream(file.toPath())) {
						IOUtils.copy(in, out);
					}
				}
				System.load(file.getAbsolutePath());
			}
		} catch (final Throwable e) {
			LOG.error("error when loading native library", e);
			System.exit(-1);
		}
		instance = new NativeAlgorithms();
		if (instance.load() != 1) {
			LOG.error("error when loading CPP implementation");
			System.exit(-1);
		}
	}

	public static int[] diff(final int[] good, final int[] bad) {
		final int[] result = instance.diff(good, good.length, bad, bad.length);
		if (result == null) {
			throw new RuntimeException("JVM out of memory error during JNI call");
		}
		return result;
	}
}
