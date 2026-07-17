/* Created on 09.11.2025 */
package org.javerland.dataanonymizer.anonymizer;

import org.javerland.dataanonymizer.model.ColumnMetadata;
import org.javerland.dataanonymizer.model.ForeignKeyMetadata;
import org.javerland.dataanonymizer.model.TableMetadata;
import org.javerland.dataanonymizer.model.config.Address;
import org.javerland.dataanonymizer.model.config.Config;
import org.javerland.dataanonymizer.model.config.Name;
import org.javerland.dataanonymizer.model.config.SearchColumnTerms;
import org.javerland.dataanonymizer.util.ConfigUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AnonymizationPlanner class.
 *
 * @author Juraj Pacolt
 */
class AnonymizationPlannerTest {

    private Config config;
    private AnonymizationPlanner planner;

    @BeforeEach
    void setUp() {
        config = new Config();
        SearchColumnTerms terms = new SearchColumnTerms();
        
        terms.setEmail(List.of("email", "mail"));
        terms.setSurname(List.of("surname", "last_name"));
        terms.setPhone(List.of("phone", "telephone"));
        
        Name nameConfig = new Name();
        nameConfig.setFilter(List.of("name", "first_name"));
        nameConfig.setTableNameSearchTerms(List.of("users", "customers"));
        terms.setName(nameConfig);
        
        Address addressConfig = new Address();
        addressConfig.setCity(List.of("city"));
        addressConfig.setCountry(List.of("country"));
        addressConfig.setStreet(List.of("street"));
        terms.setAddress(addressConfig);
        
        config.setSearchColumnTerms(terms);
        config.setExcludeTableTerms(List.of("log", "audit"));
        config.setExcludeColumnTerms(List.of("id", "created_at"));
        
        planner = new AnonymizationPlanner(config);
    }

    @Test
    void testCreatePlan_EmailColumn() {
        TableMetadata table = createTable("users", "id", "email", "phone");
        
        List<ColumnAnonymizationPlan> plans = planner.createPlan(table);
        
        assertEquals(2, plans.size());
        assertTrue(plans.stream().anyMatch(p -> p.getColumn().getName().equals("email")));
        assertTrue(plans.stream().anyMatch(p -> p.getColumn().getName().equals("phone")));
    }

    @Test
    void testCreatePlan_NameColumnInUsersTable() {
        TableMetadata table = createTable("users", "id", "first_name", "email");
        
        List<ColumnAnonymizationPlan> plans = planner.createPlan(table);
        
        assertEquals(2, plans.size());
        assertTrue(plans.stream().anyMatch(p -> p.getType() == AnonymizationType.NAME));
        assertTrue(plans.stream().anyMatch(p -> p.getType() == AnonymizationType.EMAIL));
    }

    @Test
    void testCreatePlan_NameColumnNotInUsersTable() {
        TableMetadata table = createTable("products", "id", "name", "price");
        
        List<ColumnAnonymizationPlan> plans = planner.createPlan(table);
        
        assertEquals(0, plans.size());
    }

    @Test
    void testCreatePlan_ExcludedTable() {
        TableMetadata table = createTable("audit_log", "id", "email", "action");
        
        List<ColumnAnonymizationPlan> plans = planner.createPlan(table);
        
        assertEquals(0, plans.size());
    }

    @Test
    void testCreatePlan_ExcludedColumn() {
        TableMetadata table = createTable("users", "id", "created_at", "email");
        
        List<ColumnAnonymizationPlan> plans = planner.createPlan(table);
        
        assertEquals(1, plans.size());
        assertEquals("email", plans.get(0).getColumn().getName());
    }

    @Test
    void testCreatePlan_PrimaryKeyExcluded() {
        TableMetadata table = createTable("users", "email", "phone");
        table.setPrimaryKeys(List.of("email"));
        
        List<ColumnAnonymizationPlan> plans = planner.createPlan(table);
        
        assertEquals(1, plans.size());
        assertEquals("phone", plans.get(0).getColumn().getName());
    }

