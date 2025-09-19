/* Created on 19.09.2025 */
package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.TableMetadata;

import java.util.*;

/**
 * Utility class for batching tables based on their dependencies.
 *
 * @author Juraj Pacolt
 */
public class TableBatcher {

    public static List<List<TableMetadata>> splitIntoBatches(Map<String, TableMetadata> tables, int batchSize) {
        // 1. creat dependency graph
        Map<String, Set<String>> deps = new LinkedHashMap<>();
        Map<String, Integer> inDegree = new LinkedHashMap<>();

        for (TableMetadata t : tables.values()) {
            deps.put(t.getName(), new LinkedHashSet<>(t.getReferencedTables()));
            inDegree.putIfAbsent(t.getName(), 0);
        }

        for (Set<String> refs : deps.values()) {
            for (String ref : refs) {
                inDegree.put(ref, inDegree.getOrDefault(ref, 0) + 1);
            }
        }

        // 2. queue for nodes with in-degree 0
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) {
                queue.add(e.getKey());
            }
        }

        List<List<TableMetadata>> batches = new ArrayList<>();

        // 3. process layers
        while (!queue.isEmpty()) {
            List<String> currentLayer = new ArrayList<>(queue);
            queue.clear();

            // split current layer into batches
            for (int i = 0; i < currentLayer.size(); i += batchSize) {
                int end = Math.min(i + batchSize, currentLayer.size());
                List<String> subList = currentLayer.subList(i, end);

                List<TableMetadata> batch = new ArrayList<>();
                for (String t : subList) {
                    batch.add(tables.get(t));
                }
                batches.add(batch);
            }

            // update in-degrees and queue
            for (String t : currentLayer) {
                for (String ref : deps.getOrDefault(t, Collections.emptySet())) {
                    int deg = inDegree.get(ref) - 1;
                    inDegree.put(ref, deg);
                    if (deg == 0) {
                        queue.add(ref);
                    }
                }
            }
        }

        return batches;
    }
}
