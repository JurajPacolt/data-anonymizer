package org.javerland.dataanonymizer;

import org.javerland.dataanonymizer.model.TableMetadata;
import org.javerland.dataanonymizer.util.ConfigUtils;
import org.javerland.dataanonymizer.util.MetadataReader;
import org.javerland.dataanonymizer.util.TableProcessor;
import org.junit.jupiter.api.Test;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class DatabaseContainerIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"))
            .withDatabaseName("anonymizer").withUsername("test").withPassword("test");

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer(DockerImageName.parse("mysql:8.4"))
            .withDatabaseName("anonymizer").withUsername("test").withPassword("test");

    @Test
    void anonymizesPostgreSqlThroughJdbcMetadata() throws Exception {
        verifyDatabase(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), "public");
    }

    @Test
    void anonymizesMySqlThroughJdbcMetadata() throws Exception {
        verifyDatabase(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword(), null);
    }

    private void verifyDatabase(String url, String username, String password, String schema) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE users (id INT PRIMARY KEY, email VARCHAR(255), first_name VARCHAR(100))");
                statement.execute("INSERT INTO users VALUES (1, 'before@example.test', 'Before')");
            }

            Map<String, TableMetadata> tables = MetadataReader.loadAllTables(connection, schema);
            TableMetadata users = tables.values().stream().filter(table -> table.getName().equalsIgnoreCase("users"))
                    .findFirst().orElseThrow();
            new TableProcessor(ConfigUtils.load(null), connection, users, 100, 100, Locale.ENGLISH).process();

            try (Statement statement = connection.createStatement();
                    ResultSet result = statement.executeQuery("SELECT email, first_name FROM users")) {
                assertTrue(result.next());
                assertNotEquals("before@example.test", result.getString(1));
                assertNotEquals("Before", result.getString(2));
            }
        }
    }
}
