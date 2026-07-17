package org.javerland.dataanonymizer.anonymizer;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Matches database identifiers on normalized token boundaries. A configured
 * pattern may also use {@code *} as a glob or the {@code regex:} prefix.
 */
final class IdentifierMatcher {

    private IdentifierMatcher() {
    }

    static boolean matchesAnyTerm(String identifier, List<String> patterns) {
        if (identifier == null || patterns == null) {
            return false;
        }
        return patterns.stream().anyMatch(pattern -> matchesTerm(identifier, pattern));
    }

    static boolean matchesAnyExact(String identifier, List<String> patterns) {
        if (identifier == null || patterns == null) {
            return false;
        }
        return patterns.stream().anyMatch(pattern -> matchesExact(identifier, pattern));
    }

    static boolean matchesTerm(String identifier, String configuredPattern) {
        if (configuredPattern == null || configuredPattern.isBlank()) {
            return false;
        }
        if (configuredPattern.startsWith("regex:")) {
            return Pattern.compile(configuredPattern.substring("regex:".length()), Pattern.CASE_INSENSITIVE)
                    .matcher(identifier).find();
        }

        String value = normalize(identifier);
        String pattern = normalize(configuredPattern);
        if (pattern.isEmpty()) {
            return false;
        }
        if (configuredPattern.indexOf('*') >= 0) {
            return globPattern(configuredPattern).matcher(value).matches();
        }

        return value.equals(pattern) || value.startsWith(pattern + "_") || value.endsWith("_" + pattern)
                || value.contains("_" + pattern + "_");
    }

    static boolean matchesExact(String identifier, String configuredPattern) {
        if (identifier == null || configuredPattern == null || configuredPattern.isBlank()) {
            return false;
        }
        if (configuredPattern.startsWith("regex:") || configuredPattern.indexOf('*') >= 0) {
            return matchesTerm(identifier, configuredPattern);
        }
        return normalize(identifier).equals(normalize(configuredPattern));
    }

    static String normalize(String identifier) {
        String separatedCamelCase = identifier.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        String withoutAccents = Normalizer.normalize(separatedCamelCase, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return withoutAccents.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private static Pattern globPattern(String configuredPattern) {
        String[] parts = configuredPattern.split("\\*", -1);
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                regex.append(".*");
            }
            regex.append(Pattern.quote(normalize(parts[i])));
        }
        return Pattern.compile(regex.append('$').toString(), Pattern.CASE_INSENSITIVE);
    }
}
