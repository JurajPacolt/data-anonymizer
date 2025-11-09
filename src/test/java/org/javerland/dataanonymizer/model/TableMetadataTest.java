/* Created on 09.11.2025 */
package org.javerland.dataanonymizer.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TableMetadata class.
 *
 * @author Juraj Pacolt
 */
class TableMetadataTest {

    @Test
    void testGetReferencedTables_NoForeignKeys() {
        TableMetadata table = new TableMetadata();
        table.setName("users");
        table.setForeignKeys(new ArrayList<>());
        
        Set<String> refs = table.getReferencedTables();
        
        assertTrue(refs.isEmpty());
    }

    @Test
    void testGetReferencedTables_SingleForeignKey() {
        TableMetadata table = new TableMetadata();
        table.setName("orders");
        
        List<ForeignKeyMetadata> fks = new ArrayList<>();
        ForeignKeyMetadata fk = new ForeignKeyMetadata();
        fk.setPkTable("users");
        fks.add(fk);
        table.setForeignKeys(fks);
        
        Set<String> refs = table.getReferencedTables();
        
        assertEquals(1, refs.size());
        assertTrue(refs.contains("users"));
    }

    @Test
    void testGetReferencedTables_MultipleForeignKeys() {
        TableMetadata table = new TableMetadata();
        table.setName("order_items");
        
        List<ForeignKeyMetadata> fks = new ArrayList<>();
        
        ForeignKeyMetadata fk1 = new ForeignKeyMetadata();
        fk1.setPkTable("orders");
        fks.add(fk1);
        
        ForeignKeyMetadata fk2 = new ForeignKeyMetadata();
        fk2.setPkTable("products");
        fks.add(fk2);
        
        table.setForeignKeys(fks);
        
        Set<String> refs = table.getReferencedTables();
        
        assertEquals(2, refs.size());
        assertTrue(refs.contains("orders"));
        assertTrue(refs.contains("products"));
    }

    @Test
    void testGetReferencedTables_DuplicateReferences() {
        TableMetadata table = new TableMetadata();
        table.setName("audit_log");
        
        List<ForeignKeyMetadata> fks = new ArrayList<>();
        
        ForeignKeyMetadata fk1 = new ForeignKeyMetadata();
        fk1.setPkTable("users");
        fks.add(fk1);
        
        ForeignKeyMetadata fk2 = new ForeignKeyMetadata();
        fk2.setPkTable("users");
        fks.add(fk2);
        
        table.setForeignKeys(fks);
        
        Set<String> refs = table.getReferencedTables();
        
        assertEquals(1, refs.size());
        assertTrue(refs.contains("users"));
    }

    @Test
    void testSettersAndGetters() {
        TableMetadata table = new TableMetadata();
        
        table.setSchema("public");
        table.setName("users");
        
        List<ColumnMetadata> columns = new ArrayList<>();
        ColumnMetadata col = new ColumnMetadata();
        col.setName("id");
        columns.add(col);
        table.setColumns(columns);
        
        List<String> pks = List.of("id");
        table.setPrimaryKeys(pks);
        
        assertEquals("public", table.getSchema());
        assertEquals("users", table.getName());
        assertEquals(1, table.getColumns().size());
        assertEquals("id", table.getColumns().get(0).getName());
        assertEquals(1, table.getPrimaryKeys().size());
        assertEquals("id", table.getPrimaryKeys().get(0));
    }

    @Test
    void testToString() {
        TableMetadata table = new TableMetadata();
        table.setName("users");
        table.setPrimaryKeys(List.of("id"));
        table.setForeignKeys(new ArrayList<>());
        
        String result = table.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("users"));
        assertTrue(result.contains("id"));
    }
}
