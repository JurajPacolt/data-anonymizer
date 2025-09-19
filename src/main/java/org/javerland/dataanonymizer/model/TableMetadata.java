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

    private String schema;
    private String name;
    private List<ColumnMetadata> columns = new ArrayList<>();
    private List<String> primaryKeys = new ArrayList<>();
    private List<ForeignKeyMetadata> foreignKeys = new ArrayList<>();

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

    public Set<String> getReferencedTables() {
        Set<String> refs = new LinkedHashSet<>();
        for (ForeignKeyMetadata fk : foreignKeys) {
            refs.add(fk.getPkTable());
        }
        return refs;
    }

    @Override
    public String toString() {
        return "TableMetadata{" + "name='" + name + '\'' + ", pk=" + primaryKeys + ", fk=" + foreignKeys + '}';
    }
}
