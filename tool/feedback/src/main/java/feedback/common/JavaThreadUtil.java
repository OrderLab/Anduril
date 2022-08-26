package feedback.common;

import java.util.Arrays;
import java.util.concurrent.Future;

public final class JavaThreadUtil {
    public static <T> Future<Void> parallel(final T[] items, final ActionMayThrow<T> action) {
        return Env.parallel(Arrays.stream(items).iterator(), action);
    }
}
