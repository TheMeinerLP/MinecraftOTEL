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
    private final Map<String, Long> entitiesLoadedByType;
    private final Map<ChunkEntityKey, Long> entitiesLoadedByTypeAndChunk;
    private final Map<String, Long> chunksLoadedByWorld;
    private final long exclusiveChunksLoaded;
    private final double[] tpsNullable;
    private final Double msptAvgNullable;
    private final Double msptP95Nullable;

    public PaperTelemetrySnapshot(
            long playersOnline,
            Map<String, Long> entitiesLoadedByWorld,
            Map<String, Long> entitiesLoadedByType,
            Map<ChunkEntityKey, Long> entitiesLoadedByTypeAndChunk,
            Map<String, Long> chunksLoadedByWorld,
            long exclusiveChunksLoaded,
            double[] tpsNullable,
            Double msptAvgNullable,
            Double msptP95Nullable
    ) {
        this.playersOnline = playersOnline;
        this.entitiesLoadedByWorld = entitiesLoadedByWorld == null ? null : Map.copyOf(entitiesLoadedByWorld);
        this.entitiesLoadedByType = entitiesLoadedByType == null ? null : Map.copyOf(entitiesLoadedByType);
        this.entitiesLoadedByTypeAndChunk = entitiesLoadedByTypeAndChunk == null ? null : Map.copyOf(entitiesLoadedByTypeAndChunk);
        this.chunksLoadedByWorld = chunksLoadedByWorld == null ? Map.of() : Map.copyOf(chunksLoadedByWorld);
        this.exclusiveChunksLoaded = Math.max(0L, exclusiveChunksLoaded);
        this.tpsNullable = tpsNullable == null ? null : Arrays.copyOf(tpsNullable, tpsNullable.length);
        this.msptAvgNullable = msptAvgNullable;
        this.msptP95Nullable = msptP95Nullable;
    }

    public PaperTelemetrySnapshot(
            long playersOnline,
            Map<String, Long> entitiesLoadedByWorld,
            Map<String, Long> chunksLoadedByWorld,
            double[] tpsNullable,
            Double msptAvgNullable,
            Double msptP95Nullable
    ) {
        this(playersOnline, entitiesLoadedByWorld, null, null, chunksLoadedByWorld, 0L, tpsNullable, msptAvgNullable, msptP95Nullable);
    }

    /**
     * Returns an empty snapshot.
     *
     * @return empty snapshot
     */
    public static PaperTelemetrySnapshot empty() {
        return new PaperTelemetrySnapshot(0L, null, null, null, Map.of(), 0L, null, null, null);
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
     * Returns entities loaded per type when available.
     *
     * @return entities per type
     */
    public Optional<Map<String, Long>> entitiesLoadedByType() {
        return Optional.ofNullable(entitiesLoadedByType);
    }

    /**
     * Returns entities loaded per type and chunk when available.
     *
     * @return entities per type and chunk
     */
    public Optional<Map<ChunkEntityKey, Long>> entitiesLoadedByTypeAndChunk() {
        return Optional.ofNullable(entitiesLoadedByTypeAndChunk);
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
     * Returns the number of chunks currently visible to exactly one player.
     *
     * @return exclusive chunk count
     */
    public long exclusiveChunksLoaded() {
        return exclusiveChunksLoaded;
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

    /**
     * Key describing an entity type count for a specific chunk.
     */
    public static final class ChunkEntityKey {
        private final String worldName;
        private final int chunkX;
        private final int chunkZ;
        private final String entityType;

        public ChunkEntityKey(String worldName, int chunkX, int chunkZ, String entityType) {
            this.worldName = worldName;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.entityType = entityType;
        }

        public String worldName() {
            return worldName;
        }

        public int chunkX() {
            return chunkX;
        }

        public int chunkZ() {
            return chunkZ;
        }

        public String entityType() {
            return entityType;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ChunkEntityKey other = (ChunkEntityKey) obj;
            if (chunkX != other.chunkX || chunkZ != other.chunkZ) {
                return false;
            }
            if (!worldName.equals(other.worldName)) {
                return false;
            }
            return entityType.equals(other.entityType);
        }

        @Override
        public int hashCode() {
            int result = worldName.hashCode();
            result = 31 * result + chunkX;
            result = 31 * result + chunkZ;
            result = 31 * result + entityType.hashCode();
            return result;
        }
    }
}
