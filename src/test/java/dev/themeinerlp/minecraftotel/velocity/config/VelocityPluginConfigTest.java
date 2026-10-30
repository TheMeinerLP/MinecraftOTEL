package dev.themeinerlp.minecraftotel.velocity.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.helpers.NOPLogger;

class VelocityPluginConfigTest {
    @TempDir
    Path dataDirectory;

    @Test
    void backendHealthIntervalDefaultsToThirtySeconds() throws IOException {
        VelocityPluginConfig config = load("sampling.intervalSeconds=1\n");

        assertEquals(
                30,
                config.backendHealthIntervalSeconds,
                "a missing backendHealth.intervalSeconds must default to 30 seconds"
        );
    }

    @Test
    void backendHealthIntervalIsClampedToFiveAndThreeHundredSeconds() throws IOException {
        assertEquals(
                5,
                load("backendHealth.intervalSeconds=1\n").backendHealthIntervalSeconds,
                "an interval below 5 seconds must be raised to 5 seconds"
        );
        assertEquals(
                300,
                load("backendHealth.intervalSeconds=9999\n").backendHealthIntervalSeconds,
                "an interval above 300 seconds must be lowered to 300 seconds"
        );
    }

    @Test
    void backendHealthCanBeDisabled() throws IOException {
        VelocityPluginConfig config = load("otel.enable.backendHealth=false\n");

        assertFalse(
                config.enableBackendHealth,
                "otel.enable.backendHealth=false must disable the backend probe"
        );
    }

    private VelocityPluginConfig load(String content) throws IOException {
        Files.writeString(dataDirectory.resolve(VelocityPluginConfig.CONFIG_FILE_NAME), content);
        return VelocityPluginConfig.load(dataDirectory, NOPLogger.NOP_LOGGER, getClass().getClassLoader());
    }
}
