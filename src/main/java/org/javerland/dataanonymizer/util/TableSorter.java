/* Created on 19.09.2025 */
package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.TableMetadata;

import java.util.*;

/**
 * Utility class for table sorting operations.
 *
 * @author Juraj Pacolt
 */
public class TableSorter {

    public static List<TableMetadata> sortByDependencies(Map<String, TableMetadata> tables) {
        // Create dependency graph
        Map<String, Set<String>> deps = new HashMap<>();
        for (TableMetadata t : tables.values()) {
            deps.put(t.getName(), t.getReferencedTables());
        }

        // Calculate in-degrees
        Map<String, Integer> inDegree = new HashMap<>();
        for (String t : deps.keySet()) {
            inDegree.putIfAbsent(t, 0);
        }
        for (Set<String> refs : deps.values()) {
            for (String ref : refs) {
                inDegree.put(ref, inDegree.getOrDefault(ref, 0) + 1);
            }
        }

        // Queue with all nodes with in-degree 0
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) {
                queue.add(e.getKey());
            }
        }

        // Perform topological sort
        List<TableMetadata> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String t = queue.poll();
            sorted.add(tables.get(t));

            for (String ref : deps.getOrDefault(t, Collections.emptySet())) {
                int deg = inDegree.get(ref) - 1;
                inDegree.put(ref, deg);
                if (deg == 0) {
                    queue.add(ref);
                }
            }
        }

        // If not all tables are sorted, there is a cycle
        if (sorted.size() != tables.size()) {
            System.err.println("WARN: Cyclic dependency detected among tables!");
        }

        return sorted;
    }
}
