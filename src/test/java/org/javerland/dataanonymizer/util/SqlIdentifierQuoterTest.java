package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.TableMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqlIdentifierQuoterTest {

    @Mock
    private DatabaseMetaData metadata;

    @Test
    void quotesSchemaQualifiedNamesAndEscapesQuoteCharacters() throws Exception {
        when(metadata.getIdentifierQuoteString()).thenReturn("\"");
        when(metadata.supportsCatalogsInDataManipulation()).thenReturn(true);
        SqlIdentifierQuoter quoter = new SqlIdentifierQuoter(metadata);
        TableMetadata table = table("catalog", "Sales", "Order");

        assertEquals("\"Sales\".\"Order\"", quoter.tableName(table));
        assertEquals("\"strange\"\"name\"", quoter.quote("strange\"name"));
    }

    @Test
    void usesCatalogWhenSchemaIsMissingAndDriverSupportsCatalogs() throws Exception {
        when(metadata.getIdentifierQuoteString()).thenReturn("`");
        when(metadata.supportsCatalogsInDataManipulation()).thenReturn(true);
        SqlIdentifierQuoter quoter = new SqlIdentifierQuoter(metadata);

        assertEquals("`shop`.`users`", quoter.tableName(table("shop", null, "users")));
    }

    @Test
    void handlesDriversWithoutIdentifierQuoting() throws Exception {
        when(metadata.getIdentifierQuoteString()).thenReturn(" ");
        when(metadata.supportsCatalogsInDataManipulation()).thenReturn(false);
        SqlIdentifierQuoter quoter = new SqlIdentifierQuoter(metadata);

        assertEquals("users", quoter.tableName(table("shop", null, "users")));
        assertEquals("email", quoter.quote("email"));
    }

    @Test
    void rejectsBlankIdentifiers() throws Exception {
        when(metadata.getIdentifierQuoteString()).thenReturn("\"");
        SqlIdentifierQuoter quoter = new SqlIdentifierQuoter(metadata);

        assertThrows(IllegalArgumentException.class, () -> quoter.quote(null));
        assertThrows(IllegalArgumentException.class, () -> quoter.quote("  "));
    }

    private TableMetadata table(String catalog, String schema, String name) {
        TableMetadata table = new TableMetadata();
        table.setCatalog(catalog);
        table.setSchema(schema);
        table.setName(name);
        return table;
    }
}
