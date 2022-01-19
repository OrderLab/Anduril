package analyzer.analysis;

import soot.SootClass;

import java.util.HashMap;

public final class SubTypingAnalysis {
    private static final SubTypingAnalysis instance = new SubTypingAnalysis();
    public static SubTypingAnalysis v() {
        return instance;
    }

    private final HashMap<SootClass, Integer> memoir = new HashMap<>();

    private static final String[] names = {
            "java.lang.Thread",
            "java.lang.Runnable",
            "java.lang.Throwable",
    };
    private static final int[] flags = {
            1<<0,
            1<<0,
            1<<1,
    };

    private int dfsWithMemoir(final SootClass sootClass) {
        if (memoir.containsKey(sootClass))
            return memoir.get(sootClass);
        int result = 0;
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(sootClass.getName()))
                result |= flags[i];
        }
        for (final SootClass c : sootClass.getInterfaces()) {
            result |= dfsWithMemoir(c);
        }
        try {
            // superclass may be null and throwing RuntimeException
            result |= dfsWithMemoir(sootClass.getSuperclass());
        } catch (final RuntimeException ignored) {}
        memoir.put(sootClass, result);
        return result;
    }

    public boolean isThrowable(final SootClass sootClass) {
        return (dfsWithMemoir(sootClass) & flags[2]) != 0;
    }

    public boolean isThreadOrRunnable(final SootClass sootClass) {
        return (dfsWithMemoir(sootClass) & (flags[0] | flags[1])) != 0;
    }

    public boolean isSubtype(final SootClass exception, final SootClass baseException) {
        if (exception == baseException)
            return true;
        if (!isThrowable(exception))
            return false;
        for (final SootClass i : exception.getInterfaces()) {
            if (isSubtype(i, baseException))
                return true;
        }
        try {
            if (isSubtype(exception.getSuperclass(), baseException))
                return true;
        } catch (final RuntimeException ignored) {}
        return false;
    }
}
