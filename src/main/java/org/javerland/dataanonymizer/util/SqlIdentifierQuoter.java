package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.TableMetadata;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Quotes JDBC identifiers using the connected database's own quote string.
 */
final class SqlIdentifierQuoter {

    private final String quote;
    private final boolean catalogsSupported;

    SqlIdentifierQuoter(DatabaseMetaData metadata) throws SQLException {
        String configuredQuote = metadata.getIdentifierQuoteString();
        this.quote = configuredQuote == null ? "" : configuredQuote.trim();
        this.catalogsSupported = metadata.supportsCatalogsInDataManipulation();
    }

    String quote(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("SQL identifier must not be blank");
        }
        if (quote.isEmpty()) {
            return identifier;
        }
        return quote + identifier.replace(quote, quote + quote) + quote;
    }

    String tableName(TableMetadata table) {
        List<String> components = new ArrayList<>();
        if (table.getSchema() != null && !table.getSchema().isBlank()) {
            components.add(table.getSchema());
        } else if (catalogsSupported && table.getCatalog() != null && !table.getCatalog().isBlank()) {
            components.add(table.getCatalog());
        }
        components.add(table.getName());
        return components.stream().map(this::quote).reduce((left, right) -> left + "." + right).orElseThrow();
    }
}
