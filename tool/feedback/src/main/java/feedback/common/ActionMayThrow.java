package feedback.common;

import java.util.function.Consumer;

// Do not define with Scala so that we can preserve the Java exception checking
public interface ActionMayThrow<T> extends Consumer<T> {
    @Override
    default void accept(final T t) {
        try {
            acceptMayThrow(t);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    void acceptMayThrow(final T t) throws Exception;
}
