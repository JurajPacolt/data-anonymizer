package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.TableMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetadataReaderTest {

    @Mock
    private Connection connection;
    @Mock
    private DatabaseMetaData metadata;
    @Mock
    private ResultSet tablesResultSet;

    @Test
    void usesCurrentCatalogForCatalogBasedDrivers() throws Exception {
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.supportsSchemasInTableDefinitions()).thenReturn(false);
        when(metadata.supportsCatalogsInTableDefinitions()).thenReturn(true);
        when(connection.getCatalog()).thenReturn("shop");
        when(metadata.getTables(eq("shop"), isNull(), eq("%"), any(String[].class)))
                .thenReturn(tablesResultSet);
        when(tablesResultSet.next()).thenReturn(false);

        Map<String, TableMetadata> tables = MetadataReader.loadAllTables(connection, null);

        assertTrue(tables.isEmpty());
        verify(metadata).getTables(eq("shop"), isNull(), eq("%"),
                org.mockito.ArgumentMatchers.argThat(types -> java.util.Arrays.equals(types, new String[] { "TABLE" })));
        verify(tablesResultSet).close();
    }

    @Test
    void passesSchemaPatternToSchemaBasedDrivers() throws Exception {
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.supportsSchemasInTableDefinitions()).thenReturn(true);
        when(metadata.getTables(isNull(), eq("public"), eq("%"), any(String[].class)))
                .thenReturn(tablesResultSet);
        when(tablesResultSet.next()).thenReturn(false);

        Map<String, TableMetadata> tables = MetadataReader.loadAllTables(connection, "public");

        assertTrue(tables.isEmpty());
        verify(metadata).getTables(isNull(), eq("public"), eq("%"), any(String[].class));
        verify(tablesResultSet).close();
    }
}
