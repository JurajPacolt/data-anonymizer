package org.javerland.dataanonymizer.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForeignKeyMetadataTest {

    @Test
    void buildsQualifiedReferencedTableKeyAndRetainsColumnMetadata() {
        ForeignKeyMetadata foreignKey = new ForeignKeyMetadata();
        foreignKey.setFkCatalog("sales");
        foreignKey.setFkSchema("public");
        foreignKey.setFkColumn("customer_id");
        foreignKey.setPkCatalog("sales");
        foreignKey.setPkSchema("crm");
        foreignKey.setPkTable("Customers");
        foreignKey.setPkColumn("ID");

        assertEquals("sales", foreignKey.getFkCatalog());
        assertEquals("public", foreignKey.getFkSchema());
        assertEquals("customer_id", foreignKey.getFkColumn());
        assertEquals("sales", foreignKey.getPkCatalog());
        assertEquals("crm", foreignKey.getPkSchema());
        assertEquals("Customers", foreignKey.getPkTable());
        assertEquals("ID", foreignKey.getPkColumn());
        assertEquals("sales.crm.customers", foreignKey.getReferencedTableKey());
    }
}
