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
        return createExecutionPlan(tables, batchSize).layers().stream().flatMap(List::stream).toList();
    }

    /**
     * Creates dependency layers. Parent tables are completed before their
     * dependent children. Batches inside one layer may run in parallel.
     * Unresolved cyclic tables are returned in a final layer instead of being
     * silently omitted.
     */
    public static ExecutionPlan createExecutionPlan(Map<String, TableMetadata> tables, int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("Batch size must be at least 1");
        }

        Map<String, TableMetadata> byKey = new LinkedHashMap<>();
        tables.values().forEach(table -> byKey.put(table.getKey(), table));

        Map<String, Set<String>> dependants = new LinkedHashMap<>();
        Map<String, Integer> inDegree = new LinkedHashMap<>();
        byKey.keySet().forEach(key -> {
            dependants.put(key, new LinkedHashSet<>());
            inDegree.put(key, 0);
        });

        for (TableMetadata child : byKey.values()) {
            for (String parentKey : child.getReferencedTables()) {
                if (byKey.containsKey(parentKey) && dependants.get(parentKey).add(child.getKey())) {
                    inDegree.compute(child.getKey(), (ignored, degree) -> degree + 1);
                }
            }
        }

        Queue<String> queue = new LinkedList<>();
        inDegree.forEach((key, degree) -> {
            if (degree == 0) {
                queue.add(key);
            }
        });

        List<List<List<TableMetadata>>> layers = new ArrayList<>();
        Set<String> processed = new LinkedHashSet<>();
        while (!queue.isEmpty()) {
            List<String> currentLayer = new ArrayList<>(queue);
            queue.clear();
            processed.addAll(currentLayer);
            layers.add(splitLayer(currentLayer.stream().map(byKey::get).toList(), batchSize));

            for (String parentKey : currentLayer) {
                for (String childKey : dependants.getOrDefault(parentKey, Collections.emptySet())) {
                    int degree = inDegree.compute(childKey, (ignored, current) -> current - 1);
                    if (degree == 0) {
                        queue.add(childKey);
                    }
                }
            }
        }

        List<TableMetadata> unresolved = byKey.entrySet().stream().filter(entry -> !processed.contains(entry.getKey()))
                .map(Map.Entry::getValue).toList();
        if (!unresolved.isEmpty()) {
            layers.add(splitLayer(unresolved, batchSize));
        }

        return new ExecutionPlan(layers, unresolved);
    }

    private static List<List<TableMetadata>> splitLayer(List<TableMetadata> layer, int batchSize) {
        List<List<TableMetadata>> batches = new ArrayList<>();
        for (int i = 0; i < layer.size(); i += batchSize) {
            batches.add(new ArrayList<>(layer.subList(i, Math.min(i + batchSize, layer.size()))));
        }
        return batches;
    }

    public record ExecutionPlan(List<List<List<TableMetadata>>> layers, List<TableMetadata> unresolvedTables) {
    }
}
