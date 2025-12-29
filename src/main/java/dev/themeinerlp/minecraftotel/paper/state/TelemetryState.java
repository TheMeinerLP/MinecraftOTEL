package dev.themeinerlp.minecraftotel.paper.state;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;
import dev.themeinerlp.minecraftotel.api.state.TelemetryStateStore;
import dev.themeinerlp.minecraftotel.paper.config.EntitiesByChunkMode;
import dev.themeinerlp.minecraftotel.paper.snapshot.PaperTelemetrySnapshot;
import dev.themeinerlp.minecraftotel.paper.snapshot.PaperTelemetrySnapshot.ChunkEntityKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Thread-safe state holder for Paper telemetry counters and snapshots.
 */
public final class TelemetryState implements TelemetryStateStore {
    private final Map<String, Long> entitiesGaugeByWorld;
    private final Map<String, Long> entitiesGaugeByType;
    private final Map<ChunkEntityKey, Long> entitiesGaugeByTypeAndChunk;
    private final Map<String, Long> chunksGaugeByWorld;
    private final Map<ChunkKey, Integer> playerChunkViewers;
    private long exclusivePlayerChunks;
    private volatile TelemetrySnapshot snapshot;
    private volatile boolean entityEventsAvailable;
    private volatile EntitiesByChunkMode entityTypeChunkMode;

    public TelemetryState() {
        this.snapshot = PaperTelemetrySnapshot.empty();
        this.entitiesGaugeByWorld = new HashMap<>();
        this.entitiesGaugeByType = new HashMap<>();
        this.entitiesGaugeByTypeAndChunk = new HashMap<>();
        this.chunksGaugeByWorld = new HashMap<>();
        this.playerChunkViewers = new HashMap<>();
        this.exclusivePlayerChunks = 0L;
        this.entityEventsAvailable = false;
        this.entityTypeChunkMode = EntitiesByChunkMode.OFF;
    }

    /**
     * Returns the latest snapshot used by metric callbacks.
     *
     * @return current snapshot
     */
    @Override
    public TelemetrySnapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Replaces the current snapshot.
     *
     * @param snapshot new snapshot to expose
     */
    @Override
    public void setSnapshot(TelemetrySnapshot snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * Returns whether entity add/remove events are available.
     *
     * @return true when entity events are enabled
     */
    public boolean isEntityEventsAvailable() {
        return entityEventsAvailable;
    }

    /**
     * Sets whether entity add/remove events are available.
     *
     * @param available true when entity events are enabled
     */
    public void setEntityEventsAvailable(boolean available) {
        entityEventsAvailable = available;
    }

    /**
     * Returns whether entity type per chunk tracking is enabled.
     *
     * @return true when tracking is enabled
     */
    public boolean isEntityTypeChunkTrackingEnabled() {
        return entityTypeChunkMode != EntitiesByChunkMode.OFF;
    }

    /**
     * Sets the mode for entity type per chunk tracking.
     *
     * @param mode tracking mode
     */
    public void setEntityTypeChunkMode(EntitiesByChunkMode mode) {
        entityTypeChunkMode = mode == null ? EntitiesByChunkMode.OFF : mode;
        if (entityTypeChunkMode == EntitiesByChunkMode.OFF) {
            synchronized (entitiesGaugeByTypeAndChunk) {
                entitiesGaugeByTypeAndChunk.clear();
            }
        }
    }

    /**
     * Returns the current entity type per chunk tracking mode.
     *
     * @return tracking mode
     */
    public EntitiesByChunkMode getEntityTypeChunkMode() {
        return entityTypeChunkMode;
    }

    /**
     * Increments the entity gauge for the given world.
     *
     * @param worldName world name
     */
    public void incrementEntity(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return;
        }
        updateGauge(entitiesGaugeByWorld, worldName, 1L);
    }

