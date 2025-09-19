/* Created on 19.09.2025 */
package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.TableMetadata;

import java.util.concurrent.Callable;

/**
 * Callable class for processing a table.
 *
 * @author Juraj Pacolt
 */
public class TableProcessor {

    private TableMetadata table;

    public TableProcessor(TableMetadata table) {
        this.table = table;
    }

    public void process() {
        System.out.println("Processing table: " + table.getName());
        // Implement the actual processing logic here
    }

}
