package runtime.time;

import java.util.ArrayList;
import java.util.Collections;

public final class PriorityCalculator {
    public static long compute(final int location, final int time) {
        return ((long) location) * time;
    }

    // TODO: improve to O(n)
    public static long kth(final ArrayList<Long> arr, final int k) {
        Collections.sort(arr);
        return arr.get(k - 1);
    }
}
