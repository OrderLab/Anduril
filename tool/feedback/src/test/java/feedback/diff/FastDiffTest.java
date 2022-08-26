package feedback.diff;

import difflib.Delta;
import difflib.DiffUtils;
import feedback.NativeAlgorithms;
import feedback.common.ThreadTestBase;
import feedback.common.Env;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class FastDiffTest extends ThreadTestBase {
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
        Env.parallel(0, 3, i_ -> {
            final int x = random.nextInt(10_000) + 1;
            final int y = random.nextInt(10_000) + 1;
            final int bound = random.nextInt(10) + 3;
            final ArrayList<Integer> good = generate(x, bound), bad = generate(y, bound);
            final Future<Integer> std = Env.submit(() -> {
                int expected = bad.size();
                for (final Delta<Integer> delta : DiffUtils.diff(good, bad).getDeltas()) {
                    switch (delta.getType()) {
                        case CHANGE:
                        case INSERT:
                            expected -= delta.getRevised().getLines().size();
                        default:
                    }
                }
                return expected;
            });
            final Future<FastDiff<Integer>> fast = Env.submit(() ->
                    new FastDiff<>(good.toArray(new Integer[0]), bad.toArray(new Integer[0])));
            final Future<int[]> cpp = Env.submit(() ->
                    NativeAlgorithms.diff(good.stream().mapToInt(Integer::intValue).toArray(),
                            bad.stream().mapToInt(Integer::intValue).toArray()));
            assertEquals(std.get(), fast.get().common);
            final FastDiff.CHOICE[] expected = fast.get().path;
            assertEquals(good.size() + bad.size() - fast.get().common, expected.length);
            final int[] actual = cpp.get();
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals(expected[i].id, actual[i]);
            }
        }).get();
    }
}
