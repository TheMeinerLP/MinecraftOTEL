package dev.themeinerlp.minecraftotel.velocity.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.slf4j.Logger;

/**
 * Represents the Velocity plugin configuration loaded from velocity.properties.
 */
public final class VelocityPluginConfig {
    public static final String CONFIG_FILE_NAME = "velocity.properties";
    public static final String KEY_INTERVAL_SECONDS = "sampling.intervalSeconds";
    public static final String KEY_ENABLE_PLAYERS_PER_SERVER = "otel.enable.playersPerServer";
    public static final String KEY_ENABLE_SERVER_COUNT = "otel.enable.serverCount";

    public final int intervalSeconds;
    public final boolean enablePlayersPerServer;
    public final boolean enableServerCount;

    private VelocityPluginConfig(
            int intervalSeconds,
            boolean enablePlayersPerServer,
            boolean enableServerCount
    ) {
        this.intervalSeconds = intervalSeconds;
        this.enablePlayersPerServer = enablePlayersPerServer;
        this.enableServerCount = enableServerCount;
    }

    /**
     * Loads configuration from velocity.properties, writing defaults when missing.
     *
     * @param dataDirectory plugin data directory
     * @param logger logger for warnings
     * @param classLoader class loader to read default resources
     * @return loaded config with sane defaults applied
     */
    public static VelocityPluginConfig load(
            Path dataDirectory,
            Logger logger,
            ClassLoader classLoader
    ) {
        Path configPath = dataDirectory.resolve(CONFIG_FILE_NAME);
        ensureDefaultConfig(configPath, logger, classLoader);

        Properties properties = new Properties();
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
            } catch (IOException exception) {
                logger.warn("Failed to read {}, using defaults.", CONFIG_FILE_NAME, exception);
            }
        }

        int intervalSeconds = clamp(
                parseInt(properties.getProperty(KEY_INTERVAL_SECONDS), 1),
                1,
                60
        );
        boolean enablePlayersPerServer = parseBoolean(
                properties.getProperty(KEY_ENABLE_PLAYERS_PER_SERVER),
                true
        );
        boolean enableServerCount = parseBoolean(
                properties.getProperty(KEY_ENABLE_SERVER_COUNT),
                true
        );

        return new VelocityPluginConfig(
                intervalSeconds,
                enablePlayersPerServer,
                enableServerCount
        );
    }

    private static void ensureDefaultConfig(
            Path configPath,
            Logger logger,
            ClassLoader classLoader
    ) {
        if (Files.exists(configPath)) {
            return;
        }
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException exception) {
            logger.warn("Failed to create config directory for Velocity plugin.", exception);
            return;
        }

        try (InputStream input = classLoader.getResourceAsStream(CONFIG_FILE_NAME)) {
            if (input == null) {
                logger.warn("Default {} not found in resources.", CONFIG_FILE_NAME);
                return;
            }
            Files.copy(input, configPath);
        } catch (IOException exception) {
            logger.warn("Failed to write default {}.", CONFIG_FILE_NAME, exception);
        }
    }

    private static int parseInt(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
