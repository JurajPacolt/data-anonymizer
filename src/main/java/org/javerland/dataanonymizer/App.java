/* Created on 17.09.2025 */
package org.javerland.dataanonymizer;

import org.javerland.dataanonymizer.model.config.Config;
import org.javerland.dataanonymizer.model.TableMetadata;
import org.javerland.dataanonymizer.util.ConfigUtils;
import org.javerland.dataanonymizer.util.MetadataReader;
import org.javerland.dataanonymizer.util.TableBatcher;
import org.javerland.dataanonymizer.util.TableProcessor;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Option(names = {"-d",
            "--driver"}, description = "JDBC driver class", defaultValue = "org.postgresql.Driver", required = true)
    private String driver = "org.postgresql.Driver";
    @Option(names = {"-l", "--url"}, description = "URL connection string", required = true)
    private String url = null;
    @Option(names = {"-u", "--username"}, description = "User name", required = false)
    private String username = null;
    @Option(names = {"-p", "--password"}, description = "Password", required = false)
    private String password = null;
    @Option(names = {"-s", "--schema"}, description = "Schema", required = false)
    private String schema = null;
    @Option(names = {"-c", "--config"}, description = "Configuration file", required = false)
    private String configFile = null;
    @Option(names = {"-t", "--threads"}, description = "Thread pooling count for connections", defaultValue = "5")
    private int threadPoolingCount = 5;
    @Option(names = {"-b", "--batch-size"}, description = "Table batch size for processing", defaultValue = "5")
    private int batchSize = 5;

    @Override
    public void run() {
        try {
            // load configuration
            Config config = ConfigUtils.load(configFile);
            // load JDBC driver and try connection
            Class.forName(driver);
            // connection is needed for metadata reading
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                // don't need big transaction, each update is atomic
                conn.setAutoCommit(true);
                // For first is needed read medata from DB and select all tables ...
                Map<String, TableMetadata> tables = MetadataReader.loadAllTables(conn, schema);
                int normalizedBatchSize = Math.max(1, batchSize);
                List<List<TableMetadata>> batches = TableBatcher.splitIntoBatches(tables, normalizedBatchSize);
                // ... and columns to anonymize by config
                try (ExecutorService executor = Executors.newFixedThreadPool(threadPoolingCount)) {
                    List<CompletableFuture<Void>> futures = batches.stream()
                            .map(bs -> CompletableFuture.runAsync(() -> {
                                // each thread will have own connection from pool ...
                                try {
                                    try (Connection connection = DriverManager.getConnection(url, username, password)) {
                                        connection.setAutoCommit(true);
                                        bs.forEach(b -> new TableProcessor(config, connection, b).process());
                                    }
                                } catch (SQLException ex) {
                                    throw new IllegalStateException(ex.getMessage(), ex);
                                }
                            }, executor)).toList();
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new App()).execute(args));
    }
}
