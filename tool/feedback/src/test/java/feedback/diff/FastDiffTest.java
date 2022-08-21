package feedback.diff;

import difflib.Delta;
import difflib.DiffUtils;
import feedback.ScalaUtil;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class FastDiffTest {
    private static final Random random = new Random(System.currentTimeMillis());

    private static ArrayList<Integer> generate(final int n, final int bound) {
        final ArrayList<Integer> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            result.add(random.nextInt(bound));
        }
        return result;
    }

    @RepeatedTest(10)
    void testRandomFastDiff() throws Exception {
        ScalaUtil.runTasks(0, 3, i_ -> {
            final int x = random.nextInt(10_000) + 1;
            final int y = random.nextInt(10_000) + 1;
            final int bound = random.nextInt(10) + 3;
            final ArrayList<Integer> good = generate(x, bound), bad = generate(y, bound);
            int expected = bad.size();
            for (final Delta<Integer> delta : DiffUtils.diff(good, bad).getDeltas()) {
                switch (delta.getType()) {
                    case CHANGE:
                    case INSERT:
                        expected -= delta.getRevised().getLines().size();
                    default:
                }
            }
            assertEquals(expected, new FastDiff<>(good.toArray(new Integer[0]), bad.toArray(new Integer[0])).common);
        });
    }
}
