package dev.themeinerlp.minecraftotel.paper.api;

import dev.themeinerlp.minecraftotel.api.core.MinecraftOtelApi;
import dev.themeinerlp.minecraftotel.api.service.TelemetryService;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;

/**
 * Paper-specific implementation of the MinecraftOTEL API.
 */
public final class PaperMinecraftOtelApi implements MinecraftOtelApi {
    private final String version;
    private final TelemetryService telemetryService;

    /**
     * Creates a Paper API implementation with a default version.
     *
     * @param version plugin version
     * @param telemetryService telemetry service instance
     */
    public PaperMinecraftOtelApi(String version, TelemetryService telemetryService) {
        this.version = normalizeVersion(version);
        this.telemetryService = telemetryService;
    }

    @Override
    public String getPlatform() {
        return "paper";
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public TelemetryService getTelemetryService() {
        return telemetryService;
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
