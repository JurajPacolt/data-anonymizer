package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.ColumnMetadata;
import org.javerland.dataanonymizer.model.TableMetadata;
import org.javerland.dataanonymizer.model.config.Config;
import org.javerland.dataanonymizer.model.config.SearchColumnTerms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableProcessorTest {

    @Mock
    private Connection connection;
    @Mock
    private DatabaseMetaData metadata;
    @Mock
    private PreparedStatement selectStatement;
    @Mock
    private PreparedStatement updateStatement;
    @Mock
    private ResultSet resultSet;

    @Test
    void skipsJdbcCompletelyWhenNothingIsPlanned() {
        TableMetadata table = tableWithEmail(true);

        int rows = new TableProcessor(emptyConfig(), connection, table).process();

        assertEquals(0, rows);
        verifyNoInteractions(connection);
    }

    @Test
    void skipsJdbcCompletelyWhenTableHasNoPrimaryKey() {
        TableMetadata table = tableWithEmail(false);

        int rows = new TableProcessor(customConfig("email=constant"), connection, table).process();

        assertEquals(0, rows);
        verifyNoInteractions(connection);
    }

    @Test
    void commitsRowsInConfiguredBatchesAndRestoresAutoCommit() throws Exception {
        TableMetadata table = tableWithEmail(true);
        table.setSchema("public");
        table.getColumns().get(0).setSize(4);
        prepareJdbcMocks();
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getObject(1)).thenReturn(1, 2);

        int rows = new TableProcessor(customConfig("email=long-value"), connection, table, 1, 25,
                Locale.ENGLISH).process();

        assertEquals(2, rows);
        verify(connection).prepareStatement("SELECT \"id\" FROM \"public\".\"users\"");
        verify(connection).prepareStatement(
                "UPDATE \"public\".\"users\" SET \"email\" = ? WHERE \"id\" = ?");
        verify(selectStatement).setFetchSize(25);
        verify(updateStatement, times(2)).setObject(1, "long");
        verify(updateStatement).setObject(2, 1);
        verify(updateStatement).setObject(2, 2);
        verify(updateStatement, times(2)).addBatch();
        verify(updateStatement, times(2)).executeBatch();

        InOrder transaction = inOrder(connection);
        transaction.verify(connection).setAutoCommit(false);
        transaction.verify(connection).commit();
        transaction.verify(connection).setAutoCommit(true);
        verify(connection, never()).rollback();
    }

    @Test
    void rollsBackSqlFailureAndRestoresAutoCommit() throws Exception {
        TableMetadata table = tableWithEmail(true);
        prepareJdbcMocks();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(1)).thenReturn(1);
        when(updateStatement.executeBatch()).thenThrow(new SQLException("unique constraint"));

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> new TableProcessor(customConfig("email=constant"), connection, table, 10, 25,
                        Locale.ENGLISH).process());

        assertTrue(error.getMessage().contains("users"));
        assertEquals("unique constraint", error.getCause().getMessage());
        InOrder transaction = inOrder(connection);
        transaction.verify(connection).setAutoCommit(false);
        transaction.verify(connection).rollback();
        transaction.verify(connection).setAutoCommit(true);
        verify(connection, never()).commit();
    }

    @Test
    void reportsFailureWhenTransactionStateCannotBeRead() throws Exception {
        TableMetadata table = tableWithEmail(true);
        when(connection.getAutoCommit()).thenThrow(new SQLException("connection closed"));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new TableProcessor(customConfig("email=constant"), connection, table).process());

        assertEquals("Cannot read transaction state", error.getMessage());
        verify(connection, never()).setAutoCommit(false);
        verify(connection, never()).rollback();
    }

    private void prepareJdbcMocks() throws Exception {
        when(connection.getAutoCommit()).thenReturn(true);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getIdentifierQuoteString()).thenReturn("\"");
        when(metadata.supportsCatalogsInDataManipulation()).thenReturn(false);
        when(connection.prepareStatement(anyString())).thenReturn(selectStatement, updateStatement);
        when(selectStatement.executeQuery()).thenReturn(resultSet);
    }

    private TableMetadata tableWithEmail(boolean withPrimaryKey) {
        ColumnMetadata email = new ColumnMetadata();
        email.setName("email");
        email.setSize(255);

        TableMetadata table = new TableMetadata();
        table.setName("users");
        table.setColumns(List.of(email));
        table.setPrimaryKeys(withPrimaryKey ? List.of("id") : List.of());
        return table;
    }

    private Config emptyConfig() {
        Config config = new Config();
        config.setSearchColumnTerms(new SearchColumnTerms());
        return config;
    }

    private Config customConfig(String mapping) {
        SearchColumnTerms terms = new SearchColumnTerms();
        terms.setCustoms(List.of(mapping));
        Config config = new Config();
        config.setSearchColumnTerms(terms);
        return config;
    }
}
