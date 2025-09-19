/* Created on 19.09.2025 */
package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.ColumnMetadata;
import org.javerland.dataanonymizer.model.ForeignKeyMetadata;
import org.javerland.dataanonymizer.model.TableMetadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
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
     * @return a map of table names to their metadata
     * @throws SQLException
     *         if a database access error occurs
     */
    public static Map<String, TableMetadata> loadAllTables(Connection conn, String schemaPattern) throws SQLException {
        Map<String, TableMetadata> result = new LinkedHashMap<>();
        DatabaseMetaData meta = conn.getMetaData();

        // 1. Tables
        try (ResultSet rs = meta.getTables(null, schemaPattern, "%", new String[] { "TABLE" })) {
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");

                TableMetadata table = new TableMetadata();
                table.setSchema(schema);
                table.setName(tableName);

                result.put(tableName, table);
            }
        }

        // 2. Columns
        for (TableMetadata table : result.values()) {
            try (ResultSet rs = meta.getColumns(null, schemaPattern, table.getName(), "%")) {
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
            try (ResultSet rs = meta.getPrimaryKeys(null, schemaPattern, table.getName())) {
                while (rs.next()) {
                    table.getPrimaryKeys().add(rs.getString("COLUMN_NAME"));
                }
            }
        }

        // 4. Foreign keys
        for (TableMetadata table : result.values()) {
            try (ResultSet rs = meta.getImportedKeys(null, schemaPattern, table.getName())) {
                while (rs.next()) {
                    ForeignKeyMetadata fk = new ForeignKeyMetadata();
                    fk.setFkColumn(rs.getString("FKCOLUMN_NAME"));
                    fk.setPkTable(rs.getString("PKTABLE_NAME"));
                    fk.setPkColumn(rs.getString("PKCOLUMN_NAME"));
                    table.getForeignKeys().add(fk);
                }
            }
        }

        return result;
    }
}
