/* Created on 09.11.2025 */
package org.javerland.dataanonymizer.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ColumnMetadata class.
 *
 * @author Juraj Pacolt
 */
class ColumnMetadataTest {

    @Test
    void testSettersAndGetters() {
        ColumnMetadata column = new ColumnMetadata();
        
        column.setName("email");
        column.setTypeName("VARCHAR");
        column.setSize(255);
        column.setNullable(true);
        
        assertEquals("email", column.getName());
        assertEquals("VARCHAR", column.getTypeName());
        assertEquals(255, column.getSize());
        assertTrue(column.isNullable());
    }

    @Test
    void testNullableDefaultValue() {
        ColumnMetadata column = new ColumnMetadata();
        assertFalse(column.isNullable());
    }

    @Test
    void testSizeDefaultValue() {
        ColumnMetadata column = new ColumnMetadata();
        assertEquals(0, column.getSize());
    }
}
