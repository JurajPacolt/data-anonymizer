package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.config.Address;
import org.javerland.dataanonymizer.model.config.Config;
import org.javerland.dataanonymizer.model.config.Name;
import org.javerland.dataanonymizer.model.config.SearchColumnTerms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigValidatorTest {

    @Test
    void rejectsMissingRootConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> ConfigValidator.validate(null));
        assertThrows(IllegalArgumentException.class, () -> ConfigValidator.validate(new Config()));
    }

    @Test
    void rejectsBlankPatternsInNestedConfiguration() {
        SearchColumnTerms terms = new SearchColumnTerms();
        Name name = new Name();
        name.setFilter(List.of(" "));
        terms.setName(name);
        Address address = new Address();
        address.setCity(List.of("city"));
        terms.setAddress(address);

        assertThrows(IllegalArgumentException.class, () -> ConfigValidator.validate(config(terms)));
    }

    @Test
    void rejectsMalformedRegularExpression() {
        SearchColumnTerms terms = new SearchColumnTerms();
        terms.setEmail(List.of("regex:["));

        assertThrows(IllegalArgumentException.class, () -> ConfigValidator.validate(config(terms)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "nickname", "nickname=", "=>constant" })
    void rejectsMalformedCustomMappings(String mapping) {
        SearchColumnTerms terms = new SearchColumnTerms();
        terms.setCustoms(Arrays.asList(mapping));

        assertThrows(IllegalArgumentException.class, () -> ConfigValidator.validate(config(terms)));
    }

    @Test
    void acceptsExactGlobRegexAndBothCustomSeparators() {
        SearchColumnTerms terms = new SearchColumnTerms();
        terms.setEmail(List.of("email", "legacy_*", "regex:^mail_[0-9]+$"));
        terms.setCustoms(List.of("nickname=#{Name.firstName}", "alias=>#{Name.lastName}"));

        assertDoesNotThrow(() -> ConfigValidator.validate(config(terms)));
    }

    private Config config(SearchColumnTerms terms) {
        Config config = new Config();
        config.setSearchColumnTerms(terms);
        return config;
    }
}
