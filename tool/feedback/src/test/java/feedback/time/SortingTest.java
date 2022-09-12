package feedback.time;

import org.junit.jupiter.api.RepeatedTest;
import runtime.time.TimeFeedbackManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortingTest {
    private static final Random random = new Random(System.currentTimeMillis());

    @RepeatedTest(50)
    void testKth() {
        final int n = random.nextInt(100_000) + 1;
        final int INF = 1_000_000;
        final ArrayList<Integer> a = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            a.add(random.nextInt(INF));
        }
        assertEquals(INF, TimeFeedbackManager.kth(a, n + random.nextInt(10), INF));
        final int k = random.nextInt(n);
        final int actual = TimeFeedbackManager.kth(a, k, INF);
        Collections.sort(a);
        assertEquals(a.get(k), actual);
    }
}
