package dev.themeinerlp.minecraftotel.paper.metrics;

import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import dev.themeinerlp.minecraftotel.api.sampler.TelemetrySampler;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;
import dev.themeinerlp.minecraftotel.paper.snapshot.PaperTelemetrySnapshot;
import dev.themeinerlp.minecraftotel.paper.snapshot.PaperTelemetrySnapshot.ChunkEntityKey;
import io.opentelemetry.api.common.Attributes;
import java.util.Map;

/**
 * Emits standard MinecraftOTEL metrics for Paper snapshots.
 */
public final class PaperStandardSnapshotTelemetrySampler implements TelemetrySampler {
    @Override
    public void sample(TelemetrySnapshot snapshot, TelemetryCollector collector) {
        if (snapshot == null || collector == null) {
            return;
        }

        if (!(snapshot instanceof PaperTelemetrySnapshot paperSnapshot)) {
            return;
        }

        collector.recordLongGauge(
                StandardMetrics.PLAYERS_ONLINE,
                paperSnapshot.playersOnline(),
                StandardMetrics.UNIT_COUNT,
                Attributes.empty()
        );

        paperSnapshot.entitiesLoadedByWorld().ifPresent(entitiesByWorld -> {
            for (Map.Entry<String, Long> entry : entitiesByWorld.entrySet()) {
                collector.recordLongGauge(
                        StandardMetrics.ENTITIES_LOADED,
                        entry.getValue(),
                        StandardMetrics.UNIT_COUNT,
                        Attributes.of(StandardMetrics.WORLD_KEY, entry.getKey())
                );
            }
        });

        paperSnapshot.entitiesLoadedByType().ifPresent(entitiesByType -> {
            for (Map.Entry<String, Long> entry : entitiesByType.entrySet()) {
                collector.recordLongGauge(
                        StandardMetrics.ENTITIES_LOADED_BY_TYPE,
                        entry.getValue(),
                        StandardMetrics.UNIT_COUNT,
                        Attributes.of(StandardMetrics.ENTITY_TYPE_KEY, entry.getKey())
                );
            }
        });

        paperSnapshot.entitiesLoadedByTypeAndChunk().ifPresent(entitiesByTypeAndChunk -> {
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

        for (Map.Entry<String, Long> entry : paperSnapshot.chunksLoadedByWorld().entrySet()) {
            collector.recordLongGauge(
                    StandardMetrics.CHUNKS_LOADED,
                    entry.getValue(),
                    StandardMetrics.UNIT_COUNT,
                    Attributes.of(StandardMetrics.WORLD_KEY, entry.getKey())
            );
        }

        long totalChunksLoaded = 0L;
        for (long value : paperSnapshot.chunksLoadedByWorld().values()) {
            totalChunksLoaded += value;
        }
        final long totalChunksLoadedFinal = totalChunksLoaded;

        paperSnapshot.entitiesLoadedByWorld().ifPresent(entitiesByWorld -> {
            long totalEntitiesLoaded = 0L;
            for (long value : entitiesByWorld.values()) {
                totalEntitiesLoaded += value;
            }
            if (totalChunksLoadedFinal > 0L) {
                collector.recordDoubleGauge(
                        StandardMetrics.ENTITIES_PER_CHUNK,
                        totalEntitiesLoaded / (double) totalChunksLoadedFinal,
                        StandardMetrics.UNIT_COUNT,
                        Attributes.empty()
                );
            }
        });

        long exclusiveChunksLoaded = paperSnapshot.exclusiveChunksLoaded();
        double chunksPerPlayer = totalChunksLoadedFinal > 0L
                ? exclusiveChunksLoaded / (double) totalChunksLoadedFinal
                : 0.0d;
        collector.recordDoubleGauge(
                StandardMetrics.CHUNKS_LOADED_PER_PLAYER,
                chunksPerPlayer,
                StandardMetrics.UNIT_COUNT,
                Attributes.empty()
        );

        double[] tps = paperSnapshot.tpsNullable();
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

        Double msptAvg = paperSnapshot.msptAvgNullable();
        if (msptAvg != null) {
            collector.recordDoubleGauge(
                    StandardMetrics.SERVER_MSPT_AVG,
                    msptAvg,
                    StandardMetrics.UNIT_MILLIS,
                    Attributes.empty()
            );
        }

        Double msptP95 = paperSnapshot.msptP95Nullable();
        if (msptP95 != null) {
            collector.recordDoubleGauge(
                    StandardMetrics.SERVER_MSPT_P95,
                    msptP95,
                    StandardMetrics.UNIT_MILLIS,
                    Attributes.empty()
            );
        }
    }
}