    /**
     * Decrements the entity gauge for the given world.
     *
     * @param worldName world name
     */
    public void decrementEntity(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return;
        }
        updateGauge(entitiesGaugeByWorld, worldName, -1L);
    }

    /**
     * Increments the entity type gauge for the given type key.
     *
     * @param entityTypeKey namespaced entity type key
     */
    public void incrementEntityType(String entityTypeKey) {
        if (entityTypeKey == null || entityTypeKey.isBlank()) {
            return;
        }
        updateGauge(entitiesGaugeByType, entityTypeKey, 1L);
    }

    /**
     * Decrements the entity type gauge for the given type key.
     *
     * @param entityTypeKey namespaced entity type key
     */
    public void decrementEntityType(String entityTypeKey) {
        if (entityTypeKey == null || entityTypeKey.isBlank()) {
            return;
        }
        updateGauge(entitiesGaugeByType, entityTypeKey, -1L);
    }

    /**
     * Increments the entity type gauge for the given chunk.
     *
     * @param worldName world name
     * @param chunkX chunk X coordinate
     * @param chunkZ chunk Z coordinate
     * @param entityTypeKey namespaced entity type key
     */
    public void incrementEntityTypeInChunk(Entity entity) {
        if (entity == null || entityTypeChunkMode == EntitiesByChunkMode.OFF) {
            return;
        }
        String typeKey = resolveChunkEntityTypeKey(entity);
        if (typeKey == null || typeKey.isBlank()) {
            return;
        }
        var chunk = entity.getChunk();
        String worldName = chunk.getWorld().getName();
        if (worldName == null || worldName.isBlank()) {
            return;
        }
        updateGauge(
                entitiesGaugeByTypeAndChunk,
                new ChunkEntityKey(worldName, chunk.getX(), chunk.getZ(), typeKey),
                1L
        );
    }

    /**
     * Decrements the entity type gauge for the given chunk.
     *
     * @param worldName world name
     * @param chunkX chunk X coordinate
     * @param chunkZ chunk Z coordinate
     * @param entityTypeKey namespaced entity type key
     */
    public void decrementEntityTypeInChunk(Entity entity) {
        if (entity == null || entityTypeChunkMode == EntitiesByChunkMode.OFF) {
            return;
        }
        String typeKey = resolveChunkEntityTypeKey(entity);
        if (typeKey == null || typeKey.isBlank()) {
            return;
        }
        var chunk = entity.getChunk();
        String worldName = chunk.getWorld().getName();
        if (worldName == null || worldName.isBlank()) {
            return;
        }
        updateGauge(
                entitiesGaugeByTypeAndChunk,
                new ChunkEntityKey(worldName, chunk.getX(), chunk.getZ(), typeKey),
                -1L
        );
    }

    /**
     * Increments the chunk gauge for the given world.
     *
     * @param worldName world name
     */
    public void incrementChunk(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return;
        }
        updateGauge(chunksGaugeByWorld, worldName, 1L);
    }

    /**
     * Decrements the chunk gauge for the given world.
     *
     * @param worldName world name
     */
    public void decrementChunk(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return;
        }
        updateGauge(chunksGaugeByWorld, worldName, -1L);
    }

    /**
     * Records that a player started receiving a chunk.
     *
     * @param worldName world name
     * @param chunkX chunk X coordinate
     * @param chunkZ chunk Z coordinate
     */
    public void recordPlayerChunkLoad(String worldName, int chunkX, int chunkZ) {
        if (worldName == null || worldName.isBlank()) {
            return;
        }
        ChunkKey key = new ChunkKey(worldName, chunkX, chunkZ);
        synchronized (playerChunkViewers) {
            int current = playerChunkViewers.getOrDefault(key, 0);
            int next = current + 1;
            playerChunkViewers.put(key, next);
            if (current == 0) {
                exclusivePlayerChunks++;
            } else if (current == 1) {
                exclusivePlayerChunks = Math.max(0L, exclusivePlayerChunks - 1L);
            }
        }
    }

    /**
     * Records that a player stopped receiving a chunk.
     *
     * @param worldName world name
     * @param chunkX chunk X coordinate
     * @param chunkZ chunk Z coordinate
     */
    public void recordPlayerChunkUnload(String worldName, int chunkX, int chunkZ) {
        if (worldName == null || worldName.isBlank()) {
            return;
        }
        ChunkKey key = new ChunkKey(worldName, chunkX, chunkZ);
        synchronized (playerChunkViewers) {
            Integer currentObj = playerChunkViewers.get(key);
            if (currentObj == null) {
                return;
            }
            int current = currentObj;
            if (current <= 1) {
                playerChunkViewers.remove(key);
                exclusivePlayerChunks = Math.max(0L, exclusivePlayerChunks - 1L);
                return;
            }
            int next = current - 1;
            playerChunkViewers.put(key, next);
            if (current == 2) {
                exclusivePlayerChunks++;
            }
        }
    }

    /**
     * Returns the number of chunks currently visible to exactly one player.
     *
     * @return exclusive player chunk count
     */
    public long getExclusivePlayerChunks() {
        synchronized (playerChunkViewers) {
            return exclusivePlayerChunks;
        }
    }

    /**
     * Seeds gauges with a baseline scan from the server.
     *
     * @param server Bukkit server
     */
    public void baselineInit(Server server) {
        replaceGauge(entitiesGaugeByWorld, scanEntities(server));
        replaceGauge(entitiesGaugeByType, scanEntitiesByType(server));
        if (entityTypeChunkMode != EntitiesByChunkMode.OFF) {
            replaceGauge(entitiesGaugeByTypeAndChunk, scanEntitiesByTypeAndChunk(server));
        } else {
            replaceGauge(entitiesGaugeByTypeAndChunk, Map.of());
        }
        replaceGauge(chunksGaugeByWorld, scanChunks(server));
    }

    /**
     * Rebuilds a snapshot from gauges and optionally refreshed baselines.
     *
     * @param playersOnline online player count
     * @param tpsNullable TPS samples or null
     * @param msptAvgNullable average MSPT or null
     * @param msptP95Nullable p95 MSPT or null
     * @param baselineEntitiesMapOrNull baseline entities map or null to reuse gauge
     * @param baselineEntityTypesMapOrNull baseline entity types map or null to reuse gauge
     * @param baselineEntityTypesByChunkMapOrNull baseline entity types by chunk map or null to reuse gauge
     * @param baselineChunksMapOrNull baseline chunks map or null to reuse gauge
     * @return rebuilt snapshot
     */
    public PaperTelemetrySnapshot rebuildSnapshot(
            long playersOnline,
            double[] tpsNullable,
            Double msptAvgNullable,
            Double msptP95Nullable,
            Map<String, Long> baselineEntitiesMapOrNull,
            Map<String, Long> baselineEntityTypesMapOrNull,
            Map<ChunkEntityKey, Long> baselineEntityTypesByChunkMapOrNull,
            Map<String, Long> baselineChunksMapOrNull
    ) {
        Map<String, Long> entitiesSnapshot = baselineEntitiesMapOrNull != null
                ? applyBaseline(entitiesGaugeByWorld, baselineEntitiesMapOrNull)
                : snapshotFromGauge(entitiesGaugeByWorld);
        Map<String, Long> entitiesByTypeSnapshot = baselineEntityTypesMapOrNull != null
                ? applyBaseline(entitiesGaugeByType, baselineEntityTypesMapOrNull)
                : snapshotFromGauge(entitiesGaugeByType);
        Map<ChunkEntityKey, Long> entitiesByTypeAndChunkSnapshot = null;
        if (entityTypeChunkMode != EntitiesByChunkMode.OFF) {
            entitiesByTypeAndChunkSnapshot = baselineEntityTypesByChunkMapOrNull != null
                    ? applyBaseline(entitiesGaugeByTypeAndChunk, baselineEntityTypesByChunkMapOrNull)
                    : snapshotFromGauge(entitiesGaugeByTypeAndChunk);
        }
        Map<String, Long> chunksSnapshot = baselineChunksMapOrNull != null
                ? applyBaseline(chunksGaugeByWorld, baselineChunksMapOrNull)
                : snapshotFromGauge(chunksGaugeByWorld);
        double[] tpsCopy = tpsNullable == null ? null : Arrays.copyOf(tpsNullable, tpsNullable.length);
        return new PaperTelemetrySnapshot(
                playersOnline,
                entitiesSnapshot,
                entitiesByTypeSnapshot,
                entitiesByTypeAndChunkSnapshot,
                chunksSnapshot,
                getExclusivePlayerChunks(),
                tpsCopy,
                msptAvgNullable,
                msptP95Nullable
        );
    }

    /**
     * Performs a synchronous scan of entities per world.
     *
     * @param server Bukkit server
     * @return entities per world
     */
    public Map<String, Long> scanEntities(Server server) {
        Map<String, Long> baseline = new HashMap<>();
        for (World world : server.getWorlds()) {
            baseline.put(world.getName(), (long) world.getEntities().size());
        }
        return baseline;
    }

    /**
     * Performs a synchronous scan of entities per type.
     *
     * @param server Bukkit server
     * @return entities per type
     */
    public Map<String, Long> scanEntitiesByType(Server server) {
        Map<String, Long> baseline = new HashMap<>();
        for (World world : server.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                var typeKeyObj = entity.getType().getKey();
                String typeKey = typeKeyObj == null ? "" : typeKeyObj.toString();
                if (typeKey.isBlank()) {
                    continue;
                }
                baseline.merge(typeKey, 1L, Long::sum);
            }
        }
        return baseline;
    }

    /**
     * Performs a synchronous scan of entities per type and chunk.
     *
     * @param server Bukkit server
     * @return entities per type and chunk
     */
    public Map<ChunkEntityKey, Long> scanEntitiesByTypeAndChunk(Server server) {
        if (entityTypeChunkMode == EntitiesByChunkMode.OFF) {
            return Map.of();
        }
        Map<ChunkEntityKey, Long> baseline = new HashMap<>();
        for (World world : server.getWorlds()) {
            String worldName = world.getName();
            if (worldName == null || worldName.isBlank()) {
                continue;
            }
            for (Entity entity : world.getEntities()) {
                String typeKey = resolveChunkEntityTypeKey(entity);
                if (typeKey == null || typeKey.isBlank()) {
                    continue;
                }
                var chunk = entity.getChunk();
                ChunkEntityKey key = new ChunkEntityKey(worldName, chunk.getX(), chunk.getZ(), typeKey);
                baseline.merge(key, 1L, Long::sum);
            }
        }
        return baseline;
    }

    private String resolveChunkEntityTypeKey(Entity entity) {
        EntitiesByChunkMode mode = entityTypeChunkMode;
        if (mode == EntitiesByChunkMode.OFF) {
            return null;
        }
        if (mode == EntitiesByChunkMode.HEAVY) {
            var typeKeyObj = entity.getType().getKey();
            String typeKey = typeKeyObj == null ? "" : typeKeyObj.toString();
            return typeKey.isBlank() ? null : typeKey;
        }
        if (entity instanceof Player) {
            return null;
        }
        if (entity instanceof Enemy) {
            return "hostile";
        }
        if (entity instanceof LivingEntity) {
            return "passive";
        }
        return null;
    }

    /**
     * Performs a synchronous scan of loaded chunks per world.
     *
     * @param server Bukkit server
     * @return loaded chunks per world
     */
    public Map<String, Long> scanChunks(Server server) {
        Map<String, Long> baseline = new HashMap<>();
        for (World world : server.getWorlds()) {
            baseline.put(world.getName(), (long) world.getLoadedChunks().length);
        }
        return baseline;
    }

    private static <K> void updateGauge(Map<K, Long> gauge, K key, long delta) {
        if (key == null) {
            return;
        }
        synchronized (gauge) {
            gauge.merge(key, delta, Long::sum);
        }
    }

    private static <K> Map<K, Long> snapshotFromGauge(Map<K, Long> gauge) {
        Map<K, Long> snapshot = new HashMap<>();
        synchronized (gauge) {
            for (Map.Entry<K, Long> entry : gauge.entrySet()) {
                long value = entry.getValue();
                snapshot.put(entry.getKey(), Math.max(0L, value));
            }
        }
        return Map.copyOf(snapshot);
    }

    private static <K> Map<K, Long> applyBaseline(
            Map<K, Long> gauge,
            Map<K, Long> baseline
    ) {
        Map<K, Long> normalized = new HashMap<>();
        for (Map.Entry<K, Long> entry : baseline.entrySet()) {
            long value = Math.max(0L, entry.getValue());
            normalized.put(entry.getKey(), value);
        }
        replaceGauge(gauge, normalized);
        return Map.copyOf(normalized);
    }

    private static <K> void replaceGauge(
            Map<K, Long> gauge,
            Map<K, Long> baseline
    ) {
        synchronized (gauge) {
            gauge.clear();
            for (Map.Entry<K, Long> entry : baseline.entrySet()) {
                gauge.put(entry.getKey(), Math.max(0L, entry.getValue()));
            }
        }
    }

    private static final class ChunkKey {
        private final String worldName;
        private final int chunkX;
        private final int chunkZ;

        private ChunkKey(String worldName, int chunkX, int chunkZ) {
            this.worldName = worldName;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ChunkKey other = (ChunkKey) obj;
            if (chunkX != other.chunkX || chunkZ != other.chunkZ) {
                return false;
            }
            return worldName.equals(other.worldName);
        }

        @Override
        public int hashCode() {
            int result = worldName.hashCode();
            result = 31 * result + chunkX;
            result = 31 * result + chunkZ;
            return result;
        }
    }
}
