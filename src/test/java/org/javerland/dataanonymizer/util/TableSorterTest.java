/* Created on 09.11.2025 */
package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.ForeignKeyMetadata;
import org.javerland.dataanonymizer.model.TableMetadata;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TableSorter class.
 *
 * @author Juraj Pacolt
 */
class TableSorterTest {

    @Test
    void testSortByDependencies_NoDependencies() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        tables.put("users", createTable("users"));
        tables.put("products", createTable("products"));
        
        List<TableMetadata> sorted = TableSorter.sortByDependencies(tables);
        
        assertEquals(2, sorted.size());
        assertTrue(sorted.stream().anyMatch(t -> t.getName().equals("users")));
        assertTrue(sorted.stream().anyMatch(t -> t.getName().equals("products")));
    }

    @Test
    void testSortByDependencies_SimpleDependency() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        
        TableMetadata users = createTable("users");
        TableMetadata orders = createTable("orders");
        addForeignKey(orders, "users");
        
        tables.put("users", users);
        tables.put("orders", orders);
        
        List<TableMetadata> sorted = TableSorter.sortByDependencies(tables);
        
        assertEquals(2, sorted.size());
        assertEquals("orders", sorted.get(0).getName());
        assertEquals("users", sorted.get(1).getName());
    }

    @Test
    void testSortByDependencies_MultipleDependencies() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        
        TableMetadata users = createTable("users");
        TableMetadata products = createTable("products");
        TableMetadata orders = createTable("orders");
        addForeignKey(orders, "users");
        addForeignKey(orders, "products");
        
        tables.put("users", users);
        tables.put("products", products);
        tables.put("orders", orders);
        
        List<TableMetadata> sorted = TableSorter.sortByDependencies(tables);
        
        assertEquals(3, sorted.size());
        int ordersIndex = findTableIndex(sorted, "orders");
        int usersIndex = findTableIndex(sorted, "users");
        int productsIndex = findTableIndex(sorted, "products");
        
        assertTrue(usersIndex > ordersIndex);
        assertTrue(productsIndex > ordersIndex);
    }

    @Test
    void testSortByDependencies_ChainDependency() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        
        TableMetadata countries = createTable("countries");
        TableMetadata cities = createTable("cities");
        addForeignKey(cities, "countries");
        TableMetadata addresses = createTable("addresses");
        addForeignKey(addresses, "cities");
        
        tables.put("countries", countries);
        tables.put("cities", cities);
        tables.put("addresses", addresses);
        
        List<TableMetadata> sorted = TableSorter.sortByDependencies(tables);
        
        assertEquals(3, sorted.size());
        assertEquals("addresses", sorted.get(0).getName());
        assertEquals("cities", sorted.get(1).getName());
        assertEquals("countries", sorted.get(2).getName());
    }

    @Test
    void testSortByDependencies_EmptyMap() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        
        List<TableMetadata> sorted = TableSorter.sortByDependencies(tables);
        
        assertEquals(0, sorted.size());
    }

    @Test
    void testSortByDependencies_SingleTable() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        tables.put("users", createTable("users"));
        
        List<TableMetadata> sorted = TableSorter.sortByDependencies(tables);
        
        assertEquals(1, sorted.size());
        assertEquals("users", sorted.get(0).getName());
    }

    @Test
    void testSortByDependencies_ComplexGraph() {
        Map<String, TableMetadata> tables = new LinkedHashMap<>();
        
        TableMetadata users = createTable("users");
        TableMetadata roles = createTable("roles");
        TableMetadata userRoles = createTable("user_roles");
        addForeignKey(userRoles, "users");
        addForeignKey(userRoles, "roles");
        
        TableMetadata permissions = createTable("permissions");
        TableMetadata rolePermissions = createTable("role_permissions");
        addForeignKey(rolePermissions, "roles");
        addForeignKey(rolePermissions, "permissions");
        
        tables.put("users", users);
        tables.put("roles", roles);
        tables.put("user_roles", userRoles);
        tables.put("permissions", permissions);
        tables.put("role_permissions", rolePermissions);
        
        List<TableMetadata> sorted = TableSorter.sortByDependencies(tables);
        
        assertEquals(5, sorted.size());
        
        int userRolesIndex = findTableIndex(sorted, "user_roles");
        int usersIndex = findTableIndex(sorted, "users");
        int rolesIndex = findTableIndex(sorted, "roles");
        
        assertTrue(usersIndex > userRolesIndex);
        assertTrue(rolesIndex > userRolesIndex);
    }

    private TableMetadata createTable(String name) {
        TableMetadata table = new TableMetadata();
        table.setName(name);
        table.setForeignKeys(new ArrayList<>());
        return table;
    }

    private void addForeignKey(TableMetadata table, String referencedTable) {
        ForeignKeyMetadata fk = new ForeignKeyMetadata();
        fk.setPkTable(referencedTable);
        table.getForeignKeys().add(fk);
    }

    private int findTableIndex(List<TableMetadata> tables, String tableName) {
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).getName().equals(tableName)) {
                return i;
            }
        }
        return -1;
    }
}
