package dev.themeinerlp.minecraftotel.velocity.metrics;

import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import dev.themeinerlp.minecraftotel.api.sampler.TelemetrySampler;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;
import dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshot;
import io.opentelemetry.api.common.Attributes;
import java.util.HashMap;
import java.util.Map;

/**
 * Emits standard MinecraftOTEL metrics for Velocity snapshots.
 */
public final class VelocityStandardSnapshotTelemetrySampler implements TelemetrySampler {
    private final boolean recordServerCount;

    /**
     * Creates a sampler for Velocity snapshots.
     *
     * @param recordServerCount whether to record the registered server count on every sample
     * @since 1.6.1
     */
    public VelocityStandardSnapshotTelemetrySampler(boolean recordServerCount) {
        this.recordServerCount = recordServerCount;
    }

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

        collector.replaceLongGauge(
                StandardMetrics.PROXY_PLAYERS_ONLINE,
                perServerValues(velocitySnapshot.playersByServer()),
                StandardMetrics.UNIT_COUNT
        );

        if (recordServerCount) {
            collector.recordLongGauge(
                    StandardMetrics.PROXY_SERVERS_REGISTERED,
                    velocitySnapshot.registeredServers(),
                    StandardMetrics.UNIT_COUNT,
                    Attributes.empty()
            );
        }
    }

    private static Map<Attributes, Long> perServerValues(Map<String, Long> playersByServer) {
        Map<Attributes, Long> values = new HashMap<>();
        for (Map.Entry<String, Long> entry : playersByServer.entrySet()) {
            values.put(Attributes.of(StandardMetrics.SERVER_KEY, entry.getKey()), entry.getValue());
        }
        return values;
    }
}
