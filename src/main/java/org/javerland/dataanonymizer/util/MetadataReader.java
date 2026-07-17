/* Created on 19.09.2025 */
package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.ColumnMetadata;
import org.javerland.dataanonymizer.model.ForeignKeyMetadata;
import org.javerland.dataanonymizer.model.TableMetadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for reading database metadata.
 *
 * @author Juraj Pacolt
 */
public class MetadataReader {

    /**
     * Load all tables and their metadata from the database connection.
     *
     * @param conn
     *         the database connection
     * @param schemaPattern
     *         the schema pattern to filter tables (can be null)
     * @return a map of catalog/schema-qualified table keys to their metadata
     * @throws SQLException
     *         if a database access error occurs
     */
    public static Map<String, TableMetadata> loadAllTables(Connection conn, String schemaPattern) throws SQLException {
        Map<String, TableMetadata> result = new LinkedHashMap<>();
        DatabaseMetaData meta = conn.getMetaData();
        String catalogPattern = null;
        String effectiveSchemaPattern = schemaPattern;
        if (!meta.supportsSchemasInTableDefinitions() && meta.supportsCatalogsInTableDefinitions()) {
            catalogPattern = schemaPattern != null && !schemaPattern.isBlank() ? schemaPattern : conn.getCatalog();
            effectiveSchemaPattern = null;
        }

        // 1. Tables
        try (ResultSet rs = meta.getTables(catalogPattern, effectiveSchemaPattern, "%", new String[] { "TABLE" })) {
            while (rs.next()) {
                String catalog = rs.getString("TABLE_CAT");
                String schema = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");

                TableMetadata table = new TableMetadata();
                table.setCatalog(catalog);
                table.setSchema(schema);
                table.setName(tableName);

                result.put(table.getKey(), table);
            }
        }

        // 2. Columns
        for (TableMetadata table : result.values()) {
            try (ResultSet rs = meta.getColumns(table.getCatalog(), table.getSchema(), table.getName(), "%")) {
                while (rs.next()) {
                    ColumnMetadata col = new ColumnMetadata();
                    col.setName(rs.getString("COLUMN_NAME"));
                    col.setTypeName(rs.getString("TYPE_NAME"));
                    col.setSize(rs.getInt("COLUMN_SIZE"));
                    col.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);

                    table.getColumns().add(col);
                }
            }
        }

        // 3. Primary keys
        for (TableMetadata table : result.values()) {
            try (ResultSet rs = meta.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getName())) {
                while (rs.next()) {
                    table.getPrimaryKeys().add(rs.getString("COLUMN_NAME"));
                }
            }
        }

        // 4. Single-column unique constraints/indexes
        for (TableMetadata table : result.values()) {
            Map<String, List<String>> indexes = new LinkedHashMap<>();
            try (ResultSet rs = meta.getIndexInfo(table.getCatalog(), table.getSchema(), table.getName(), true, false)) {
                while (rs.next()) {
                    if (rs.getShort("TYPE") == DatabaseMetaData.tableIndexStatistic) {
                        continue;
                    }
                    String indexName = rs.getString("INDEX_NAME");
                    String columnName = rs.getString("COLUMN_NAME");
                    if (indexName != null && columnName != null) {
                        indexes.computeIfAbsent(indexName, ignored -> new ArrayList<>()).add(columnName);
                    }
                }
            }
            indexes.values().stream().filter(columns -> columns.size() == 1)
                    .map(columns -> columns.get(0)).forEach(table.getUniqueColumns()::add);
        }

        // 5. Foreign keys
        for (TableMetadata table : result.values()) {
            try (ResultSet rs = meta.getImportedKeys(table.getCatalog(), table.getSchema(), table.getName())) {
                while (rs.next()) {
                    ForeignKeyMetadata fk = new ForeignKeyMetadata();
                    fk.setFkCatalog(rs.getString("FKTABLE_CAT"));
                    fk.setFkSchema(rs.getString("FKTABLE_SCHEM"));
                    fk.setFkColumn(rs.getString("FKCOLUMN_NAME"));
                    fk.setPkCatalog(rs.getString("PKTABLE_CAT"));
                    fk.setPkSchema(rs.getString("PKTABLE_SCHEM"));
                    fk.setPkTable(rs.getString("PKTABLE_NAME"));
                    fk.setPkColumn(rs.getString("PKCOLUMN_NAME"));
                    table.getForeignKeys().add(fk);
                }
            }
        }

        // Mark parent-side columns referenced by foreign keys. Both sides are
        // protected from anonymization so constraints remain valid.
        for (TableMetadata table : result.values()) {
            for (ForeignKeyMetadata fk : table.getForeignKeys()) {
                TableMetadata referencedTable = result.get(fk.getReferencedTableKey());
                if (referencedTable != null && fk.getPkColumn() != null) {
                    referencedTable.getReferencedKeyColumns().add(fk.getPkColumn());
                }
            }
        }

        return result;
    }
}
