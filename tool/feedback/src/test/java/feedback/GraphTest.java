package feedback;

import feedback.common.ThreadTestBase;
import org.junit.jupiter.api.RepeatedTest;
import runtime.graph.PriorityGraph;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GraphTest extends ThreadTestBase {
    private static final Random random = new Random(System.currentTimeMillis());
    static final int INF = 1_000_000_000;

    private static final class Edge {
        private final int x, y;
        private Edge(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final class NodeValue {
        private final int node;
        private final int weight;
        private NodeValue(final int node, final int w) {
            this.node = node;
            this.weight = w;
        }
    }

    private static final class PriorityGraphHelper extends PriorityGraph {
        private final int n;  // the number of all nodes
        private final int[] w;  // weight of each node
        private final int injectionNumber;
        private final int[] injections;  // weight of each injection
        private final PriorityQueue<NodeValue> q = new PriorityQueue<>(Comparator.comparingInt(nv -> nv.weight));

        private PriorityGraphHelper() {
            super(null, random.nextInt(100) + 10);
            // caller2callee2injections
            // outcome2cause
            this.n = super.startNumber + random.nextInt(10000) + 10;
            this.w = new int[this.n];
            this.injectionNumber = random.nextInt(this.n / 2) + 10;
            this.injections = new int[this.injectionNumber];

            final int e = this.n * 2 + random.nextInt(this.n * 8);
            final Edge[] edges = new Edge[e];
            for (int i = 0; i < e; i++) {
                int x, y;
                do {
                    x = random.nextInt(this.n);
                    y = random.nextInt(this.n);
                } while (x == y || y >= super.startNumber);
                final ArrayList<Integer> nodes;
                if (super.outcome2cause.containsKey(x)) {
                    nodes = super.outcome2cause.get(x);
                } else {
                    nodes = new ArrayList<>();
                    super.outcome2cause.put(x, nodes);
                }
                nodes.add(y);
                edges[i] = new Edge(x, y);
            }
            for (int i = 0; i < super.startNumber; i++) {
                final int weight = -random.nextInt(this.n);
                super.setStartValue(i, weight);
                q.add(new NodeValue(i, weight));
            }
            dijkstra();
            for (int injectionId = 0; injectionId < this.injectionNumber; injectionId++) {
                final Edge edge = edges[random.nextInt(e)];
                final int caller = edge.x;
                final int callee = edge.y;
                final Map<Integer, ArrayList<Integer>> map;
                if (super.caller2callee2injections.containsKey(caller)) {
                    map = super.caller2callee2injections.get(caller);
                } else {
                    map = new TreeMap<>();
                    super.caller2callee2injections.put(caller, map);
                }
                if (!map.containsKey(callee)) {
                    map.put(callee, new ArrayList<>());
                }
                map.get(callee).add(injectionId);
                this.injections[injectionId] = this.w[caller];
            }
        }

        private void dijkstra() {
            Arrays.fill(this.w, INF);
            while (!this.q.isEmpty()) {
                final NodeValue nv = this.q.poll();
                if (w[nv.node] != INF) {
                    continue;
                }
                w[nv.node] = nv.weight;
                if (super.outcome2cause.containsKey(nv.node)) {
                    for (final int child : super.outcome2cause.get(nv.node)) {
                        if (nv.weight + 1 < this.w[child]) {
                            this.q.add(new NodeValue(child, nv.weight + 1));
                        }
                    }
                }
            }
        }

        private void test() {
            final Set<Integer> collected = new TreeSet<>();
            final ArrayList<Integer> actual = new ArrayList<>();
            final int acceptNumber = Math.min(random.nextInt(n) + 10, this.injectionNumber);
            super.calculatePriorities(injectionId -> {
                if (collected.add(injectionId)) {
                    actual.add(injectionId);
                }
                return collected.size() >= acceptNumber;
            });
            final int[] expected = this.injections.clone();
            Arrays.sort(expected);
            for (int i = 0; i < actual.size(); i++) {
                assertEquals(expected[i], this.injections[actual.get(i)]);
                // difference (caller vs injection/callee) of two weights is 1
                assertEquals(expected[i] + 1, super.w.get(actual.get(i)));
            }
            if (actual.size() < acceptNumber) {
                assertEquals(INF, expected[actual.size()]);
            }
        }
    }

    @RepeatedTest(10)
    void testRandomPriorityGraph() {
        new PriorityGraphHelper().test();
    }
}
