/* Created 17.09.2025 */
package org.javerland.dataanonymizer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.javerland.dataanonymizer.model.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Utility class for loading and handling configuration settings.
 *
 * @author Juraj Pacolt
 */
public class ConfigUtils {

    public static Config load(String fileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            String defaultConfigJson;
            try (InputStream defaultConfig = ConfigUtils.class.getResourceAsStream("/default-config.json")) {
                if (defaultConfig == null) {
                    throw new IllegalStateException("Default configuration resource is missing");
                }
                defaultConfigJson = new String(defaultConfig.readAllBytes(), StandardCharsets.UTF_8);
            }

            Config config;

            if (fileName == null || fileName.isEmpty()) {
                config = gson.fromJson(defaultConfigJson, Config.class);
            } else {
                String customConfigJson = Files.readString(Path.of(fileName), StandardCharsets.UTF_8);

                java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>() { }.getType();
                Map<String, Object> defaultConfigMap = gson.fromJson(defaultConfigJson, mapType);
                Map<String, Object> customConfigMap = gson.fromJson(customConfigJson, mapType);

                Map<String, Object> mergedMap = mergeConfigs(defaultConfigMap, customConfigMap);

                String mergedJson = gson.toJson(mergedMap);
                config = gson.fromJson(mergedJson, Config.class);
            }

            ConfigValidator.validate(config);
            return config;
        } catch (IOException ex) {
            throw new IllegalStateException(
                    String.format("Cannot read configuration file: %s", fileName != null ? fileName : "default"), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mergeConfigs(Map<String, Object> defaultConfig, Map<String, Object> customConfig) {
        Map<String, Object> result = new java.util.LinkedHashMap<>(defaultConfig);

        for (Map.Entry<String, Object> entry : customConfig.entrySet()) {
            String key = entry.getKey();
            Object customValue = entry.getValue();

            if (customValue instanceof Map && result.get(key) instanceof Map) {
                result.put(key, mergeConfigs((Map<String, Object>) result.get(key), (Map<String, Object>) customValue));
            } else {
                result.put(key, customValue);
            }
        }

        return result;
    }

}
