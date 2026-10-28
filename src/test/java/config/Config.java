package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Reads settings from environment variables first, then from config.properties.
 * Credentials are never hard-coded in the source code.
 */
public final class Config {

    private static final Properties PROPS = new Properties();

    static {
        try (FileInputStream in = new FileInputStream("config.properties")) {
            PROPS.load(in);
        } catch (IOException e) {
            System.out.println("[Config] config.properties not found - using environment variables/defaults only.");
        }
    }

    private Config() { }

    /** Optional setting with a default value. */
    public static String get(String key, String envName, String defaultValue) {
        String env = System.getenv(envName);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String value = PROPS.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        return defaultValue;
    }

    /** Mandatory setting (e.g. credentials). Fails with a clear message if missing. */
    public static String required(String key, String envName) {
        String value = get(key, envName, null);
        if (value == null) {
            throw new IllegalStateException("Missing '" + key + "'. Set it in config.properties "
                    + "(copy config.properties.example) or as environment variable " + envName + ".");
        }
        return value;
    }
}
