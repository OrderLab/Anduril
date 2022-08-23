package feedback;

public final class ScalaTestUtil {
    public static void assertRequireFail(final Runnable task) {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, task::run);
    }

    public static void assertEquals(final Object expected, final Object actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }

    public static void assertTrue(final boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }

    public static <T> void assertArrayEquals(final T[] expected, final T[] actual) {
        org.junit.jupiter.api.Assertions.assertArrayEquals(expected, actual);
    }
}
