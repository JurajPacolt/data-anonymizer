/* Created 17.09.2025 */
package org.javerland.dataanonymizer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.IOUtils;
import org.javerland.dataanonymizer.model.config.Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Utility class for loading and handling configuration settings.
 *
 * @author Juraj Pacolt
 */
public class ConfigUtils {

    @SuppressWarnings("deprecation")
    public static Config load(String fileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            String defaultConfigJson = String.join("\n",
                    IOUtils.readLines(ConfigUtils.class.getResourceAsStream("/default-config.json"),
                            StandardCharsets.UTF_8));

            Config config;

            if (fileName == null || fileName.isEmpty()) {
                config = gson.fromJson(defaultConfigJson, Config.class);
            } else {
                String customConfigJson = String.join("\n",
                        IOUtils.readLines(new FileInputStream(fileName), StandardCharsets.UTF_8));

                JsonReader readerDefault = new JsonReader(new StringReader(defaultConfigJson));
                readerDefault.setLenient(true);
                JsonReader readerCustom = new JsonReader(new StringReader(customConfigJson));
                readerCustom.setLenient(true);

                Map<String, Object> defaultConfigMap = gson.fromJson(readerDefault, Map.class);
                Map<String, Object> customConfigMap = gson.fromJson(readerCustom, Map.class);

                Map<String, Object> mergedMap = mergeConfigs(defaultConfigMap, customConfigMap);

                String mergedJson = gson.toJson(mergedMap);
                config = gson.fromJson(mergedJson, Config.class);
            }

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