    @Test
    void testCreatePlan_MultipleColumns() {
        TableMetadata table = createTable("customers", "id", "email", "phone", "surname");
        
        List<ColumnAnonymizationPlan> plans = planner.createPlan(table);
        
        assertTrue(plans.size() >= 2, "Should have at least 2 plans, but got: " + plans.size());
        assertTrue(plans.stream().anyMatch(p -> p.getType() == AnonymizationType.EMAIL), "Should have EMAIL type");
        assertTrue(plans.stream().anyMatch(p -> p.getType() == AnonymizationType.PHONE), "Should have PHONE type");
        assertTrue(plans.stream().anyMatch(p -> p.getType() == AnonymizationType.SURNAME), "Should have SURNAME type");
    }

    @Test
    void testCreatePlan_AddressFields() {
        TableMetadata table = createTable("addresses", "id", "street", "city", "country");
        
        List<ColumnAnonymizationPlan> plans = planner.createPlan(table);
        
        assertEquals(3, plans.size());
        assertTrue(plans.stream().anyMatch(p -> p.getType() == AnonymizationType.STREET));
        assertTrue(plans.stream().anyMatch(p -> p.getType() == AnonymizationType.CITY));
        assertTrue(plans.stream().anyMatch(p -> p.getType() == AnonymizationType.COUNTRY));
    }

    @Test
    void testCreatePlan_NullConfig() {
        AnonymizationPlanner nullPlanner = new AnonymizationPlanner(null);
        TableMetadata table = createTable("users", "email");
        
        List<ColumnAnonymizationPlan> plans = nullPlanner.createPlan(table);
        
        assertEquals(0, plans.size());
    }

    @Test
    void testCreatePlan_CaseInsensitiveMatching() {
        TableMetadata table = createTable("users", "ID", "EMAIL", "Phone");
        
        List<ColumnAnonymizationPlan> plans = planner.createPlan(table);
        
        assertEquals(2, plans.size());
        assertTrue(plans.stream().anyMatch(p -> p.getColumn().getName().equals("EMAIL")));
        assertTrue(plans.stream().anyMatch(p -> p.getColumn().getName().equals("Phone")));
    }

    @Test
    void testCreatePlan_NameWithExcludedTable() {
        Name nameConfig = new Name();
        nameConfig.setFilter(List.of("name"));
        nameConfig.setTableNameSearchTerms(List.of("users"));
        nameConfig.setExcludedTableNameSearchTerms(List.of("admin"));
        
        SearchColumnTerms terms = new SearchColumnTerms();
        terms.setName(nameConfig);
        
        Config customConfig = new Config();
        customConfig.setSearchColumnTerms(terms);
        
        AnonymizationPlanner customPlanner = new AnonymizationPlanner(customConfig);
        
        TableMetadata adminTable = createTable("admin_users", "id", "name");
        List<ColumnAnonymizationPlan> plans = customPlanner.createPlan(adminTable);
        
        assertEquals(0, plans.size());
    }

    @Test
    void testCreatePlan_CustomMapping() {
        SearchColumnTerms terms = new SearchColumnTerms();
        terms.setCustoms(List.of("nickname=#{Name.firstName}"));

        Config customConfig = new Config();
        customConfig.setSearchColumnTerms(terms);

        AnonymizationPlanner customPlanner = new AnonymizationPlanner(customConfig);

        TableMetadata table = createTable("users", "id", "nickname");
        List<ColumnAnonymizationPlan> plans = customPlanner.createPlan(table);

        assertEquals(1, plans.size());
        assertEquals(AnonymizationType.CUSTOM, plans.get(0).getType());
        assertEquals("#{Name.firstName}", plans.get(0).getCustomExpression());
    }

    @Test
    void testDefaultConfigurationUsesSpecificTypesAndTokenBoundaries() {
        AnonymizationPlanner defaultPlanner = new AnonymizationPlanner(ConfigUtils.load(null));
        TableMetadata table = createTable("users", "id", "username", "first_name", "last_name", "full_name",
                "national_id", "valid_email", "shipping_address", "source");

        List<ColumnAnonymizationPlan> plans = defaultPlanner.createPlan(table);

        assertPlan(plans, "username", AnonymizationType.USERNAME);
        assertPlan(plans, "first_name", AnonymizationType.NAME);
        assertPlan(plans, "last_name", AnonymizationType.SURNAME);
        assertPlan(plans, "full_name", AnonymizationType.FULL_NAME);
        assertPlan(plans, "national_id", AnonymizationType.NATIONAL_ID);
        assertPlan(plans, "valid_email", AnonymizationType.EMAIL);
        assertPlan(plans, "shipping_address", AnonymizationType.STREET);
        assertTrue(plans.stream().noneMatch(plan -> plan.getColumn().getName().equals("source")));
    }

