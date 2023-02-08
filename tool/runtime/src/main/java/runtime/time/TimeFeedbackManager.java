package runtime.time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.FeedbackManager;
import runtime.TraceAgent;

import javax.json.JsonObject;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;

public class TimeFeedbackManager extends FeedbackManager {
    private static final Logger LOG = LoggerFactory.getLogger(TimeFeedbackManager.class);

    private static final double INF = 1e20;
    private static int fixLog = -1;
    private static boolean isTime = true;

    private enum Mode {
        // E[_+_]
        E_ADD((time, location) -> {
            double total = 0;
            int size = 0;
            for (final Map.Entry<Integer, Integer> entry : location.entrySet()) {
                total += time.get(entry.getKey()) + entry.getValue();
                size++;
            }
            if (size == 0) {
                return INF;
            }
            return total / size;
        }),
        // E[_*_]
        E_TIMES((time, location) -> {
            double total = 0;
            int size = 0;
            for (final Map.Entry<Integer, Integer> entry : location.entrySet()) {
                total += ((double) time.get(entry.getKey())) * entry.getValue();
                size++;
            }
            if (size == 0) {
                return INF;
            }
            return total / size;
        }),
        // min[_+_]
        MIN_ADD((time, location) -> {
            double min = 0;
            int size = 0;
            for (final Map.Entry<Integer, Integer> entry : location.entrySet()) {
                final double v = time.get(entry.getKey()) + entry.getValue();
                if (size == 0 || v < min) {
                    min = v;
                }
                size++;
            }
            if (size == 0) {
                return INF;
            }
            return min;
        }),
        // min[_*_]
        MIN_TIMES((time, location) -> {
            double min = 0;
            int size = 0;
            for (final Map.Entry<Integer, Integer> entry : location.entrySet()) {
                final double v = ((double) time.get(entry.getKey())) * entry.getValue();
                if (size == 0 || v < min) {
                    min = v;
                }
                size++;
            }
            if (size == 0) {
                return INF;
            }
            return min;
        }),
        // min[min(_,_)]
        MIN_MIN((time, location) -> {
            double min = 0;
            int size = 0;
            for (final Map.Entry<Integer, Integer> entry : time.entrySet()) {
                final Integer k = entry.getKey();
                final double v = entry.getValue();
                if (size == 0 || v < min) {
                    min = v;
                }

                if (location.get(k) < min) {
                    min = location.get(k);
                }
                size = size + 2;
            }
            /**
            for (final Map.Entry<Integer, Integer> entry : location.entrySet()) {
                final double v = entry.getValue();
                if (size == 0 || v < min) {
                    min = v;
                }
                size++;
            }
             **/
            if (size == 0) {
                return INF;
            }
            return min;
        }),
        // min[_ or _]
        MIN_INTERLEAVE((time, location) -> {
            double min = 0;
            int size = 0;
            if (isTime) {
                for (final Map.Entry<Integer, Integer> entry : time.entrySet()) {
                    final double v = entry.getValue();
                    if (size == 0 || v < min) {
                        min = v;
                    }
                    size++;
                }
            } else {
                for (final Map.Entry<Integer, Integer> entry : location.entrySet()) {
                    final double v = entry.getValue();
                    if (size == 0 || v < min) {
                        min = v;
                    }
                    size++;
                }
            }
            if (size == 0) {
                return INF;
            }
            return min;
        }),
        // min[_ or _]
        MIN_RANDOM((time, location) -> {
            final Integer v;
            if (isTime) {
                v = time.get(fixLog);
            } else {
                v = location.get(fixLog);
            }
            return v == null ? INF : v;
        });

        public final BiFunction<Map<Integer, Integer>, Map<Integer, Integer>, Double> formula;

        Mode(final BiFunction<Map<Integer, Integer>, Map<Integer, Integer>, Double> formula) {
            this.formula = formula;
        }
    }

