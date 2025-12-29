package dev.themeinerlp.minecraftotel.paper.snapshot;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshotBuilder;
import java.util.Map;

/**
 * Builder for Paper telemetry snapshots.
 */
public final class PaperTelemetrySnapshotBuilder implements TelemetrySnapshotBuilder {
    private Long playersOnline;
    private Map<String, Long> entitiesLoadedByWorld;
    private Map<String, Long> chunksLoadedByWorld;
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
                chunksLoadedByWorld == null ? Map.of() : chunksLoadedByWorld,
                tpsNullable,
                msptAvgNullable,
                msptP95Nullable
        );
    }
}
