package feedback.time;

import org.junit.jupiter.api.RepeatedTest;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

final class TimelineTest {
    private static final Random random = new Random(System.currentTimeMillis());

    @RepeatedTest(10)
    void testRandomCount() {
        final Boolean[] timeline = new Boolean[random.nextInt(1000) + 10];
        for (int i = 0; i < timeline.length; i++) {
            timeline[i] = random.nextBoolean();
        }
        final Timeline.UpdateAgent<Boolean, Integer> agent =
                new Timeline.UpdateAgent<>(timeline, b -> b);
        for (int i_ = random.nextInt(1000) + 10; i_ > 0; i_--) {
            int x, y;
            do {
                x = random.nextInt(timeline.length);
                y = random.nextInt(timeline.length);
            } while (x > y);
            final int expected = (int) IntStream.range(x, y + 1).filter(i -> timeline[i]).count();
            if (x > 0) {
                assertEquals(expected, agent.forwardDistance(x - 1, y));
            }
            if (y + 1 < timeline.length) {
                assertEquals(expected, agent.backwardDistance(y + 1, x));
            }
        };
    }

    private static void testTimeline(final Integer[] timeline,
                                     final Function<Integer, Integer> forwardWeight,
                                     final Function<Integer, Integer> backwardWeight) {
        final Map<Integer, BestTargetLogForInjection> actual = new TreeMap<>();
        final Timeline.UpdateAgent<Integer, Integer> update =
                new Timeline.UpdateAgent<Integer, Integer>(timeline, v -> v == -1 || v == 0) {
                    @Override
                    int forwardDistance(int index, int target) {
                        return forwardWeight.apply(super.forwardDistance(index, target));
                    }

                    @Override
                    int backwardDistance(int index, int target) {
                        return backwardWeight.apply(super.backwardDistance(index, target));
                    }

                    @Override
                    void update(final Integer id, final int target, final int weight) {
                        assertFalse(actual.containsKey(id));
                        actual.put(id, new BestTargetLogForInjection(target, weight));
                    }
                };
        Timeline.updateTimeline(timeline, v -> v == 0, v -> v > 0 ? v : null, update, -1);
        final Map<Integer, BestTargetLogForInjection> expected = new TreeMap<>();
        for (int i = 0; i < timeline.length; i++) {
            if (timeline[i] > 0) {
                int target = -1, weight = -1;
                int count = 0;
                // prefer forward
                for (int j = i + 1; j < timeline.length; j++) {
                    if (timeline[j] == -1 || timeline[j] == 0) {
                        count++;
                    }
                    if (timeline[j] == 0) {
                        int w = forwardWeight.apply(count);
                        if (target == -1 || w < weight) {
                            target = j;
                            weight = w;
                        }
                    }
                }
                count = 0;
                for (int j = i - 1; j > -1; j--) {
                    if (timeline[j] == -1 || timeline[j] == 0) {
                        count++;
                    }
                    if (timeline[j] == 0) {
                        int w = backwardWeight.apply(count);
                        if (target == -1 || w < weight) {
                            target = j;
                            weight = w;
                        }
                    }
                }
                final int id = timeline[i];
                assertFalse(expected.containsKey(id));
                assertNotEquals(-1, target);
                expected.put(id, new BestTargetLogForInjection(target, weight));
            }
        }
        assertEquals(expected, actual);
    }

    private static Integer[] generateRandomTimeline(final int log, final int critical, final int injection) {
        final List<Integer> timeline = IntStream.concat(IntStream.range(1, injection + 1),
                IntStream.concat(IntStream.generate(() -> 0).limit(critical),
                        IntStream.generate(() -> -1).limit(log))).boxed().collect(Collectors.toList());
        Collections.shuffle(timeline);
        return timeline.toArray(new Integer[0]);
    }

    @RepeatedTest(10)
    void testRandomTimeline() {
        final BiConsumer<Function<Integer, Integer>, Function<Integer, Integer>> testRandom = (f, b) -> {
            testTimeline(generateRandomTimeline(
                            random.nextInt(1000),
                            random.nextInt(50) + 1,
                            random.nextInt(500) + 1),
                    f, b);
        };
        final BiConsumer<Function<Integer, Integer>, Function<Integer, Integer>> testExtreme = (f, b) -> {
            for (int i = 0; i < 3; i++) {
                for (int j = 1; j < 4; j++) {
                    for (int k = 1; k < 5; k++) {
                        testTimeline(generateRandomTimeline(i, j, k), f, b);
                    }
                }
            }
        };
        final Function<Integer, Integer>[] distances = new Function[]{
                w -> w,
                randomConstant(100),
                randomConstant(1000),
                randomLinear(10, 10),
                randomLinear(100, 100),
                randomQuad(10, 10, 10),
                randomQuad(10, 10, 10),
        };
        Arrays.stream(distances).forEach(f -> Arrays.stream(distances).forEach(g -> {
            testRandom.accept(f, g);
            testExtreme.accept(f, g);
        }));
    }

    private static final class BestTargetLogForInjection {
        private final int target, weight;
        private BestTargetLogForInjection(final int target, final int weight) {
            this.target = target;
            this.weight = weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BestTargetLogForInjection)) return false;
            BestTargetLogForInjection that = (BestTargetLogForInjection) o;
            return target == that.target && weight == that.weight;
        }

        @Override
        public int hashCode() {
            return Objects.hash(target, weight);
        }

        @Override
        public String toString() {
            return "(" + target + "," + weight + ")";
        }
    }

    private static Function<Integer, Integer> randomConstant(final int a) {
        final int A = random.nextInt(a);
        return i -> A;
    }

    private static Function<Integer, Integer> randomLinear(final int a, final int b) {
        final int A = random.nextInt(a);
        final int B = random.nextInt(b);
        return i -> A * i + B;
    }

    private static Function<Integer, Integer> randomQuad(final int a, final int b, final int c) {
        final int A = random.nextInt(a);
        final int B = random.nextInt(b);
        final int C = random.nextInt(c);
        return i -> (A * i + B) * i + C;
    }
}
