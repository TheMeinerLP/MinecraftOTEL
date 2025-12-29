package dev.themeinerlp.minecraftotel.paper.state;

import dev.themeinerlp.minecraftotel.api.TelemetrySnapshot;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import org.bukkit.Server;
import org.bukkit.World;

/**
 * Thread-safe state holder for Paper telemetry counters and snapshots.
 */
public final class TelemetryState {
    private final AtomicReference<TelemetrySnapshot> snapshotRef;
    private final ConcurrentHashMap<String, LongAdder> entitiesGaugeByWorld;
    private final ConcurrentHashMap<String, LongAdder> chunksGaugeByWorld;
    private final AtomicBoolean entityEventsAvailable;

    public TelemetryState() {
        this.snapshotRef = new AtomicReference<>(TelemetrySnapshot.empty());
        this.entitiesGaugeByWorld = new ConcurrentHashMap<>();
        this.chunksGaugeByWorld = new ConcurrentHashMap<>();
        this.entityEventsAvailable = new AtomicBoolean(false);
    }

    /**
     * Returns the latest snapshot used by metric callbacks.
     *
     * @return current snapshot
     */
    public TelemetrySnapshot getSnapshot() {
        return snapshotRef.get();
    }

    /**
     * Replaces the current snapshot.
     *
     * @param snapshot new snapshot to expose
     */
    public void setSnapshot(TelemetrySnapshot snapshot) {
        snapshotRef.set(snapshot);
    }

    /**
     * Returns whether entity add/remove events are available.
     *
     * @return true when entity events are enabled
     */
    public boolean isEntityEventsAvailable() {
        return entityEventsAvailable.get();
    }

    /**
     * Sets whether entity add/remove events are available.
     *
     * @param available true when entity events are enabled
     */
    public void setEntityEventsAvailable(boolean available) {
        entityEventsAvailable.set(available);
    }

    /**
     * Increments the entity gauge for the given world.
     *
     * @param worldName world name
     */
    public void incrementEntity(String worldName) {
        addGauge(entitiesGaugeByWorld, worldName, 1L);
    }

    /**
     * Decrements the entity gauge for the given world.
     *
     * @param worldName world name
     */
    public void decrementEntity(String worldName) {
        addGauge(entitiesGaugeByWorld, worldName, -1L);
    }

    /**
     * Increments the chunk gauge for the given world.
     *
     * @param worldName world name
     */
    public void incrementChunk(String worldName) {
        addGauge(chunksGaugeByWorld, worldName, 1L);
    }

    /**
     * Decrements the chunk gauge for the given world.
     *
     * @param worldName world name
     */
    public void decrementChunk(String worldName) {
        addGauge(chunksGaugeByWorld, worldName, -1L);
    }

    /**
     * Seeds gauges with a baseline scan from the server.
     *
     * @param server Bukkit server
     */
    public void baselineInit(Server server) {
        replaceGauge(entitiesGaugeByWorld, scanEntities(server));
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
     * @param baselineChunksMapOrNull baseline chunks map or null to reuse gauge
     * @return rebuilt snapshot
     */
    public TelemetrySnapshot rebuildSnapshot(
            long playersOnline,
            double[] tpsNullable,
            Double msptAvgNullable,
            Double msptP95Nullable,
            Map<String, Long> baselineEntitiesMapOrNull,
            Map<String, Long> baselineChunksMapOrNull
    ) {
        Map<String, Long> entitiesSnapshot = baselineEntitiesMapOrNull != null
                ? applyBaseline(entitiesGaugeByWorld, baselineEntitiesMapOrNull)
                : snapshotFromGauge(entitiesGaugeByWorld);
        Map<String, Long> chunksSnapshot = baselineChunksMapOrNull != null
                ? applyBaseline(chunksGaugeByWorld, baselineChunksMapOrNull)
                : snapshotFromGauge(chunksGaugeByWorld);
        double[] tpsCopy = tpsNullable == null ? null : Arrays.copyOf(tpsNullable, tpsNullable.length);
        return new TelemetrySnapshot(
                playersOnline,
                entitiesSnapshot,
                chunksSnapshot,
                tpsCopy,
                msptAvgNullable,
                msptP95Nullable,
                Map.of(),
                0L
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

    private static void addGauge(
            ConcurrentHashMap<String, LongAdder> gauge,
            String worldName,
            long delta
    ) {
        if (worldName == null || worldName.isBlank()) {
            return;
        }
        gauge.computeIfAbsent(worldName, ignored -> new LongAdder()).add(delta);
    }

    private static Map<String, Long> snapshotFromGauge(ConcurrentHashMap<String, LongAdder> gauge) {
        Map<String, Long> snapshot = new HashMap<>();
        for (Map.Entry<String, LongAdder> entry : gauge.entrySet()) {
            long value = entry.getValue().sum();
            snapshot.put(entry.getKey(), Math.max(0L, value));
        }
        return Map.copyOf(snapshot);
    }

    private static Map<String, Long> applyBaseline(
            ConcurrentHashMap<String, LongAdder> gauge,
            Map<String, Long> baseline
    ) {
        Map<String, Long> normalized = new HashMap<>();
        for (Map.Entry<String, Long> entry : baseline.entrySet()) {
            long value = Math.max(0L, entry.getValue());
            normalized.put(entry.getKey(), value);
        }
        replaceGauge(gauge, normalized);
        return Map.copyOf(normalized);
    }

    private static void replaceGauge(
            ConcurrentHashMap<String, LongAdder> gauge,
            Map<String, Long> baseline
    ) {
        gauge.clear();
        for (Map.Entry<String, Long> entry : baseline.entrySet()) {
            LongAdder adder = new LongAdder();
            adder.add(Math.max(0L, entry.getValue()));
            gauge.put(entry.getKey(), adder);
        }
    }
}
