/* Created on 17.09.2025 */
package org.javerland.dataanonymizer;

import org.javerland.dataanonymizer.model.config.Config;
import org.javerland.dataanonymizer.anonymizer.AnonymizationPlanner;
import org.javerland.dataanonymizer.anonymizer.ColumnAnonymizationPlan;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Main application class for the Data Anonymizer tool.
 *
 * @author Juraj Pacolt
 */
//@formatter:off
@Command(name = "data-anonymizer", mixinStandardHelpOptions = true, versionProvider = BuildVersionProvider.class,
        description = "Data anonymizer tool by JDBC")
//@formatter:on
public class App implements Callable<Integer> {

    @Option(names = {"-d",
            "--driver"}, description = "JDBC driver class", defaultValue = "org.postgresql.Driver")
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
    @Option(names = {"-t", "--threads"}, description = "Maximum number of parallel database workers", defaultValue = "5")
    private int threadPoolingCount = 5;
    @Option(names = {"-b", "--batch-size"}, description = "Table batch size for processing", defaultValue = "5")
    private int batchSize = 5;
    @Option(names = "--jdbc-batch-size", description = "Rows per JDBC update batch", defaultValue = "500")
    private int jdbcBatchSize = 500;
    @Option(names = "--fetch-size", description = "JDBC result-set fetch size", defaultValue = "1000")
    private int fetchSize = 1_000;
    @Option(names = "--locale", description = "DataFaker locale language tag", defaultValue = "en")
    private String localeTag = "en";
    @Option(names = "--dry-run", description = "Print the anonymization plan without changing data")
    private boolean dryRun;
    @Option(names = "--table", split = ",", description = "Process only matching table names; '*' is supported")
    private List<String> tableFilters = new ArrayList<>();
    @Option(names = "--continue-on-error", description = "Continue with other tables and print a failure summary")
    private boolean continueOnError;

    @Override
    public Integer call() {
        validateOptions();
        try {
            // load configuration
            Config config = ConfigUtils.load(configFile);
            // load JDBC driver and try connection
            Class.forName(driver);
            // connection is needed for metadata reading
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                // For first is needed read medata from DB and select all tables ...
                Map<String, TableMetadata> tables = selectTables(MetadataReader.loadAllTables(conn, schema));
                if (dryRun) {
                    printDryRun(config, tables);
                    return 0;
                }

                TableBatcher.ExecutionPlan plan = TableBatcher.createExecutionPlan(tables, batchSize);
                if (!plan.unresolvedTables().isEmpty()) {
                    System.err.println("Warning: Cyclic or unresolved dependencies detected. Key columns are protected; "
                            + "processing these tables in a final layer: "
                            + plan.unresolvedTables().stream().map(TableMetadata::getKey).toList());
                }
                List<TableFailure> failures = processLayers(config, plan.layers(), parseLocale());
                if (!failures.isEmpty()) {
                    System.err.println("Anonymization completed with " + failures.size() + " failed table(s):");
                    failures.forEach(failure -> System.err.println("  " + failure.tableKey() + ": " + failure.message()));
                    System.err.println("Re-run failed tables with --table <name> after fixing the cause.");
                    return 2;
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        return 0;
    }

    private List<TableFailure> processLayers(Config config, List<List<List<TableMetadata>>> layers, Locale locale) {
        Queue<TableFailure> failures = new ConcurrentLinkedQueue<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(threadPoolingCount)) {
            for (List<List<TableMetadata>> layer : layers) {
                List<CompletableFuture<Void>> futures = layer.stream().map(batch -> CompletableFuture.runAsync(() -> {
                    try (Connection connection = DriverManager.getConnection(url, username, password)) {
                        for (TableMetadata table : batch) {
                            try {
                                new TableProcessor(config, connection, table, jdbcBatchSize, fetchSize, locale).process();
                            } catch (RuntimeException ex) {
                                if (!continueOnError) {
                                    throw ex;
                                }
                                failures.add(new TableFailure(table.getKey(), rootMessage(ex)));
                            }
                        }
                    } catch (SQLException ex) {
                        if (!continueOnError) {
                            throw new IllegalStateException(ex.getMessage(), ex);
                        }
                        batch.forEach(table -> failures.add(new TableFailure(table.getKey(), rootMessage(ex))));
                    }
                }, executor)).toList();
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }
        }
        return List.copyOf(failures);
    }

    private Map<String, TableMetadata> selectTables(Map<String, TableMetadata> tables) {
        if (tableFilters == null || tableFilters.isEmpty()) {
            return tables;
        }
        Map<String, TableMetadata> selected = new LinkedHashMap<>();
        tables.forEach((key, table) -> {
            if (tableFilters.stream().anyMatch(filter -> matchesTableFilter(table, filter))) {
                selected.put(key, table);
            }
        });
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("No table matched --table " + tableFilters);
        }
        return selected;
    }

    private boolean matchesTableFilter(TableMetadata table, String filter) {
        String regex = Pattern.quote(filter).replace("*", "\\E.*\\Q");
        return Pattern.compile("^" + regex + "$", Pattern.CASE_INSENSITIVE).matcher(table.getName()).matches()
                || Pattern.compile("^" + regex + "$", Pattern.CASE_INSENSITIVE).matcher(table.getKey()).matches();
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : current.getClass().getSimpleName();
    }

    private void printDryRun(Config config, Map<String, TableMetadata> tables) {
        AnonymizationPlanner planner = new AnonymizationPlanner(config);
        System.out.println("Dry run - no data will be changed.");
        for (TableMetadata table : tables.values()) {
            List<ColumnAnonymizationPlan> plans = planner.createPlan(table);
            if (!plans.isEmpty()) {
                System.out.println(table.getKey() + ":");
                plans.forEach(plan -> System.out.println("  " + plan.getColumn().getName() + " -> " + plan.getType()));
            }
            table.getColumns().stream().filter(column -> table.isProtectedKey(column.getName())).forEach(column ->
                    System.out.println("  " + table.getKey() + "." + column.getName() + " -> protected ("
                            + table.protectionReason(column.getName()) + ")"));
        }
    }

    private Locale parseLocale() {
        Locale locale = Locale.forLanguageTag(localeTag.replace('_', '-'));
        if (locale.getLanguage().isBlank()) {
            throw new IllegalArgumentException("Invalid locale: " + localeTag);
        }
        return locale;
    }

    private void validateOptions() {
        if (threadPoolingCount < 1) {
            throw new IllegalArgumentException("--threads must be at least 1");
        }
        if (batchSize < 1) {
            throw new IllegalArgumentException("--batch-size must be at least 1");
        }
        if (jdbcBatchSize < 1) {
            throw new IllegalArgumentException("--jdbc-batch-size must be at least 1");
        }
        if (fetchSize < 1) {
            throw new IllegalArgumentException("--fetch-size must be at least 1");
        }
        parseLocale();
    }

    private record TableFailure(String tableKey, String message) {
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new App()).execute(args));
    }
}