    @Test
    void testForeignKeyIsProtected() {
        TableMetadata table = createTable("orders", "id", "customer_email", "contact_email");
        ForeignKeyMetadata foreignKey = new ForeignKeyMetadata();
        foreignKey.setFkColumn("customer_email");
        foreignKey.setPkTable("customers");
        table.getForeignKeys().add(foreignKey);

        List<ColumnAnonymizationPlan> plans = planner.createPlan(table);

        assertTrue(plans.stream().noneMatch(plan -> plan.getColumn().getName().equals("customer_email")));
        assertTrue(plans.stream().anyMatch(plan -> plan.getColumn().getName().equals("contact_email")));
    }

    @Test
    void testDefaultConfigurationMapsEveryBuiltInType() {
        Map<String, AnonymizationType> expected = new LinkedHashMap<>();
        expected.put("email", AnonymizationType.EMAIL);
        expected.put("first_name", AnonymizationType.NAME);
        expected.put("last_name", AnonymizationType.SURNAME);
        expected.put("username", AnonymizationType.USERNAME);
        expected.put("phone", AnonymizationType.PHONE);
        expected.put("birth_date", AnonymizationType.BIRTH_DATE);
        expected.put("city", AnonymizationType.CITY);
        expected.put("county", AnonymizationType.COUNTY);
        expected.put("region", AnonymizationType.REGION);
        expected.put("country", AnonymizationType.COUNTRY);
        expected.put("postal_code", AnonymizationType.POSTAL_CODE);
        expected.put("street", AnonymizationType.STREET);
        expected.put("ssn", AnonymizationType.SSN);
        expected.put("passport_number", AnonymizationType.PASSPORT_NUMBER);
        expected.put("driver_license", AnonymizationType.DRIVER_LICENSE);
        expected.put("tax_id", AnonymizationType.TAX_ID);
        expected.put("national_id", AnonymizationType.NATIONAL_ID);
        expected.put("credit_card", AnonymizationType.CREDIT_CARD);
        expected.put("iban", AnonymizationType.IBAN);
        expected.put("bank_account", AnonymizationType.BANK_ACCOUNT);
        expected.put("company_name", AnonymizationType.COMPANY_NAME);
        expected.put("job_title", AnonymizationType.JOB_TITLE);
        expected.put("department", AnonymizationType.DEPARTMENT);
        expected.put("ip_address", AnonymizationType.IP_ADDRESS);
        expected.put("mac_address", AnonymizationType.MAC_ADDRESS);
        expected.put("url", AnonymizationType.URL);
        expected.put("domain", AnonymizationType.DOMAIN);
        expected.put("full_name", AnonymizationType.FULL_NAME);
        expected.put("gender", AnonymizationType.GENDER);
        expected.put("blood_type", AnonymizationType.BLOOD_TYPE);

        List<String> columns = new ArrayList<>();
        columns.add("id");
        columns.addAll(expected.keySet());
        TableMetadata table = createTable("users", columns.toArray(String[]::new));

        List<ColumnAnonymizationPlan> plans = new AnonymizationPlanner(ConfigUtils.load(null)).createPlan(table);

        assertEquals(expected.size(), plans.size());
        expected.forEach((column, type) -> assertPlan(plans, column, type));
    }

    private void assertPlan(List<ColumnAnonymizationPlan> plans, String columnName, AnonymizationType type) {
        assertTrue(plans.stream().anyMatch(plan -> plan.getColumn().getName().equals(columnName)
                && plan.getType() == type), () -> "Missing plan " + columnName + " -> " + type);
    }

    private TableMetadata createTable(String tableName, String... columnNames) {
        TableMetadata table = new TableMetadata();
        table.setName(tableName);
        table.setPrimaryKeys(List.of("id"));
        
        List<ColumnMetadata> columns = new ArrayList<>();
        for (String colName : columnNames) {
            ColumnMetadata col = new ColumnMetadata();
            col.setName(colName);
            col.setTypeName("VARCHAR");
            columns.add(col);
        }
        table.setColumns(columns);
        
        return table;
    }
}
