package dev.themeinerlp.minecraftotel.velocity.metrics;

import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import dev.themeinerlp.minecraftotel.api.sampler.TelemetrySampler;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;
import dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshot;
import io.opentelemetry.api.common.Attributes;
import java.util.Map;

/**
 * Emits standard MinecraftOTEL metrics for Velocity snapshots.
 */
public final class VelocityStandardSnapshotTelemetrySampler implements TelemetrySampler {
    @Override
    public void sample(TelemetrySnapshot snapshot, TelemetryCollector collector) {
        if (snapshot == null || collector == null) {
            return;
        }

        if (!(snapshot instanceof VelocityTelemetrySnapshot velocitySnapshot)) {
            return;
        }

        collector.recordLongGauge(
                StandardMetrics.PLAYERS_ONLINE,
                velocitySnapshot.playersOnline(),
                StandardMetrics.UNIT_COUNT,
                Attributes.empty()
        );

        for (Map.Entry<String, Long> entry : velocitySnapshot.playersByServer().entrySet()) {
            collector.recordLongGauge(
                    StandardMetrics.PROXY_PLAYERS_ONLINE,
                    entry.getValue(),
                    StandardMetrics.UNIT_COUNT,
                    Attributes.of(StandardMetrics.SERVER_KEY, entry.getKey())
            );
        }

        if (!velocitySnapshot.playersByServer().isEmpty() || velocitySnapshot.registeredServers() > 0L) {
            collector.recordLongGauge(
                    StandardMetrics.PROXY_SERVERS_REGISTERED,
                    velocitySnapshot.registeredServers(),
                    StandardMetrics.UNIT_COUNT,
                    Attributes.empty()
            );
        }
    }
}
