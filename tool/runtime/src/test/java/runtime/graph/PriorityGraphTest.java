package runtime.graph;

import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class PriorityGraphTest {

    @Test
    void traverseGraph() throws IOException {
        final InputStream inputStream = Files.newInputStream(Paths.get("C:\\Users\\panji\\Desktop\\flaky-reproduction\\tool\\runtime\\src\\test\\resources\\hdfs-12070\\tree.json"));
        final JsonReader reader = Json.createReader(inputStream);
        final JsonObject json = reader.readObject();
        final PriorityGraph graph = new PriorityGraph(json);
        final Map<Integer, Integer> reachable = new TreeMap<>();
        graph.calculatePriorities(2, 0, reachable::put);
        //System.out.println(reachable.get(53701));
        //Sort by value
        ArrayList<Integer> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : reachable.entrySet()) {
            list.add(entry.getValue());
        }
        Collections.sort(list);
        LinkedHashMap<Integer, Integer> sortedMap = new LinkedHashMap<>();
        for (int num : list) {
            for (Map.Entry<Integer, Integer> entry : reachable.entrySet()) {
                if (entry.getValue().equals(num)) {
                    sortedMap.put(entry.getKey(), num);
                }
            }
        }
        System.out.println(sortedMap);
        List<Integer> order = new ArrayList<>();
        final Set<Integer> visited = new TreeSet<>();
        graph.findPath(2,2621,0,15,visited,order::add);
        System.out.println(order);
    }
}