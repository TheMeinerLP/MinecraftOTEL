package dev.themeinerlp.minecraftotel.velocity.api;

import dev.themeinerlp.minecraftotel.api.MinecraftOtelApi;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;

/**
 * Velocity-specific implementation of the MinecraftOTEL API.
 */
public final class VelocityMinecraftOtelApi implements MinecraftOtelApi {
    private final String version;

    /**
     * Creates a Velocity API implementation with a default version.
     *
     * @param version plugin version
     */
    public VelocityMinecraftOtelApi(String version) {
        this.version = normalizeVersion(version);
    }

    @Override
    public String getPlatform() {
        return "velocity";
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Meter getMeter(String instrumentationName) {
        return getMeter(instrumentationName, version);
    }

    @Override
    public Meter getMeter(String instrumentationName, String instrumentationVersion) {
        return GlobalOpenTelemetry.get()
                .meterBuilder(instrumentationName)
                .setInstrumentationVersion(normalizeVersion(instrumentationVersion))
                .build();
    }

    private static String normalizeVersion(String version) {
        if (version == null || version.isBlank()) {
            return "unknown";
        }
        return version;
    }
}
