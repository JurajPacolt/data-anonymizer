/* Created on 19.09.2025 */
package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.anonymizer.Anonymizer;
import org.javerland.dataanonymizer.anonymizer.AnonymizationPlanner;
import org.javerland.dataanonymizer.anonymizer.AnonymizationType;
import org.javerland.dataanonymizer.anonymizer.ColumnAnonymizationPlan;
import org.javerland.dataanonymizer.model.config.Config;
import org.javerland.dataanonymizer.model.TableMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Callable class for processing a table.
 *
 * @author Juraj Pacolt
 */
public class TableProcessor {

    private final Connection connection;
    private final TableMetadata table;
    private final Anonymizer anonymizer;
    private final AnonymizationPlanner planner;
    private final int jdbcBatchSize;
    private final int fetchSize;

    public TableProcessor(Config config, Connection connection, TableMetadata table) {
        this(config, connection, table, 500, 1_000, Locale.ENGLISH);
    }

    public TableProcessor(Config config, Connection connection, TableMetadata table, int jdbcBatchSize, int fetchSize,
            Locale locale) {
        this.connection = connection;
        this.table = table;
        this.jdbcBatchSize = Math.max(1, jdbcBatchSize);
        this.fetchSize = Math.max(1, fetchSize);
        this.anonymizer = new Anonymizer(locale);
        this.planner = new AnonymizationPlanner(config);
    }

