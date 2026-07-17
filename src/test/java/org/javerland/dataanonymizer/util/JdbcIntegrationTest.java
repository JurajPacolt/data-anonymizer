package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.anonymizer.AnonymizationPlanner;
import org.javerland.dataanonymizer.model.TableMetadata;
import org.javerland.dataanonymizer.model.config.Config;
import org.javerland.dataanonymizer.model.config.SearchColumnTerms;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcIntegrationTest {

    @Test
    void metadataKeepsSameTableNameFromDifferentSchemas() throws Exception {
        try (Connection connection = connection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE SCHEMA S1");
                statement.execute("CREATE SCHEMA S2");
                statement.execute("CREATE TABLE S1.USERS (ID INT PRIMARY KEY, EMAIL VARCHAR(255))");
                statement.execute("CREATE TABLE S2.USERS (ID INT PRIMARY KEY, EMAIL VARCHAR(255))");
            }

            Map<String, TableMetadata> tables = MetadataReader.loadAllTables(connection, "S%");

            assertEquals(2, tables.size());
            assertEquals(2, tables.values().stream().map(TableMetadata::getKey).distinct().count());
        }
    }

    @Test
    void metadataAndPlannerProtectBothSidesOfForeignKeys() throws Exception {
        try (Connection connection = connection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE CUSTOMERS (ID INT PRIMARY KEY, EMAIL VARCHAR(255) UNIQUE)");
                statement.execute("CREATE TABLE ORDERS (ID INT PRIMARY KEY, CUSTOMER_EMAIL VARCHAR(255), "
                        + "CONTACT_EMAIL VARCHAR(255), FOREIGN KEY (CUSTOMER_EMAIL) REFERENCES CUSTOMERS(EMAIL))");
            }
            Map<String, TableMetadata> tables = MetadataReader.loadAllTables(connection, "PUBLIC");
            TableMetadata customers = onlyTable(tables, "CUSTOMERS");
            TableMetadata orders = onlyTable(tables, "ORDERS");

            assertTrue(customers.isReferencedKey("EMAIL"));
            assertTrue(orders.isForeignKey("CUSTOMER_EMAIL"));
            var plans = new AnonymizationPlanner(ConfigUtils.load(null)).createPlan(orders);
            assertTrue(plans.stream().noneMatch(plan -> plan.getColumn().getName().equals("CUSTOMER_EMAIL")));
            assertTrue(plans.stream().anyMatch(plan -> plan.getColumn().getName().equals("CONTACT_EMAIL")));
        }
    }

    @Test
    void processorUsesTransactionsBatchesAndUniqueValues() throws Exception {
        try (Connection connection = connection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE PEOPLE (ID INT PRIMARY KEY, EMAIL VARCHAR(255) UNIQUE, "
                        + "FIRST_NAME VARCHAR(100))");
                statement.execute("INSERT INTO PEOPLE VALUES (1, 'one@example.test', 'One'), "
                        + "(2, 'two@example.test', 'Two')");
            }
            TableMetadata table = onlyTable(MetadataReader.loadAllTables(connection, "PUBLIC"), "PEOPLE");

            int rows = new TableProcessor(ConfigUtils.load(null), connection, table, 1, 10, Locale.ENGLISH).process();

            assertEquals(2, rows);
            try (Statement statement = connection.createStatement();
                    ResultSet result = statement.executeQuery("SELECT EMAIL, FIRST_NAME FROM PEOPLE ORDER BY ID")) {
                assertTrue(result.next());
                String firstEmail = result.getString(1);
                assertNotEquals("one@example.test", firstEmail);
                assertNotEquals("One", result.getString(2));
                assertTrue(result.next());
                assertNotEquals(firstEmail, result.getString(1));
                assertNotEquals("two@example.test", result.getString(1));
            }
        }
    }

    @Test
    void processorRollsBackWholeTableOnGenerationFailure() throws Exception {
        try (Connection connection = connection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE CUSTOM_VALUES (ID INT PRIMARY KEY, NICKNAME VARCHAR(100) UNIQUE)");
                statement.execute("INSERT INTO CUSTOM_VALUES VALUES (1, 'one'), (2, 'two')");
            }
            TableMetadata table = onlyTable(MetadataReader.loadAllTables(connection, "PUBLIC"), "CUSTOM_VALUES");
            Config config = customConfig("nickname=constant");

            assertThrows(RuntimeException.class,
                    () -> new TableProcessor(config, connection, table, 1, 10, Locale.ENGLISH).process());

            try (Statement statement = connection.createStatement();
                    ResultSet result = statement.executeQuery("SELECT NICKNAME FROM CUSTOM_VALUES ORDER BY ID")) {
                assertTrue(result.next());
                assertEquals("one", result.getString(1));
                assertTrue(result.next());
                assertEquals("two", result.getString(1));
            }
        }
    }

    @Test
    void processorQuotesReservedAndCaseSensitiveIdentifiers() throws Exception {
        try (Connection connection = connection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE \"Order\" (\"Id\" INT PRIMARY KEY, \"Select\" VARCHAR(100))");
                statement.execute("INSERT INTO \"Order\" VALUES (1, 'original')");
            }
            TableMetadata table = onlyTable(MetadataReader.loadAllTables(connection, "PUBLIC"), "Order");

            int rows = new TableProcessor(customConfig("select=constant"), connection, table, 10, 10, Locale.ENGLISH)
                    .process();

            assertEquals(1, rows);
            try (Statement statement = connection.createStatement();
                    ResultSet result = statement.executeQuery("SELECT \"Select\" FROM \"Order\"")) {
                assertTrue(result.next());
                assertEquals("constant", result.getString(1));
            }
        }
    }

    private Connection connection() throws Exception {
        return DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
    }

    private TableMetadata onlyTable(Map<String, TableMetadata> tables, String tableName) {
        return tables.values().stream().filter(table -> table.getName().equalsIgnoreCase(tableName)).findFirst()
                .orElseThrow();
    }

    private Config customConfig(String mapping) {
        SearchColumnTerms terms = new SearchColumnTerms();
        terms.setCustoms(List.of(mapping));
        Config config = new Config();
        config.setSearchColumnTerms(terms);
        return config;
    }
}
