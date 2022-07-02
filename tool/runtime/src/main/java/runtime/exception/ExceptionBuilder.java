package runtime.exception;

import java.lang.reflect.Constructor;

public class ExceptionBuilder {
    static private final String injectionMark = "flaky test exception injection of TraceAgent";
    static public Throwable createException(final String exceptionName) {
        try {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            final Class<?> exceptionClass = Class.forName(exceptionName, true, loader);
            final Constructor<?>[] cons = exceptionClass.getDeclaredConstructors();
            for (final Constructor<?> ctr : cons) {
                ctr.setAccessible(true);
            }
            try {
                return (Throwable) exceptionClass.getConstructor(String.class).newInstance(injectionMark);
            } catch (Exception ignored) { }
            try {
                return (Throwable) exceptionClass.getConstructor().newInstance();
            } catch (Exception ignored) { }
        } catch (final Exception ignored) { }
        return null;
    }
}
