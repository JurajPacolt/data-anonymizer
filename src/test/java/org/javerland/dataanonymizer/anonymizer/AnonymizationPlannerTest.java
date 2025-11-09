/* Created on 09.11.2025 */
package org.javerland.dataanonymizer.anonymizer;

import org.javerland.dataanonymizer.model.ColumnMetadata;
import org.javerland.dataanonymizer.model.TableMetadata;
import org.javerland.dataanonymizer.model.config.Address;
import org.javerland.dataanonymizer.model.config.Config;
import org.javerland.dataanonymizer.model.config.Name;
import org.javerland.dataanonymizer.model.config.SearchColumnTerms;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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
        assertTrue(plans.stream().anyMatch(p -> p.getType() == AnonymizationType.PHONE || p.getType() == AnonymizationType.SURNAME), 
                   "Should have either PHONE or SURNAME type");
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
