package dev.themeinerlp.minecraftotel.paper.snapshot;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Paper-specific telemetry snapshot data.
 */
public final class PaperTelemetrySnapshot implements TelemetrySnapshot {
    private final long playersOnline;
    private final Map<String, Long> entitiesLoadedByWorld;
    private final Map<String, Long> chunksLoadedByWorld;
    private final double[] tpsNullable;
    private final Double msptAvgNullable;
    private final Double msptP95Nullable;

    public PaperTelemetrySnapshot(
            long playersOnline,
            Map<String, Long> entitiesLoadedByWorld,
            Map<String, Long> chunksLoadedByWorld,
            double[] tpsNullable,
            Double msptAvgNullable,
            Double msptP95Nullable
    ) {
        this.playersOnline = playersOnline;
        this.entitiesLoadedByWorld = entitiesLoadedByWorld == null ? null : Map.copyOf(entitiesLoadedByWorld);
        this.chunksLoadedByWorld = chunksLoadedByWorld == null ? Map.of() : Map.copyOf(chunksLoadedByWorld);
        this.tpsNullable = tpsNullable == null ? null : Arrays.copyOf(tpsNullable, tpsNullable.length);
        this.msptAvgNullable = msptAvgNullable;
        this.msptP95Nullable = msptP95Nullable;
    }

    /**
     * Returns an empty snapshot.
     *
     * @return empty snapshot
     */
    public static PaperTelemetrySnapshot empty() {
        return new PaperTelemetrySnapshot(0L, null, Map.of(), null, null, null);
    }

    /**
     * Returns the total online player count.
     *
     * @return online players
     */
    public long playersOnline() {
        return playersOnline;
    }

    /**
     * Returns entities loaded per world when available.
     *
     * @return entities per world
     */
    public Optional<Map<String, Long>> entitiesLoadedByWorld() {
        return Optional.ofNullable(entitiesLoadedByWorld);
    }

    /**
     * Returns chunks loaded per world.
     *
     * @return chunks per world
     */
    public Map<String, Long> chunksLoadedByWorld() {
        return chunksLoadedByWorld;
    }

    /**
     * Returns TPS samples or null when not available.
     *
     * @return TPS samples
     */
    public double[] tpsNullable() {
        return tpsNullable == null ? null : Arrays.copyOf(tpsNullable, tpsNullable.length);
    }

    /**
     * Returns average MSPT or null when not available.
     *
     * @return average MSPT
     */
    public Double msptAvgNullable() {
        return msptAvgNullable;
    }

    /**
     * Returns p95 MSPT or null when not available.
     *
     * @return p95 MSPT
     */
    public Double msptP95Nullable() {
        return msptP95Nullable;
    }
}
