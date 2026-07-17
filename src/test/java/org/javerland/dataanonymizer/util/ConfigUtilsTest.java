package org.javerland.dataanonymizer.util;

import org.javerland.dataanonymizer.model.config.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigUtilsTest {

    @TempDir
    Path tempDirectory;

    @Test
    void mergesCustomConfigurationWithDefaults() throws IOException {
        Path configFile = tempDirectory.resolve("config.json");
        Files.writeString(configFile, """
                {
                  "searchColumnTerms": {
                    "email": ["business_email"]
                  }
                }
                """);

        Config config = ConfigUtils.load(configFile.toString());

        assertEquals(java.util.List.of("business_email"), config.getSearchColumnTerms().getEmail());
        assertTrue(config.getSearchColumnTerms().getSurname().contains("surname"));
    }

    @Test
    void rejectsInvalidRegularExpression() throws IOException {
        Path configFile = tempDirectory.resolve("invalid.json");
        Files.writeString(configFile, """
                {
                  "searchColumnTerms": {
                    "email": ["regex:["]
                  }
                }
                """);

        assertThrows(IllegalArgumentException.class, () -> ConfigUtils.load(configFile.toString()));
    }

    @Test
    void emptyArrayReplacesOnlyTheSelectedDefaultCategory() throws IOException {
        Path configFile = tempDirectory.resolve("disable-email.json");
        Files.writeString(configFile, """
                {
                  "searchColumnTerms": {
                    "email": []
                  }
                }
                """);

        Config config = ConfigUtils.load(configFile.toString());

        assertTrue(config.getSearchColumnTerms().getEmail().isEmpty());
        assertFalse(config.getSearchColumnTerms().getSurname().isEmpty());
    }

    @Test
    void blankFileNameLoadsBuiltInConfiguration() {
        Config config = ConfigUtils.load("");

        assertTrue(config.getSearchColumnTerms().getEmail().contains("email"));
    }

    @Test
    void reportsMissingConfigurationFile() {
        Path missing = tempDirectory.resolve("missing.json");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ConfigUtils.load(missing.toString()));

        assertTrue(error.getMessage().contains("Cannot read configuration file"));
    }

    @Test
    void rejectsMalformedCustomMappingLoadedFromJson() throws IOException {
        Path configFile = tempDirectory.resolve("invalid-custom.json");
        Files.writeString(configFile, """
                {
                  "searchColumnTerms": {
                    "customs": ["nickname"]
                  }
                }
                """);

        assertThrows(IllegalArgumentException.class, () -> ConfigUtils.load(configFile.toString()));
    }
}
