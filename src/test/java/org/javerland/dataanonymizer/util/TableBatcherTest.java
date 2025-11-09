/* Created on 09.11.2025 */
package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.ForeignKeyMetadata;
import org.javerland.dataanonymizer.model.TableMetadata;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TableBatcher class.
 *
 * @author Juraj Pacolt
 */
class TableBatcherTest {

    @Test
    void testSplitIntoBatches_NoDependencies() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        tables.put("users", createTable("users"));
        tables.put("products", createTable("products"));
        tables.put("categories", createTable("categories"));
        
        List<List<TableMetadata>> batches = TableBatcher.splitIntoBatches(tables, 2);
        
        assertEquals(2, batches.size());
        assertEquals(2, batches.get(0).size());
        assertEquals(1, batches.get(1).size());
    }

    @Test
    void testSplitIntoBatches_SimpleDependency() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        
        TableMetadata users = createTable("users");
        TableMetadata orders = createTable("orders");
        addForeignKey(orders, "users");
        
        tables.put("users", users);
        tables.put("orders", orders);
        
        List<List<TableMetadata>> batches = TableBatcher.splitIntoBatches(tables, 2);
        
        assertEquals(2, batches.size());
        assertEquals("orders", batches.get(0).get(0).getName());
        assertEquals("users", batches.get(1).get(0).getName());
    }

    @Test
    void testSplitIntoBatches_BatchSizeOne() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        tables.put("users", createTable("users"));
        tables.put("products", createTable("products"));
        tables.put("orders", createTable("orders"));
        
        List<List<TableMetadata>> batches = TableBatcher.splitIntoBatches(tables, 1);
        
        assertEquals(3, batches.size());
        assertEquals(1, batches.get(0).size());
        assertEquals(1, batches.get(1).size());
        assertEquals(1, batches.get(2).size());
    }

    @Test
    void testSplitIntoBatches_LargeBatchSize() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        tables.put("users", createTable("users"));
        tables.put("products", createTable("products"));
        tables.put("orders", createTable("orders"));
        
        List<List<TableMetadata>> batches = TableBatcher.splitIntoBatches(tables, 10);
        
        assertEquals(1, batches.size());
        assertEquals(3, batches.get(0).size());
    }

    @Test
    void testSplitIntoBatches_ChainDependency() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        
        TableMetadata countries = createTable("countries");
        TableMetadata cities = createTable("cities");
        addForeignKey(cities, "countries");
        TableMetadata addresses = createTable("addresses");
        addForeignKey(addresses, "cities");
        
        tables.put("countries", countries);
        tables.put("cities", cities);
        tables.put("addresses", addresses);
        
        List<List<TableMetadata>> batches = TableBatcher.splitIntoBatches(tables, 2);
        
        assertEquals(3, batches.size());
        assertEquals("addresses", batches.get(0).get(0).getName());
        assertEquals("cities", batches.get(1).get(0).getName());
        assertEquals("countries", batches.get(2).get(0).getName());
    }

    @Test
    void testSplitIntoBatches_ParallelProcessing() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        
        TableMetadata users = createTable("users");
        TableMetadata products = createTable("products");
        TableMetadata orders = createTable("orders");
        addForeignKey(orders, "users");
        addForeignKey(orders, "products");
        
        TableMetadata payments = createTable("payments");
        addForeignKey(payments, "orders");
        
        tables.put("users", users);
        tables.put("products", products);
        tables.put("orders", orders);
        tables.put("payments", payments);
        
        List<List<TableMetadata>> batches = TableBatcher.splitIntoBatches(tables, 2);
        
        assertEquals(3, batches.size());
        
        Set<String> firstLayer = new HashSet<>();
        batches.get(0).forEach(t -> firstLayer.add(t.getName()));
        assertTrue(firstLayer.contains("payments") || firstLayer.contains("orders"));
    }

    @Test
    void testSplitIntoBatches_EmptyMap() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        
        List<List<TableMetadata>> batches = TableBatcher.splitIntoBatches(tables, 2);
        
        assertEquals(0, batches.size());
    }

    @Test
    void testSplitIntoBatches_SingleTable() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        tables.put("users", createTable("users"));
        
        List<List<TableMetadata>> batches = TableBatcher.splitIntoBatches(tables, 2);
        
        assertEquals(1, batches.size());
        assertEquals(1, batches.get(0).size());
        assertEquals("users", batches.get(0).get(0).getName());
    }

    @Test
    void testSplitIntoBatches_RespectsDependencyLayers() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        
        TableMetadata t1 = createTable("table1");
        TableMetadata t2 = createTable("table2");
        TableMetadata t3 = createTable("table3");
        TableMetadata t4 = createTable("table4");
        
        addForeignKey(t2, "table1");
        addForeignKey(t3, "table1");
        addForeignKey(t4, "table2");
        addForeignKey(t4, "table3");
        
        tables.put("table1", t1);
        tables.put("table2", t2);
        tables.put("table3", t3);
        tables.put("table4", t4);
        
        List<List<TableMetadata>> batches = TableBatcher.splitIntoBatches(tables, 2);
        
        assertTrue(batches.size() >= 3);
        
        assertEquals("table4", batches.get(0).get(0).getName());
        
        Set<String> secondLayer = new HashSet<>();
        batches.get(1).forEach(t -> secondLayer.add(t.getName()));
        assertTrue(secondLayer.contains("table2"));
        assertTrue(secondLayer.contains("table3"));
    }

    private TableMetadata createTable(String name) {
        TableMetadata table = new TableMetadata();
        table.setName(name);
        table.setForeignKeys(new ArrayList<>());
        return table;
    }

    private void addForeignKey(TableMetadata table, String referencedTable) {
        ForeignKeyMetadata fk = new ForeignKeyMetadata();
        fk.setPkTable(referencedTable);
        table.getForeignKeys().add(fk);
    }
}
