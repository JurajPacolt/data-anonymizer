/* Created on 19.09.2025 */
package org.javerland.dataanonymizer.model;

/**
 * Foreign key metadata model class.
 *
 * @author Juraj Pacolt
 */
public class ForeignKeyMetadata {

    private String fkColumn;
    private String pkTable;
    private String pkColumn;

    public String getFkColumn() {
        return fkColumn;
    }

    public void setFkColumn(String fkColumn) {
        this.fkColumn = fkColumn;
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
}
