/* Created on 19.09.2025 */
package org.javerland.dataanonymizer.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Table metadata model class.
 *
 * @author Juraj Pacolt
 */
public class TableMetadata {

    private String catalog;
    private String schema;
    private String name;
    private List<ColumnMetadata> columns = new ArrayList<>();
    private List<String> primaryKeys = new ArrayList<>();
    private List<ForeignKeyMetadata> foreignKeys = new ArrayList<>();
    private Set<String> referencedKeyColumns = new LinkedHashSet<>();
    private Set<String> uniqueColumns = new LinkedHashSet<>();

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ColumnMetadata> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnMetadata> columns) {
        this.columns = columns;
    }

    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public List<ForeignKeyMetadata> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(List<ForeignKeyMetadata> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public Set<String> getReferencedKeyColumns() {
        return referencedKeyColumns;
    }

    public void setReferencedKeyColumns(Set<String> referencedKeyColumns) {
        this.referencedKeyColumns = referencedKeyColumns;
    }

    public Set<String> getUniqueColumns() {
        return uniqueColumns;
    }

    public void setUniqueColumns(Set<String> uniqueColumns) {
        this.uniqueColumns = uniqueColumns;
    }

    public Set<String> getReferencedTables() {
        Set<String> refs = new LinkedHashSet<>();
        for (ForeignKeyMetadata fk : foreignKeys) {
            refs.add(fk.getReferencedTableKey());
        }
        return refs;
    }

    public String getKey() {
        return key(catalog, schema, name);
    }

    public boolean isPrimaryKey(String columnName) {
        return containsIgnoreCase(primaryKeys, columnName);
    }

    public boolean isForeignKey(String columnName) {
        return foreignKeys.stream().anyMatch(fk -> equalsIgnoreCase(fk.getFkColumn(), columnName));
    }

    public boolean isReferencedKey(String columnName) {
        return containsIgnoreCase(referencedKeyColumns, columnName);
    }

    public boolean isUnique(String columnName) {
        return containsIgnoreCase(uniqueColumns, columnName);
    }

    public boolean isProtectedKey(String columnName) {
        return isPrimaryKey(columnName) || isForeignKey(columnName) || isReferencedKey(columnName);
    }

    public String protectionReason(String columnName) {
        if (isPrimaryKey(columnName)) {
            return "primary key";
        }
        if (isForeignKey(columnName)) {
            return "foreign key";
        }
        if (isReferencedKey(columnName)) {
            return "referenced key";
        }
        return null;
    }

    public static String key(String catalog, String schema, String table) {
        StringBuilder result = new StringBuilder();
        appendKeyPart(result, catalog);
        appendKeyPart(result, schema);
        appendKeyPart(result, table);
        return result.toString();
    }

    private static void appendKeyPart(StringBuilder target, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!target.isEmpty()) {
            target.append('.');
        }
        target.append(value.toLowerCase(java.util.Locale.ROOT));
    }

    private static boolean containsIgnoreCase(Iterable<String> values, String expected) {
        for (String value : values) {
            if (equalsIgnoreCase(value, expected)) {
                return true;
            }
        }
        return false;
    }

    private static boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    @Override
    public String toString() {
        return "TableMetadata{" + "name='" + name + '\'' + ", pk=" + primaryKeys + ", fk=" + foreignKeys + '}';
    }
}
