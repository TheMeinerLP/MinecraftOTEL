package dev.themeinerlp.minecraftotel.metrics;

import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import dev.themeinerlp.minecraftotel.api.sampler.TelemetrySampler;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;
import dev.themeinerlp.minecraftotel.paper.snapshot.PaperTelemetrySnapshot;
import dev.themeinerlp.minecraftotel.paper.snapshot.PaperTelemetrySnapshot.ChunkEntityKey;
import dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshot;
import io.opentelemetry.api.common.Attributes;
import java.util.Map;

/**
 * Emits standard MinecraftOTEL metrics from platform snapshots.
 */
public final class StandardSnapshotTelemetrySampler implements TelemetrySampler {
    @Override
    public void sample(TelemetrySnapshot snapshot, TelemetryCollector collector) {
        if (snapshot == null || collector == null) {
            return;
        }

        if (snapshot instanceof PaperTelemetrySnapshot paperSnapshot) {
            emitPaperSnapshot(paperSnapshot, collector);
            return;
        }

        if (snapshot instanceof VelocityTelemetrySnapshot velocitySnapshot) {
            emitVelocitySnapshot(velocitySnapshot, collector);
        }
    }

    private void emitPaperSnapshot(PaperTelemetrySnapshot snapshot, TelemetryCollector collector) {
        collector.recordLongGauge(
                StandardMetrics.PLAYERS_ONLINE,
                snapshot.playersOnline(),
                StandardMetrics.UNIT_COUNT,
                Attributes.empty()
        );

        snapshot.entitiesLoadedByWorld().ifPresent(entitiesByWorld -> {
            for (Map.Entry<String, Long> entry : entitiesByWorld.entrySet()) {
                collector.recordLongGauge(
                        StandardMetrics.ENTITIES_LOADED,
                        entry.getValue(),
                        StandardMetrics.UNIT_COUNT,
                        Attributes.of(StandardMetrics.WORLD_KEY, entry.getKey())
                );
            }
        });

        snapshot.entitiesLoadedByType().ifPresent(entitiesByType -> {
            for (Map.Entry<String, Long> entry : entitiesByType.entrySet()) {
                collector.recordLongGauge(
                        StandardMetrics.ENTITIES_LOADED_BY_TYPE,
                        entry.getValue(),
                        StandardMetrics.UNIT_COUNT,
                        Attributes.of(StandardMetrics.ENTITY_TYPE_KEY, entry.getKey())
                );
            }
        });

        snapshot.entitiesLoadedByTypeAndChunk().ifPresent(entitiesByTypeAndChunk -> {
            for (Map.Entry<ChunkEntityKey, Long> entry : entitiesByTypeAndChunk.entrySet()) {
                var key = entry.getKey();
                collector.recordLongGauge(
                        StandardMetrics.ENTITIES_LOADED_BY_TYPE_CHUNK,
                        entry.getValue(),
                        StandardMetrics.UNIT_COUNT,
                        Attributes.of(
                                StandardMetrics.WORLD_KEY,
                                key.worldName(),
                                StandardMetrics.CHUNK_X_KEY,
                                (long) key.chunkX(),
                                StandardMetrics.CHUNK_Z_KEY,
                                (long) key.chunkZ(),
                                StandardMetrics.ENTITY_TYPE_KEY,
                                key.entityType()
                        )
                );
            }
        });

        for (Map.Entry<String, Long> entry : snapshot.chunksLoadedByWorld().entrySet()) {
            collector.recordLongGauge(
                    StandardMetrics.CHUNKS_LOADED,
                    entry.getValue(),
                    StandardMetrics.UNIT_COUNT,
                    Attributes.of(StandardMetrics.WORLD_KEY, entry.getKey())
            );
        }

        snapshot.entitiesLoadedByWorld().ifPresent(entitiesByWorld -> {
            long totalEntitiesLoaded = 0L;
            for (long value : entitiesByWorld.values()) {
                totalEntitiesLoaded += value;
            }
            long totalChunksLoaded = 0L;
            for (long value : snapshot.chunksLoadedByWorld().values()) {
                totalChunksLoaded += value;
            }
            if (totalChunksLoaded > 0L) {
                collector.recordDoubleGauge(
                        StandardMetrics.ENTITIES_PER_CHUNK,
                        totalEntitiesLoaded / (double) totalChunksLoaded,
                        StandardMetrics.UNIT_COUNT,
                        Attributes.empty()
                );
            }
        });

        long exclusiveChunksLoaded = snapshot.exclusiveChunksLoaded();
        double chunksPerPlayer = snapshot.playersOnline() > 0L
                ? exclusiveChunksLoaded / (double) snapshot.playersOnline()
                : 0.0d;
        collector.recordDoubleGauge(
                StandardMetrics.CHUNKS_LOADED_PER_PLAYER,
                chunksPerPlayer,
                StandardMetrics.UNIT_COUNT,
                Attributes.empty()
        );

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
    }

    private void emitVelocitySnapshot(VelocityTelemetrySnapshot snapshot, TelemetryCollector collector) {
        collector.recordLongGauge(
                StandardMetrics.PLAYERS_ONLINE,
                snapshot.playersOnline(),
                StandardMetrics.UNIT_COUNT,
                Attributes.empty()
        );

        for (Map.Entry<String, Long> entry : snapshot.playersByServer().entrySet()) {
            collector.recordLongGauge(
                    StandardMetrics.PROXY_PLAYERS_ONLINE,
                    entry.getValue(),
                    StandardMetrics.UNIT_COUNT,
                    Attributes.of(StandardMetrics.SERVER_KEY, entry.getKey())
            );
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
