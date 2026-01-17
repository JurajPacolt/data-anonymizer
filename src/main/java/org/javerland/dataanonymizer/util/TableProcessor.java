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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Callable class for processing a table.
 *
 * @author Juraj Pacolt
 */
public class TableProcessor {

    private Config config;
    private Connection connection;
    private TableMetadata table;
    private Anonymizer anonymizer;
    private AnonymizationPlanner planner;

    public TableProcessor(Config config, Connection connection, TableMetadata table) {
        this.config = config;
        this.connection = connection;
        this.table = table;
        this.anonymizer = new Anonymizer();
        this.planner = new AnonymizationPlanner(config);
    }

    public void process() {
        List<ColumnAnonymizationPlan> plans = planner.createPlan(table);

        if (plans.isEmpty()) {
            System.out.println("Skipping table (no columns to anonymize): " + table.getName());
            return;
        }

        System.out.println("Processing table: " + table.getName() + " (" + plans.size() + " columns to anonymize)");

        try {
            anonymizeTable(plans);
            System.out.println("Completed table: " + table.getName());
        } catch (SQLException ex) {
            System.err.println("Error processing table " + table.getName() + ": " + ex.getMessage());
            throw new RuntimeException("Failed to anonymize table: " + table.getName(), ex);
        }
    }

    private void anonymizeTable(List<ColumnAnonymizationPlan> plans) throws SQLException {
        if (table.getPrimaryKeys().isEmpty()) {
            System.out.println("Warning: Table " + table.getName() + " has no primary key. Skipping.");
            return;
        }

        List<String> primaryKeys = table.getPrimaryKeys();
        String schema = table.getSchema() != null ? table.getSchema() + "." : "";
        String fullTableName = schema + table.getName();

        String selectQuery = String.format("SELECT %s FROM %s", String.join(", ", primaryKeys), fullTableName);

        String setClause = plans.stream().map(p -> p.getColumn().getName() + " = ?").collect(Collectors.joining(", "));

        String whereClause = primaryKeys.stream().map(pk -> pk + " = ?").collect(Collectors.joining(" AND "));
        String updateQuery = String.format("UPDATE %s SET %s WHERE %s", fullTableName, setClause, whereClause);

        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                ResultSet rs = selectStmt.executeQuery();
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

            int rowCount = 0;
            while (rs.next()) {
                Object[] pkValues = new Object[primaryKeys.size()];
                for (int i = 0; i < primaryKeys.size(); i++) {
                    pkValues[i] = rs.getObject(primaryKeys.get(i));
                }

                for (int i = 0; i < plans.size(); i++) {
                    ColumnAnonymizationPlan plan = plans.get(i);
                    Object anonymizedValue = normalizeValue(generateAnonymizedValue(plan), plan.getColumn());
                    updateStmt.setObject(i + 1, anonymizedValue);
                }

                for (int i = 0; i < pkValues.length; i++) {
                    updateStmt.setObject(plans.size() + i + 1, pkValues[i]);
                }
                updateStmt.executeUpdate();

                rowCount++;
                if (rowCount % 1000 == 0) {
                    System.out.println("  Processed " + rowCount + " rows in " + table.getName());
                }
            }

            System.out.println("  Total rows anonymized: " + rowCount);
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
