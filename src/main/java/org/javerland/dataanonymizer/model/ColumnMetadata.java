/* Created on 19.09.2025 */
package org.javerland.dataanonymizer.model;

/**
 * Column metadata model class.
 *
 * @author Juraj Pacolt
 */
public class ColumnMetadata {

    private String name;
    private String typeName;
    private int size;
    private boolean nullable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
}
