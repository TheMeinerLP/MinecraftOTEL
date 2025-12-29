package dev.themeinerlp.minecraftotel.api.core;

import dev.themeinerlp.minecraftotel.api.service.TelemetryService;
import io.opentelemetry.api.metrics.Meter;

/**
 * Public API for obtaining OpenTelemetry meters from MinecraftOTEL.
 *
 * @since 1.1.0
 * @version 1.1.0
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
     * Returns the telemetry service used by this platform.
     *
     * @return telemetry service
     */
    TelemetryService getTelemetryService();

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
