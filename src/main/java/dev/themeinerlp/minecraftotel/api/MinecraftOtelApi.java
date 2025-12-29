package dev.themeinerlp.minecraftotel.api;

import io.opentelemetry.api.metrics.Meter;

/**
 * Public API for obtaining OpenTelemetry meters from MinecraftOTEL.
 */
public interface MinecraftOtelApi {
    /**
     * Returns the platform identifier (for example "paper" or "velocity").
     *
     * @return platform identifier
     */
    String getPlatform();

    /**
     * Returns the plugin version used as a default instrumentation version.
     *
     * @return plugin version
     */
    String getVersion();

    /**
     * Creates a meter with the given instrumentation name and the plugin version.
     *
     * @param instrumentationName instrumentation scope name
     * @return meter instance
     */
    Meter getMeter(String instrumentationName);

    /**
     * Creates a meter with the given instrumentation name and version.
     *
     * @param instrumentationName instrumentation scope name
     * @param instrumentationVersion instrumentation version
     * @return meter instance
     */
    Meter getMeter(String instrumentationName, String instrumentationVersion);
}
