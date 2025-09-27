/* Created on 27.09.2025 */
package org.javerland.dataanonymizer.model.config;

import java.util.List;

/**
 * Model class representing a name configuration.
 *
 * @author Juraj Pacolt
 */
public class Name {

    private List<String> tableNameSearchTerms = List.of();
    private List<String> excludedTableNameSearchTerms = List.of();
    private List<String> filter = List.of();

    public List<String> getTableNameSearchTerms() {
        return tableNameSearchTerms;
    }

    public void setTableNameSearchTerms(List<String> tableNameSearchTerms) {
        this.tableNameSearchTerms = tableNameSearchTerms;
    }

    public List<String> getExcludedTableNameSearchTerms() {
        return excludedTableNameSearchTerms;
    }

    public void setExcludedTableNameSearchTerms(List<String> excludedTableNameSearchTerms) {
        this.excludedTableNameSearchTerms = excludedTableNameSearchTerms;
    }

    public List<String> getFilter() {
        return filter;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
    }
}