    public int process() {
        List<ColumnAnonymizationPlan> plans = planner.createPlan(table);

        if (plans.isEmpty()) {
            System.out.println("Skipping table (no columns to anonymize): " + table.getName());
            return 0;
        }

        if (table.getPrimaryKeys().isEmpty()) {
            System.out.println("Warning: Table " + table.getName() + " has no primary key. Skipping.");
            return 0;
        }

        System.out.println("Processing table: " + table.getName() + " (" + plans.size() + " columns to anonymize)");

        boolean originalAutoCommit;
        try {
            originalAutoCommit = connection.getAutoCommit();
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot read transaction state", ex);
        }

        try {
            connection.setAutoCommit(false);
            int rowCount = anonymizeTable(plans);
            connection.commit();
            System.out.println("Completed table: " + table.getName());
            return rowCount;
        } catch (SQLException | RuntimeException ex) {
            rollback(ex);
            System.err.println("Error processing table " + table.getName() + ": " + ex.getMessage());
            throw new RuntimeException("Failed to anonymize table: " + table.getName(), ex);
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException ex) {
                System.err.println("Warning: Cannot restore auto-commit for table " + table.getName() + ": "
                        + ex.getMessage());
            }
        }
    }

    private int anonymizeTable(List<ColumnAnonymizationPlan> plans) throws SQLException {
        List<String> primaryKeys = table.getPrimaryKeys();
        SqlIdentifierQuoter quoter = new SqlIdentifierQuoter(connection.getMetaData());
        String fullTableName = quoter.tableName(table);

        String selectQuery = String.format("SELECT %s FROM %s",
                primaryKeys.stream().map(quoter::quote).collect(Collectors.joining(", ")), fullTableName);

        String setClause = plans.stream().map(p -> quoter.quote(p.getColumn().getName()) + " = ?")
                .collect(Collectors.joining(", "));

        String whereClause = primaryKeys.stream().map(pk -> quoter.quote(pk) + " = ?")
                .collect(Collectors.joining(" AND "));
        String updateQuery = String.format("UPDATE %s SET %s WHERE %s", fullTableName, setClause, whereClause);

        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

            selectStmt.setFetchSize(fetchSize);
            Map<String, Set<Object>> generatedUniqueValues = new HashMap<>();

            int rowCount = 0;
            try (ResultSet rs = selectStmt.executeQuery()) {
                while (rs.next()) {
                    Object[] pkValues = new Object[primaryKeys.size()];
                    for (int i = 0; i < primaryKeys.size(); i++) {
                        pkValues[i] = rs.getObject(i + 1);
                    }

                    for (int i = 0; i < plans.size(); i++) {
                        ColumnAnonymizationPlan plan = plans.get(i);
                        Object anonymizedValue = generateNormalizedValue(plan, generatedUniqueValues);
                        updateStmt.setObject(i + 1, anonymizedValue);
                    }

                    for (int i = 0; i < pkValues.length; i++) {
                        updateStmt.setObject(plans.size() + i + 1, pkValues[i]);
                    }
                    updateStmt.addBatch();

                    rowCount++;
                    if (rowCount % jdbcBatchSize == 0) {
                        updateStmt.executeBatch();
                    }
                    if (rowCount % 1000 == 0) {
                        System.out.println("  Processed " + rowCount + " rows in " + table.getName());
                    }
                }
            }
            if (rowCount % jdbcBatchSize != 0) {
                updateStmt.executeBatch();
            }

            System.out.println("  Total rows anonymized: " + rowCount);
            return rowCount;
        }
    }

    private Object generateNormalizedValue(ColumnAnonymizationPlan plan, Map<String, Set<Object>> uniqueValues) {
        String columnName = plan.getColumn().getName();
        Set<Object> usedValues = table.isUnique(columnName)
                ? uniqueValues.computeIfAbsent(columnName.toLowerCase(Locale.ROOT), ignored -> new HashSet<>())
                : null;

        for (int attempt = 0; attempt < 100; attempt++) {
            Object value = normalizeValue(generateAnonymizedValue(plan), plan.getColumn());
            if (usedValues == null || usedValues.add(value)) {
                return value;
            }
        }
        throw new IllegalStateException("Could not generate a unique value for " + table.getName() + "." + columnName);
    }

    private void rollback(Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    private Object generateAnonymizedValue(ColumnAnonymizationPlan plan) {
        AnonymizationType type = plan.getType();

        return switch (type) {
        case EMAIL -> anonymizer.anonymizeEmail();
        case NAME -> anonymizer.anonymizeName();
        case SURNAME -> anonymizer.anonymizeSurname();
        case FULL_NAME -> anonymizer.anonymizeFullName();
        case USERNAME -> anonymizer.anonymizeUsername();
        case PHONE -> anonymizer.anonymizePhone();
        case BIRTH_DATE -> anonymizer.anonymizeBirthDate();
        case CITY -> anonymizer.anonymizeCity();
        case COUNTY -> anonymizer.anonymizeCounty();
        case REGION -> anonymizer.anonymizeRegion();
        case COUNTRY -> anonymizer.anonymizeCountry();
        case POSTAL_CODE -> anonymizer.anonymizePostalCode();
        case STREET -> anonymizer.anonymizeStreet();
        case SSN -> anonymizer.anonymizeSsn();
        case PASSPORT_NUMBER -> anonymizer.anonymizePassportNumber();
        case DRIVER_LICENSE -> anonymizer.anonymizeDriverLicense();
        case TAX_ID -> anonymizer.anonymizeTaxId();
        case NATIONAL_ID -> anonymizer.anonymizeNationalId();
        case CREDIT_CARD -> anonymizer.anonymizeCreditCard();
        case IBAN -> anonymizer.anonymizeIban();
        case BANK_ACCOUNT -> anonymizer.anonymizeBankAccount();
        case COMPANY_NAME -> anonymizer.anonymizeCompanyName();
        case JOB_TITLE -> anonymizer.anonymizeJobTitle();
        case DEPARTMENT -> anonymizer.anonymizeDepartment();
        case IP_ADDRESS -> anonymizer.anonymizeIpAddress();
        case MAC_ADDRESS -> anonymizer.anonymizeMacAddress();
        case URL -> anonymizer.anonymizeUrl();
        case DOMAIN -> anonymizer.anonymizeDomain();
        case GENDER -> anonymizer.anonymizeGender();
        case BLOOD_TYPE -> anonymizer.anonymizeBloodType();
        case CUSTOM -> plan.getCustomExpression() != null ? anonymizer.anonymizeCustom(plan.getCustomExpression())
                : null;
        };
    }

    private Object normalizeValue(Object value, org.javerland.dataanonymizer.model.ColumnMetadata column) {
        if (!(value instanceof String)) {
            return value;
        }

        String strValue = (String) value;
        int size = column.getSize();
        if (size > 0 && strValue.length() > size) {
            return strValue.substring(0, size);
        }

        return strValue;
    }

}
