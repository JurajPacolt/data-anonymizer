/* Created on 09.11.2025 */
package org.javerland.dataanonymizer.anonymizer;

import org.javerland.dataanonymizer.model.ColumnMetadata;
import org.javerland.dataanonymizer.model.TableMetadata;
import org.javerland.dataanonymizer.model.config.Address;
import org.javerland.dataanonymizer.model.config.Config;
import org.javerland.dataanonymizer.model.config.Name;
import org.javerland.dataanonymizer.model.config.SearchColumnTerms;

import java.util.ArrayList;
import java.util.List;

/**
 * Planner class responsible for creating anonymization plans for tables based on configuration.
 *
 * @author Juraj Pacolt
 */
public class AnonymizationPlanner {

    private final Config config;

    public AnonymizationPlanner(Config config) {
        this.config = config;
    }

    public List<ColumnAnonymizationPlan> createPlan(TableMetadata table) {
        List<ColumnAnonymizationPlan> plans = new ArrayList<>();

        if (config == null || config.getSearchColumnTerms() == null) {
            return plans;
        }

        if (isTableExcluded(table.getName())) {
            return plans;
        }

        SearchColumnTerms terms = config.getSearchColumnTerms();

        for (ColumnMetadata column : table.getColumns()) {
            String columnName = column.getName().toLowerCase();

            if (isColumnExcluded(columnName)) {
                continue;
            }

            if (table.getPrimaryKeys().contains(column.getName())) {
                continue;
            }

            AnonymizationType type = determineAnonymizationType(table.getName(), columnName, terms);
            if (type != null) {
                if (type == AnonymizationType.CUSTOM && terms.getCustoms() != null) {
                    for (String customExpr : terms.getCustoms()) {
                        if (columnName.contains(customExpr.toLowerCase())) {
                            plans.add(new ColumnAnonymizationPlan(column, type, customExpr));
                            break;
                        }
                    }
                } else {
                    plans.add(new ColumnAnonymizationPlan(column, type));
                }
            }
        }

        return plans;
    }

