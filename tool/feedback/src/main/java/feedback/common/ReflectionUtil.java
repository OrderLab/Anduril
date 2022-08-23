package feedback.common;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.Set;

public final class ReflectionUtil {
    public static <T> Set<Class<? extends T>> getClasses(final String prefix, final Class<T> type) {
        return new Reflections(prefix, Scanners.SubTypes.filterResultsBy(c -> true)).getSubTypesOf(type);
    }
}
