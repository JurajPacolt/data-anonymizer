package org.javerland.dataanonymizer.anonymizer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifierMatcherTest {

    @Test
    void matchesTermsOnlyOnIdentifierBoundaries() {
        assertTrue(IdentifierMatcher.matchesAnyTerm("valid_email", List.of("email")));
        assertTrue(IdentifierMatcher.matchesAnyTerm("userEmail", List.of("email")));
        assertFalse(IdentifierMatcher.matchesAnyTerm("shipping_address", List.of("ip")));
        assertFalse(IdentifierMatcher.matchesAnyTerm("source", List.of("rc")));
    }

    @Test
    void exactExclusionsDoNotHideNationalIdentifiers() {
        assertTrue(IdentifierMatcher.matchesAnyExact("id", List.of("id")));
        assertFalse(IdentifierMatcher.matchesAnyExact("national_id", List.of("id")));
        assertFalse(IdentifierMatcher.matchesAnyExact("valid_email", List.of("id")));
    }

    @Test
    void supportsGlobAndRegularExpressionPatterns() {
        assertTrue(IdentifierMatcher.matchesAnyTerm("legacy_user_email", List.of("legacy_*")));
        assertTrue(IdentifierMatcher.matchesAnyTerm("email_2026", List.of("regex:^email_[0-9]{4}$")));
        assertFalse(IdentifierMatcher.matchesAnyTerm("primary_email", List.of("legacy_*")));
    }

    @Test
    void normalizesCamelCaseSeparatorsAndAccents() {
        assertTrue(IdentifierMatcher.matchesTerm("ČísloPasu", "cislo_pasu"));
        assertTrue(IdentifierMatcher.matchesTerm("business-email", "business_email"));
        assertTrue(IdentifierMatcher.matchesExact("Rodné Číslo", "rodne_cislo"));
    }

    @Test
    void safelyRejectsNullAndBlankInputs() {
        assertFalse(IdentifierMatcher.matchesAnyTerm(null, List.of("email")));
        assertFalse(IdentifierMatcher.matchesAnyTerm("email", null));
        assertFalse(IdentifierMatcher.matchesTerm("email", null));
        assertFalse(IdentifierMatcher.matchesTerm("email", " "));
        assertFalse(IdentifierMatcher.matchesExact(null, "email"));
    }

    @Test
    void exactMatchingStillSupportsExplicitAdvancedPatterns() {
        assertTrue(IdentifierMatcher.matchesAnyExact("legacy_id", List.of("legacy_*")));
        assertTrue(IdentifierMatcher.matchesAnyExact("id_42", List.of("regex:^id_[0-9]+$")));
        assertFalse(IdentifierMatcher.matchesAnyExact("national_id", List.of("id")));
    }
}
