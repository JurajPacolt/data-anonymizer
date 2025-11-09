/* Created on 09.11.2025 */
package org.javerland.dataanonymizer.anonymizer;

import org.javerland.dataanonymizer.model.ColumnMetadata;

/**
 * Model class representing an anonymization plan for a column.
 *
 * @author Juraj Pacolt
 */
public class ColumnAnonymizationPlan {

    private ColumnMetadata column;
    private AnonymizationType type;
    private String customExpression;

    public ColumnAnonymizationPlan(ColumnMetadata column, AnonymizationType type) {
        this.column = column;
        this.type = type;
    }

    public ColumnAnonymizationPlan(ColumnMetadata column, AnonymizationType type, String customExpression) {
        this.column = column;
        this.type = type;
        this.customExpression = customExpression;
    }

    public ColumnMetadata getColumn() {
        return column;
    }

    public void setColumn(ColumnMetadata column) {
        this.column = column;
    }

    public AnonymizationType getType() {
        return type;
    }

    public void setType(AnonymizationType type) {
        this.type = type;
    }

    public String getCustomExpression() {
        return customExpression;
    }

    public void setCustomExpression(String customExpression) {
        this.customExpression = customExpression;
    }
}
