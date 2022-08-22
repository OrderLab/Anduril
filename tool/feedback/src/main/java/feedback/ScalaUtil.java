package feedback;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public final class ScalaUtil {
    public static <T> Set<Class<? extends T>> getClasses(final String prefix, final Class<T> type) {
        return new Reflections(prefix, Scanners.SubTypes.filterResultsBy(c -> true)).getSubTypesOf(type);
    }

    public static <T> Future<T> submit(final Callable<T> task) {
        return ThreadUtil.submit(task);
    }

    public static Future<Boolean> submit(final Runnable task) {
        return submit(() -> {
            task.run();
            return true;
        });
    }

    public static <T> void runTasks(final Iterator<T> item, final Consumer<T> action) throws Exception {
        final List<Future<Boolean>> tasks = new LinkedList<>();
        while (item.hasNext()) {
            final T current = item.next();
            tasks.add(submit(() -> action.accept(current)));
        }
        for (final Future<Boolean> task : tasks) {
            task.get();
        }
    }

    public static <T> void runTasks(final Iterable<T> items, final Consumer<T> action) throws Exception {
        runTasks(items.iterator(), action);
    }

    public static <T> void runTasks(final T[] items, final Consumer<T> action) throws Exception {
        runTasks(Arrays.stream(items).iterator(), action);
    }

    public static void runTasks(final int begin, final int end, final Consumer<Integer> action) throws Exception {
        runTasks(IntStream.range(begin, end).iterator(), action);
    }
}
