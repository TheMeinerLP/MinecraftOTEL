package dev.themeinerlp.minecraftotel.velocity.metrics;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import java.util.Map;
import dev.themeinerlp.minecraftotel.velocity.config.VelocityPluginConfig;
import dev.themeinerlp.minecraftotel.velocity.state.VelocityTelemetryState;
import dev.themeinerlp.minecraftotel.api.TelemetrySnapshot;

/**
 * Registers OpenTelemetry metrics for Velocity telemetry snapshots.
 */
public final class VelocityMetricsRegistry {
    /** Attribute key for backend server names. */
    public static final AttributeKey<String> SERVER_KEY = AttributeKey.stringKey("server");

    /**
     * Creates and registers all Velocity metrics based on the current config.
     *
     * @param version plugin version for instrumentation metadata
     * @param state telemetry state backing metric callbacks
     * @param config feature flags controlling metric registration
     */
    public VelocityMetricsRegistry(
            String version,
            VelocityTelemetryState state,
            VelocityPluginConfig config
    ) {
        Meter meter = GlobalOpenTelemetry.get()
                .meterBuilder("minecraft-otel-velocity")
                .setInstrumentationVersion(version)
                .build();

        meter.gaugeBuilder("minecraft.players.online")
                .setUnit("1")
                .ofLongs()
                .buildWithCallback(measurement -> recordPlayersOnline(measurement, state));

        if (config.enablePlayersPerServer) {
            meter.gaugeBuilder("minecraft.proxy.players.online")
                    .setUnit("1")
                    .ofLongs()
                    .buildWithCallback(measurement -> recordPlayersPerServer(measurement, state));
        }

        if (config.enableServerCount) {
            meter.gaugeBuilder("minecraft.proxy.servers.registered")
                    .setUnit("1")
                    .ofLongs()
                    .buildWithCallback(measurement -> recordServerCount(measurement, state));
        }
    }

    private static void recordPlayersOnline(
            ObservableLongMeasurement measurement,
            VelocityTelemetryState state
    ) {
        TelemetrySnapshot snapshot = state.getSnapshot();
        measurement.record(snapshot.playersOnline());
    }

    private static void recordPlayersPerServer(
            ObservableLongMeasurement measurement,
            VelocityTelemetryState state
    ) {
        TelemetrySnapshot snapshot = state.getSnapshot();
        for (Map.Entry<String, Long> entry : snapshot.playersByServer().entrySet()) {
            measurement.record(entry.getValue(), Attributes.of(SERVER_KEY, entry.getKey()));
        }
    }

    private static void recordServerCount(
            ObservableLongMeasurement measurement,
            VelocityTelemetryState state
    ) {
        TelemetrySnapshot snapshot = state.getSnapshot();
        measurement.record(snapshot.registeredServers());
    }
}
