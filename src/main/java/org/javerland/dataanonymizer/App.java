/* Created on 17.09.2025 */
package org.javerland.dataanonymizer;

import org.javerland.dataanonymizer.model.Config;
import org.javerland.dataanonymizer.util.ConfigUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Main application class for the Data Anonymizer tool.
 *
 * @author Juraj Pacolt
 */
//@formatter:off
@Command(name = "data-anonymizer", mixinStandardHelpOptions = true, version = "0.0.1",
        description = "Data anonymizer tool by JDBC")
//@formatter:on
public class App implements Runnable {

    @Option(names = { "-d",
            "--driver" }, description = "JDBC driver class", defaultValue = "org.postgresql.Driver", required = true)
    private String driver = "org.postgresql.Driver";
    @Option(names = { "-l", "--url" }, description = "URL connection string", required = true)
    private String url = null;
    @Option(names = { "-u", "--username" }, description = "User name", required = false)
    private String username = null;
    @Option(names = { "-p", "--password" }, description = "Password", required = false)
    private String password = null;
    @Option(names = { "-c", "--config" }, description = "Configuration file", required = false)
    private String configFile = null;
    @Option(names = { "-t", "--threads" }, description = "Thread pooling count for connections", required = false)
    private Integer threadPoolingCount = null;

    @Override
    public void run() {
        try {
            // load configuration
            Config config = ConfigUtils.load(configFile);
            // load JDBC driver and try connection
            Class.forName(driver);
            // TODO try to connections with thread pooling
            try (Connection conn = DriverManager.getConnection(url)) {
                // don't need big transaction, each update is atomic
                conn.setAutoCommit(true);
                // TODO for first is needed read medata from DB and select all tables and columns to anonymize by config
            }
        } catch (ClassNotFoundException | SQLException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new App()).execute(args));
    }
}
