package dev.themeinerlp.minecraftotel.paper.snapshot;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshotBuilder;
import java.util.Map;

/**
 * Builder for Paper telemetry snapshots.
 */
public final class PaperTelemetrySnapshotBuilder implements TelemetrySnapshotBuilder {
    private Long playersOnline;
    private Map<String, Long> entitiesLoadedByWorld;
    private Map<String, Long> entitiesLoadedByType;
    private Map<PaperTelemetrySnapshot.ChunkEntityKey, Long> entitiesLoadedByTypeAndChunk;
    private Map<String, Long> chunksLoadedByWorld;
    private Long exclusiveChunksLoaded;
    private double[] tpsNullable;
    private Double msptAvgNullable;
    private Double msptP95Nullable;

    /**
     * Sets the online player count.
     *
     * @param playersOnline online players
     * @return builder
     */
    public PaperTelemetrySnapshotBuilder setPlayersOnline(long playersOnline) {
        this.playersOnline = playersOnline;
        return this;
    }

    /**
     * Sets entities loaded per world.
     *
     * @param entitiesLoadedByWorld entities per world
     * @return builder
     */
    public PaperTelemetrySnapshotBuilder setEntitiesLoadedByWorld(Map<String, Long> entitiesLoadedByWorld) {
        this.entitiesLoadedByWorld = entitiesLoadedByWorld;
        return this;
    }

    /**
     * Sets entities loaded per type.
     *
     * @param entitiesLoadedByType entities per type
     * @return builder
     */
    public PaperTelemetrySnapshotBuilder setEntitiesLoadedByType(Map<String, Long> entitiesLoadedByType) {
        this.entitiesLoadedByType = entitiesLoadedByType;
        return this;
    }

    /**
     * Sets entities loaded per type and chunk.
     *
     * @param entitiesLoadedByTypeAndChunk entities per type and chunk
     * @return builder
     */
    public PaperTelemetrySnapshotBuilder setEntitiesLoadedByTypeAndChunk(
            Map<PaperTelemetrySnapshot.ChunkEntityKey, Long> entitiesLoadedByTypeAndChunk
    ) {
        this.entitiesLoadedByTypeAndChunk = entitiesLoadedByTypeAndChunk;
        return this;
    }

    /**
     * Sets chunks loaded per world.
     *
     * @param chunksLoadedByWorld chunks per world
     * @return builder
     */
    public PaperTelemetrySnapshotBuilder setChunksLoadedByWorld(Map<String, Long> chunksLoadedByWorld) {
        this.chunksLoadedByWorld = chunksLoadedByWorld;
        return this;
    }

    /**
     * Sets the number of chunks visible to exactly one player.
     *
     * @param exclusiveChunksLoaded exclusive chunk count
     * @return builder
     */
    public PaperTelemetrySnapshotBuilder setExclusiveChunksLoaded(long exclusiveChunksLoaded) {
        this.exclusiveChunksLoaded = exclusiveChunksLoaded;
        return this;
    }

    /**
     * Sets TPS sample windows.
     *
     * @param tpsNullable TPS samples or null
     * @return builder
     */
    public PaperTelemetrySnapshotBuilder setTps(double[] tpsNullable) {
        this.tpsNullable = tpsNullable;
        return this;
    }

    /**
     * Sets average MSPT.
     *
     * @param msptAvgNullable average MSPT or null
     * @return builder
     */
    public PaperTelemetrySnapshotBuilder setMsptAvg(Double msptAvgNullable) {
        this.msptAvgNullable = msptAvgNullable;
        return this;
    }

    /**
     * Sets p95 MSPT.
     *
     * @param msptP95Nullable p95 MSPT or null
     * @return builder
     */
    public PaperTelemetrySnapshotBuilder setMsptP95(Double msptP95Nullable) {
        this.msptP95Nullable = msptP95Nullable;
        return this;
    }

    /**
     * Builds the immutable Paper snapshot.
     *
     * @return snapshot
     */
    public PaperTelemetrySnapshot build() {
        return new PaperTelemetrySnapshot(
                playersOnline == null ? 0L : playersOnline,
                entitiesLoadedByWorld,
                entitiesLoadedByType,
                entitiesLoadedByTypeAndChunk,
                chunksLoadedByWorld == null ? Map.of() : chunksLoadedByWorld,
                exclusiveChunksLoaded == null ? 0L : exclusiveChunksLoaded,
                tpsNullable,
                msptAvgNullable,
                msptP95Nullable
        );
    }
}