    private AnonymizationType determineAnonymizationType(String tableName, String columnName,
            SearchColumnTerms terms) {
        if (terms.getEmail() != null && matchesAny(columnName, terms.getEmail())) {
            return AnonymizationType.EMAIL;
        }

        if (terms.getName() != null) {
            Name nameConfig = terms.getName();
            if (matchesName(tableName, columnName, nameConfig)) {
                return AnonymizationType.NAME;
            }
        }

        if (terms.getSurname() != null && matchesAny(columnName, terms.getSurname())) {
            return AnonymizationType.SURNAME;
        }

        if (terms.getFullName() != null && matchesAny(columnName, terms.getFullName())) {
            return AnonymizationType.FULL_NAME;
        }

        if (terms.getUsername() != null && matchesAny(columnName, terms.getUsername())) {
            return AnonymizationType.USERNAME;
        }

        if (terms.getPhone() != null && matchesAny(columnName, terms.getPhone())) {
            return AnonymizationType.PHONE;
        }

        if (terms.getBirthDate() != null && matchesAny(columnName, terms.getBirthDate())) {
            return AnonymizationType.BIRTH_DATE;
        }

        // Personal identification
        if (terms.getSsn() != null && matchesAny(columnName, terms.getSsn())) {
            return AnonymizationType.SSN;
        }

        if (terms.getPassportNumber() != null && matchesAny(columnName, terms.getPassportNumber())) {
            return AnonymizationType.PASSPORT_NUMBER;
        }

        if (terms.getDriverLicense() != null && matchesAny(columnName, terms.getDriverLicense())) {
            return AnonymizationType.DRIVER_LICENSE;
        }

        if (terms.getTaxId() != null && matchesAny(columnName, terms.getTaxId())) {
            return AnonymizationType.TAX_ID;
        }

        if (terms.getNationalId() != null && matchesAny(columnName, terms.getNationalId())) {
            return AnonymizationType.NATIONAL_ID;
        }

        // Financial
        if (terms.getCreditCard() != null && matchesAny(columnName, terms.getCreditCard())) {
            return AnonymizationType.CREDIT_CARD;
        }

        if (terms.getIban() != null && matchesAny(columnName, terms.getIban())) {
            return AnonymizationType.IBAN;
        }

        if (terms.getBankAccount() != null && matchesAny(columnName, terms.getBankAccount())) {
            return AnonymizationType.BANK_ACCOUNT;
        }

        // Company
        if (terms.getCompanyName() != null && matchesAny(columnName, terms.getCompanyName())) {
            return AnonymizationType.COMPANY_NAME;
        }

        if (terms.getJobTitle() != null && matchesAny(columnName, terms.getJobTitle())) {
            return AnonymizationType.JOB_TITLE;
        }

        if (terms.getDepartment() != null && matchesAny(columnName, terms.getDepartment())) {
            return AnonymizationType.DEPARTMENT;
        }

        // Internet
        if (terms.getIpAddress() != null && matchesAny(columnName, terms.getIpAddress())) {
            return AnonymizationType.IP_ADDRESS;
        }

        if (terms.getMacAddress() != null && matchesAny(columnName, terms.getMacAddress())) {
            return AnonymizationType.MAC_ADDRESS;
        }

        if (terms.getUrl() != null && matchesAny(columnName, terms.getUrl())) {
            return AnonymizationType.URL;
        }

        if (terms.getDomain() != null && matchesAny(columnName, terms.getDomain())) {
            return AnonymizationType.DOMAIN;
        }

        // Additional personal
        if (terms.getGender() != null && matchesAny(columnName, terms.getGender())) {
            return AnonymizationType.GENDER;
        }

        if (terms.getBloodType() != null && matchesAny(columnName, terms.getBloodType())) {
            return AnonymizationType.BLOOD_TYPE;
        }

        if (terms.getAddress() != null) {
            Address addressConfig = terms.getAddress();
            if (addressConfig.getCity() != null && matchesAny(columnName, addressConfig.getCity())) {
                return AnonymizationType.CITY;
            }
            if (addressConfig.getCounty() != null && matchesAny(columnName, addressConfig.getCounty())) {
                return AnonymizationType.COUNTY;
            }
            if (addressConfig.getRegion() != null && matchesAny(columnName, addressConfig.getRegion())) {
                return AnonymizationType.REGION;
            }
            if (addressConfig.getCountry() != null && matchesAny(columnName, addressConfig.getCountry())) {
                return AnonymizationType.COUNTRY;
            }
            if (addressConfig.getPostalCode() != null && matchesAny(columnName, addressConfig.getPostalCode())) {
                return AnonymizationType.POSTAL_CODE;
            }
            if (addressConfig.getStreet() != null && matchesAny(columnName, addressConfig.getStreet())) {
                return AnonymizationType.STREET;
            }
        }

        if (terms.getCustoms() != null && matchesAny(columnName, terms.getCustoms())) {
            return AnonymizationType.CUSTOM;
        }

        return null;
    }

    private boolean matchesName(String tableName, String columnName, Name nameConfig) {
        if (nameConfig.getFilter() == null || nameConfig.getFilter().isEmpty()) {
            return false;
        }

        if (!matchesAny(columnName, nameConfig.getFilter())) {
            return false;
        }

        if (nameConfig.getTableNameSearchTerms() != null && !nameConfig.getTableNameSearchTerms().isEmpty()) {
            if (!matchesAny(tableName.toLowerCase(), nameConfig.getTableNameSearchTerms())) {
                return false;
            }
        }

        if (nameConfig.getExcludedTableNameSearchTerms() != null
                && !nameConfig.getExcludedTableNameSearchTerms().isEmpty()) {
            if (matchesAny(tableName.toLowerCase(), nameConfig.getExcludedTableNameSearchTerms())) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesAny(String value, List<String> patterns) {
        return patterns.stream().anyMatch(pattern -> value.contains(pattern.toLowerCase()));
    }

    private boolean isTableExcluded(String tableName) {
        if (config.getExcludeTableTerms() == null || config.getExcludeTableTerms().isEmpty()) {
            return false;
        }
        return matchesAny(tableName.toLowerCase(), config.getExcludeTableTerms());
    }

    private boolean isColumnExcluded(String columnName) {
        if (config.getExcludeColumnTerms() == null || config.getExcludeColumnTerms().isEmpty()) {
            return false;
        }
        return matchesAny(columnName, config.getExcludeColumnTerms());
    }
}