    private final Mode mode;
    private double boundary;
    private final Map<Integer, double[]>[] nodes;
    private final Map<Integer, double[]> standalone = new TreeMap<>();

    protected final TimePriorityTable timePriorityTable;
    private final Random random = new Random(System.currentTimeMillis());

    public TimeFeedbackManager(final String specPath, final JsonObject json, final String timePriorityTable) {
        super(specPath, json);
        this.timePriorityTable = TimePriorityTable.load(timePriorityTable);
        switch (TraceAgent.config.timeFeedbackMode) {
            case "add"            : this.mode = Mode.E_ADD; break;
            case "times"          : this.mode = Mode.E_TIMES; break;
            case "min_add"        : this.mode = Mode.MIN_ADD; break;
            case "min_times"      : this.mode = Mode.MIN_TIMES; break;
            case "min_min"      : this.mode = Mode.MIN_MIN; break;
            case "min_interleave" : this.mode = Mode.MIN_INTERLEAVE; break;
            case "min_random"     : this.mode = Mode.MIN_RANDOM; break;
            default: throw new RuntimeException("invalid time feedback formula");
        }
        this.nodes = new Map[this.timePriorityTable.nodes];
        for (int i = 0; i < this.timePriorityTable.nodes; i++) {
            this.nodes[i] = new TreeMap<>();
        }
    }

    @Override
    public boolean isAllowed(final int pid, final int injectionId, final int occurrence) {
        final double[] priorities = nodes[pid].get(injectionId);
        if (priorities == null) {
            return false;
        }
        if (occurrence > priorities.length) {
            return false;
        }
        return priorities[occurrence - 1] <= boundary;
    }

    @Override
    public boolean isAllowed(final int injectionId, final int occurrence) {
        final double[] priorities = standalone.get(injectionId);
        if (priorities == null) {
            return false;
        }
        if (occurrence > priorities.length) {
            return false;
        }
        return priorities[occurrence - 1] <= boundary;
    }

    private static final Map<Integer, Integer> sentinel = new HashMap<>();

