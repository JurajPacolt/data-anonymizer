package org.javerland.dataanonymizer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Juraj Pacolt
 *
 * Unit test for simple App.
 */
public class AppTest {

    @TempDir
    Path tempDirectory;

    @Test
    void helpAndVersionWorkWithoutRequiredDatabaseOptions() {
        assertEquals(0, quietCommand().execute("--help"));
        assertEquals(0, quietCommand().execute("--version"));
        assertEquals("data-anonymizer development", new BuildVersionProvider().getVersion()[0]);
    }

    @Test
    void invalidNumericAndLocaleOptionsFailBeforeConnecting() {
        String[][] invalidOptions = {
                { "--threads", "0" },
                { "--batch-size", "0" },
                { "--jdbc-batch-size", "0" },
                { "--fetch-size", "0" },
                { "--locale", "###" }
        };

        for (String[] option : invalidOptions) {
            assertEquals(1, quietCommand().execute("--driver", "org.h2.Driver", "--url", "jdbc:h2:mem:unused",
                    option[0], option[1]), option[0]);
        }
    }

    @Test
    void dryRunWithGlobTableFilterDoesNotChangeData() throws Exception {
        String url = databaseUrl();
        try (Connection connection = DriverManager.getConnection(url);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE USERS (ID INT PRIMARY KEY, EMAIL VARCHAR(255))");
            statement.execute("INSERT INTO USERS VALUES (1, 'before@example.test')");
        }

        int exitCode = quietCommand().execute("--driver", "org.h2.Driver", "--url", url, "--schema", "PUBLIC",
                "--dry-run", "--table", "US*");

        assertEquals(0, exitCode);
        try (Connection connection = DriverManager.getConnection(url);
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT EMAIL FROM USERS WHERE ID = 1")) {
            assertTrue(result.next());
            assertEquals("before@example.test", result.getString(1));
        }
    }

    @Test
    void unmatchedTableFilterReturnsFailure() throws Exception {
        String url = databaseUrl();
        try (Connection connection = DriverManager.getConnection(url);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE USERS (ID INT PRIMARY KEY, EMAIL VARCHAR(255))");
        }

        int exitCode = quietCommand().execute("--driver", "org.h2.Driver", "--url", url, "--schema", "PUBLIC",
                "--dry-run", "--table", "missing*");

        assertEquals(1, exitCode);
    }

    @Test
    void continueOnErrorRollsBackFailedTableAndProcessesRemainingTables() throws Exception {
        String url = databaseUrl();
        try (Connection connection = DriverManager.getConnection(url);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE FAILING_VALUES (ID INT PRIMARY KEY, NICKNAME VARCHAR(100) UNIQUE)");
            statement.execute("INSERT INTO FAILING_VALUES VALUES (1, 'one'), (2, 'two')");
            statement.execute("CREATE TABLE PEOPLE (ID INT PRIMARY KEY, EMAIL VARCHAR(255))");
            statement.execute("INSERT INTO PEOPLE VALUES (1, 'before@example.test')");
        }
        Path config = tempDirectory.resolve("continue-config.json");
        Files.writeString(config, """
                {
                  "searchColumnTerms": {
                    "customs": [
                      "nickname=constant",
                      "email=constant@example.test"
                    ]
                  }
                }
                """);

        int exitCode = quietCommand().execute("--driver", "org.h2.Driver", "--url", url, "--schema", "PUBLIC",
                "--config", config.toString(), "--threads", "1", "--batch-size", "1", "--jdbc-batch-size", "1",
                "--continue-on-error", "--table", "FAILING_VALUES,PEOPLE");

        assertEquals(2, exitCode);
        try (Connection connection = DriverManager.getConnection(url);
                Statement statement = connection.createStatement();
                ResultSet failed = statement.executeQuery("SELECT NICKNAME FROM FAILING_VALUES ORDER BY ID")) {
            assertTrue(failed.next());
            assertEquals("one", failed.getString(1));
            assertTrue(failed.next());
            assertEquals("two", failed.getString(1));
        }
        try (Connection connection = DriverManager.getConnection(url);
                Statement statement = connection.createStatement();
                ResultSet completed = statement.executeQuery("SELECT EMAIL FROM PEOPLE WHERE ID = 1")) {
            assertTrue(completed.next());
            assertEquals("constant@example.test", completed.getString(1));
        }
    }

    private CommandLine quietCommand() {
        CommandLine commandLine = new CommandLine(new App());
        commandLine.setOut(new PrintWriter(new StringWriter()));
        commandLine.setErr(new PrintWriter(new StringWriter()));
        return commandLine;
    }

    private String databaseUrl() {
        return "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
    }
}
