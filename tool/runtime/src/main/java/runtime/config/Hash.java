package runtime.config;

public final class Hash {
    private final static long A = 13, P = 1_000_000_007;

    public static long addStackTrace(long v, final String cls, final String method, final int line) {
        v = (A * v + cls.hashCode()) % P;
        v = (A * v + method.hashCode()) % P;
        return (A * v + line + 1) % P;
    }

    public static String getStackTrace(final StackTraceElement[] stackTrace) {
        final StringBuilder buffer = new StringBuilder("[");
        for (int i = stackTrace.length - 1; i > -1; i--) {
            if (stackTrace[i].getClassName().startsWith("runtime.")) {
                break;
            }
            buffer.append("(");
            buffer.append(stackTrace[i].getClassName());
            buffer.append(",");
            buffer.append(stackTrace[i].getMethodName());
            buffer.append(",");
            buffer.append(stackTrace[i].getLineNumber());
            buffer.append("),");
        }
        buffer.append("]");
        return buffer.toString();
    }
}