    @Override
    public void calc(final int windowSize) {
        final Map<Integer, Map<Integer, Integer>> locationPriorities = new TreeMap<>();
        System.out.printf("\nSys Graph whz Start Time:   %s\n",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
        final int[] change = new int[super.graph.startNumber];
        for (int i = 0; i < super.graph.startNumber; i++) {
            change[i] = super.active.getOrDefault(i, 0);
        }
        this.timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) -> v.locationPriorities.forEach((log, priority) ->
                locationPriorities.computeIfAbsent(injection, t -> new TreeMap<>()).put(log, priority + change[log]))));
        if (this.mode == Mode.MIN_INTERLEAVE) {
            isTime = random.nextBoolean();
        }
        System.out.printf("\nFlaky Agent Table Start Time: %s\n",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
        // WARN: time priority exists iff location priority exists according to the algorithm in Timeline.java
        if (this.mode == Mode.MIN_RANDOM) {
            final Set<Integer> timeSet = new TreeSet<>();
            //final Set<Integer> locationSet = new TreeSet<>();
            this.timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) -> {
                timeSet.addAll(v.timePriorities.keySet());
                //locationSet.addAll(v.timePriorities.keySet());
            }));
            if (timeSet.isEmpty()) {
                throw new RuntimeException("invalid table");
            }

            isTime = random.nextBoolean();
            fixLog = random.nextInt(timeSet.size());
            /**
            if (c < timeSet.size()) {
                isTime = true;
                fixLog = timeSet.toArray(new Integer[0])[c];
            } else {
                isTime = false;
                fixLog = locationSet.toArray(new Integer[0])[c - timeSet.size()];
            }
              **/
        }
        final ArrayList<Double> priorities = new ArrayList<>(
                this.timePriorityTable.boundaries.values().stream().reduce(0, Integer::sum));
        if (this.timePriorityTable.distributed) {
            this.timePriorityTable.boundaries.forEach((k, v) -> this.nodes[k.pid].put(k.injection, new double[v]));
            this.timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) -> {
                final double priority;
                if (k.occurrence > 3) {
                    priority = mode.formula.apply(v.timePriorities, locationPriorities.get(injection));
                } else {
                    priority = locationPriorities.get(injection).values().stream().mapToDouble(x->x).min().orElseGet(() -> INF);
                }
                this.nodes[k.pid].get(injection)[k.occurrence - 1] = priority;
                priorities.add(priority);
            }));
        } else {
            this.timePriorityTable.boundaries.forEach((k, v) -> this.standalone.put(k.injection, new double[v]));
            this.timePriorityTable.injections.forEach((injection, m) -> m.forEach((k, v) -> {
                System.out.printf("%d,%d,", injection, k.occurrence);
                for (int log = 0; log < super.graph.startNumber; log++) {
                    if (v.timePriorities.containsKey(log)) {
                        System.out.printf("(%d_%d_%d),", log,
                                locationPriorities.getOrDefault(injection, sentinel)
                                        .getOrDefault(log, -1),
                                v.timePriorities.get(log));
                    }
                }
                System.out.println();
                final double priority;
                if (k.occurrence > 3) {
                    priority = mode.formula.apply(v.timePriorities, locationPriorities.get(injection));
                } else {
                    priority = locationPriorities.get(injection).values().stream().mapToDouble(x->x).min().orElseGet(() -> INF);
                }
                this.standalone.get(injection)[k.occurrence - 1] = priority;
                priorities.add(priority);
            }));
        }
        this.boundary = kth(priorities, windowSize, INF);
        System.out.println("Using time feedback mode: " + getMode());
    }

    // reference: https://www.geeksforgeeks.org/quicksort-using-random-pivoting/
    static public <T extends Comparable<T>> T kth(final ArrayList<T> a, int k, final T INF) {
        if (a.size() <= k) {
            return INF;
        }
        final Random random = new Random(System.currentTimeMillis());
        int x = 0, y = a.size();
        while (x + 1 < y) {
            final T pivot = a.get(x + random.nextInt(y - x));
            int i = x, j = y - 1;
            while (true) {
                while (a.get(i).compareTo(pivot) < 0) i++;
                while (pivot.compareTo(a.get(j)) < 0) j--;
                if (i >= j) break;
                final T tmp = a.get(i);
                a.set(i, a.get(j));
                a.set(j, tmp);
                i++;
                j--;
            }
            j++;
            if (x + k < j) {
                y = j;
            } else {
                k -= j - x;
                x = j;
            }
        }
        if (k != 0) {
            throw new RuntimeException("invalid kth");
        }
        return a.get(x);
    }

    private String getMode() {
        switch (mode) {
            case E_ADD          : return "add";
            case E_TIMES        : return "times";
            case MIN_ADD        : return "min_add";
            case MIN_TIMES      : return "min_times";
            case MIN_MIN        : return "min_min";
            case MIN_INTERLEAVE : return isTime ? "min_time" : "min_location";
            case MIN_RANDOM     : return String.format("min_%s_log_%d", isTime ? "time" : "location", fixLog);
            default: throw new RuntimeException("invalid mode");
        }
    }

    public void printCSV(final PrintWriter csv) {
        csv.println(getMode());
        if (timePriorityTable.distributed) {
            csv.println("pid,id,occurrence,priority");
            for (final Map<Integer, double[]> node : nodes) {
                node.forEach((i, arr) -> {
                    for (int j = 0; j < arr.length; j++) {
                        csv.printf("%d,%d,%.4f\n", i, j + 1, arr[j]);
                    }
                });
            }
        } else {
            csv.println("id,occurrence,priority");
            standalone.forEach((i, arr) -> {
                for (int j = 0; j < arr.length; j++) {
                    csv.printf("%d,%d,%.4f\n", i, j + 1, arr[j]);
                }
            });
        }
    }
}
