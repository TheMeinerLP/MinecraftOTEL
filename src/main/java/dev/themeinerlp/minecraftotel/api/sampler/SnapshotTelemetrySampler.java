package dev.themeinerlp.minecraftotel.api.sampler;

import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;
import io.opentelemetry.api.common.Attributes;
import java.util.Map;

/**
 * Emits standard MinecraftOTEL metrics from a TelemetrySnapshot.
 *
 * @since 1.1.0
 * @version 1.1.0
 */
public final class SnapshotTelemetrySampler implements TelemetrySampler {
    @Override
    public void sample(TelemetrySnapshot snapshot, TelemetryCollector collector) {
        if (snapshot == null || collector == null) {
            return;
        }

        collector.recordLongGauge(
                StandardMetrics.PLAYERS_ONLINE,
                snapshot.playersOnline(),
                StandardMetrics.UNIT_COUNT,
                Attributes.empty()
        );

        for (Map.Entry<String, Long> entry : snapshot.entitiesLoadedByWorld().entrySet()) {
            collector.recordLongGauge(
                    StandardMetrics.ENTITIES_LOADED,
                    entry.getValue(),
                    StandardMetrics.UNIT_COUNT,
                    Attributes.of(StandardMetrics.WORLD_KEY, entry.getKey())
            );
        }

        for (Map.Entry<String, Long> entry : snapshot.chunksLoadedByWorld().entrySet()) {
            collector.recordLongGauge(
                    StandardMetrics.CHUNKS_LOADED,
                    entry.getValue(),
                    StandardMetrics.UNIT_COUNT,
                    Attributes.of(StandardMetrics.WORLD_KEY, entry.getKey())
            );
        }

        double[] tps = snapshot.tpsNullable();
        if (tps != null) {
            int limit = Math.min(tps.length, StandardMetrics.TPS_WINDOWS.length);
            for (int i = 0; i < limit; i++) {
                collector.recordDoubleGauge(
                        StandardMetrics.SERVER_TPS,
                        tps[i],
                        StandardMetrics.UNIT_COUNT,
                        Attributes.of(StandardMetrics.WINDOW_KEY, StandardMetrics.TPS_WINDOWS[i])
                );
            }
        }

        Double msptAvg = snapshot.msptAvgNullable();
        if (msptAvg != null) {
            collector.recordDoubleGauge(
                    StandardMetrics.SERVER_MSPT_AVG,
                    msptAvg,
                    StandardMetrics.UNIT_MILLIS,
                    Attributes.empty()
            );
        }

        Double msptP95 = snapshot.msptP95Nullable();
        if (msptP95 != null) {
            collector.recordDoubleGauge(
                    StandardMetrics.SERVER_MSPT_P95,
                    msptP95,
                    StandardMetrics.UNIT_MILLIS,
                    Attributes.empty()
            );
        }

        if (!snapshot.playersByServer().isEmpty()) {
            for (Map.Entry<String, Long> entry : snapshot.playersByServer().entrySet()) {
                collector.recordLongGauge(
                        StandardMetrics.PROXY_PLAYERS_ONLINE,
                        entry.getValue(),
                        StandardMetrics.UNIT_COUNT,
                        Attributes.of(StandardMetrics.SERVER_KEY, entry.getKey())
                );
            }
        }

        if (!snapshot.playersByServer().isEmpty() || snapshot.registeredServers() > 0L) {
            collector.recordLongGauge(
                    StandardMetrics.PROXY_SERVERS_REGISTERED,
                    snapshot.registeredServers(),
                    StandardMetrics.UNIT_COUNT,
                    Attributes.empty()
            );
        }
    }
}
