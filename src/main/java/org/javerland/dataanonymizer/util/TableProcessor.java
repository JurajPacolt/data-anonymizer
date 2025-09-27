/* Created on 19.09.2025 */
package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.config.Config;
import org.javerland.dataanonymizer.model.TableMetadata;

import java.sql.Connection;

/**
 * Callable class for processing a table.
 *
 * @author Juraj Pacolt
 */
public class TableProcessor {

    private Config config;
    private Connection connection;
    private TableMetadata table;

    public TableProcessor(Config config, Connection connection, TableMetadata table) {
        this.connection = connection;
        this.table = table;
    }

    public void process() {
        System.out.println("Processing table: " + table.getName());
        // Implement the actual processing logic here
    }

}
