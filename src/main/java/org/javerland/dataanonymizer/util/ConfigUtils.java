/* Created 17.09.2025 */
package org.javerland.dataanonymizer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.IOUtils;
import org.javerland.dataanonymizer.model.Config;

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
        // TODO fileName can be null, then load only default config
        // TODO load from config JSON file, read default and nmerge with custom if exists

        String configJson = null;
        try {
            configJson = String.join("\n", IOUtils.readLines(new FileInputStream(fileName), StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalStateException(String.format("Cannot read configuration file: %s", fileName), ex);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonReader reader = new JsonReader(new StringReader(configJson));
        reader.setLenient(true);

        Map<String, Object> configObj = gson.fromJson(reader, Map.class);

        return null; // TODO return new Config(configObj);
    }

}
