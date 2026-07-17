/* Created on 19.09.2025 */
package org.javerland.dataanonymizer.model;

/**
 * Foreign key metadata model class.
 *
 * @author Juraj Pacolt
 */
public class ForeignKeyMetadata {

    private String fkCatalog;
    private String fkSchema;
    private String fkColumn;
    private String pkCatalog;
    private String pkSchema;
    private String pkTable;
    private String pkColumn;

    public String getFkCatalog() {
        return fkCatalog;
    }

    public void setFkCatalog(String fkCatalog) {
        this.fkCatalog = fkCatalog;
    }

    public String getFkSchema() {
        return fkSchema;
    }

    public void setFkSchema(String fkSchema) {
        this.fkSchema = fkSchema;
    }

    public String getFkColumn() {
        return fkColumn;
    }

    public void setFkColumn(String fkColumn) {
        this.fkColumn = fkColumn;
    }

    public String getPkCatalog() {
        return pkCatalog;
    }

    public void setPkCatalog(String pkCatalog) {
        this.pkCatalog = pkCatalog;
    }

    public String getPkSchema() {
        return pkSchema;
    }

    public void setPkSchema(String pkSchema) {
        this.pkSchema = pkSchema;
    }

    public String getPkTable() {
        return pkTable;
    }

    public void setPkTable(String pkTable) {
        this.pkTable = pkTable;
    }

    public String getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(String pkColumn) {
        this.pkColumn = pkColumn;
    }

    public String getReferencedTableKey() {
        return TableMetadata.key(pkCatalog, pkSchema, pkTable);
    }
}
