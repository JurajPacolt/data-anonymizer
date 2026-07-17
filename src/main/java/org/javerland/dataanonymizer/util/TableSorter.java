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
        TableBatcher.ExecutionPlan plan = TableBatcher.createExecutionPlan(tables, Math.max(1, tables.size()));
        if (!plan.unresolvedTables().isEmpty()) {
            System.err.println("WARN: Cyclic or unresolved dependencies detected among tables: "
                    + plan.unresolvedTables().stream().map(TableMetadata::getKey).toList());
        }
        return plan.layers().stream().flatMap(List::stream).flatMap(List::stream).toList();
    }
}
