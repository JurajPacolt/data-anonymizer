package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.config.Address;
import org.javerland.dataanonymizer.model.config.Config;
import org.javerland.dataanonymizer.model.config.Name;
import org.javerland.dataanonymizer.model.config.SearchColumnTerms;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

final class ConfigValidator {

    private ConfigValidator() {
    }

    static void validate(Config config) {
        if (config == null || config.getSearchColumnTerms() == null) {
            throw new IllegalArgumentException("Configuration must contain searchColumnTerms");
        }

        SearchColumnTerms terms = config.getSearchColumnTerms();
        Map<String, List<String>> lists = new LinkedHashMap<>();
        lists.put("email", terms.getEmail());
        lists.put("surname", terms.getSurname());
        lists.put("fullName", terms.getFullName());
        lists.put("username", terms.getUsername());
        lists.put("phone", terms.getPhone());
        lists.put("birthDate", terms.getBirthDate());
        lists.put("ssn", terms.getSsn());
        lists.put("passportNumber", terms.getPassportNumber());
        lists.put("driverLicense", terms.getDriverLicense());
        lists.put("taxId", terms.getTaxId());
        lists.put("nationalId", terms.getNationalId());
        lists.put("creditCard", terms.getCreditCard());
        lists.put("iban", terms.getIban());
        lists.put("bankAccount", terms.getBankAccount());
        lists.put("companyName", terms.getCompanyName());
        lists.put("jobTitle", terms.getJobTitle());
        lists.put("department", terms.getDepartment());
        lists.put("ipAddress", terms.getIpAddress());
        lists.put("macAddress", terms.getMacAddress());
        lists.put("url", terms.getUrl());
        lists.put("domain", terms.getDomain());
        lists.put("gender", terms.getGender());
        lists.put("bloodType", terms.getBloodType());
        lists.put("excludeTableTerms", config.getExcludeTableTerms());
        lists.put("excludeColumnTerms", config.getExcludeColumnTerms());

        Name name = terms.getName();
        if (name != null) {
            lists.put("name.tableNameSearchTerms", name.getTableNameSearchTerms());
            lists.put("name.excludedTableNameSearchTerms", name.getExcludedTableNameSearchTerms());
            lists.put("name.filter", name.getFilter());
        }
        Address address = terms.getAddress();
        if (address != null) {
            lists.put("address.city", address.getCity());
            lists.put("address.county", address.getCounty());
            lists.put("address.region", address.getRegion());
            lists.put("address.country", address.getCountry());
            lists.put("address.postalCode", address.getPostalCode());
            lists.put("address.street", address.getStreet());
        }

        lists.forEach(ConfigValidator::validatePatterns);
        validateCustomMappings(terms.getCustoms());
    }

    private static void validatePatterns(String path, List<String> patterns) {
        if (patterns == null) {
            return;
        }
        for (String pattern : patterns) {
            if (pattern == null || pattern.isBlank()) {
                throw new IllegalArgumentException("Blank identifier pattern in " + path);
            }
            if (pattern.startsWith("regex:")) {
                try {
                    Pattern.compile(pattern.substring("regex:".length()), Pattern.CASE_INSENSITIVE);
                } catch (PatternSyntaxException ex) {
                    throw new IllegalArgumentException("Invalid regular expression in " + path + ": " + pattern, ex);
                }
            }
        }
    }

    private static void validateCustomMappings(List<String> customs) {
        if (customs == null) {
            return;
        }
        for (String mapping : customs) {
            if (mapping == null || mapping.isBlank()) {
                throw new IllegalArgumentException("Blank custom mapping");
            }
            int separator = mapping.contains("=>") ? mapping.indexOf("=>") : mapping.indexOf('=');
            int separatorLength = mapping.contains("=>") ? 2 : 1;
            if (separator <= 0 || mapping.substring(separator + separatorLength).isBlank()) {
                throw new IllegalArgumentException(
                        "Custom mapping must use columnPattern=expression: " + mapping);
            }
        }
    }
}
