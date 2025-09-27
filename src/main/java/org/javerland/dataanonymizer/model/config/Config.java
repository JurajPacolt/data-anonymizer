/* Created on 17.09.2025 */
package org.javerland.dataanonymizer.model.config;

import java.util.List;

/**
 * Configuration model class.
 *
 * @author Juraj Pacolt
 */
public class Config {

    private SearchColumnTerms searchColumnTerms;
    private List<String> excludeTableTerms = List.of();
    private List<String> excludeColumnTerms = List.of();

    public SearchColumnTerms getSearchColumnTerms() {
        return searchColumnTerms;
    }

    public void setSearchColumnTerms(SearchColumnTerms searchColumnTerms) {
        this.searchColumnTerms = searchColumnTerms;
    }

    public List<String> getExcludeTableTerms() {
        return excludeTableTerms;
    }

    public void setExcludeTableTerms(List<String> excludeTableTerms) {
        this.excludeTableTerms = excludeTableTerms;
    }

    public List<String> getExcludeColumnTerms() {
        return excludeColumnTerms;
    }

    public void setExcludeColumnTerms(List<String> excludeColumnTerms) {
        this.excludeColumnTerms = excludeColumnTerms;
    }
}
