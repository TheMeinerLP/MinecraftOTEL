package dev.themeinerlp.minecraftotel.state;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import org.bukkit.Server;
import org.bukkit.World;

public final class TelemetryState {
    private final AtomicReference<TelemetrySnapshot> snapshotRef;
    private final ConcurrentHashMap<String, LongAdder> entitiesGaugeByWorld;
    private final ConcurrentHashMap<String, LongAdder> chunksGaugeByWorld;
    private final AtomicBoolean paperEntityEventsAvailable;

    public TelemetryState() {
        this.snapshotRef = new AtomicReference<>(
                new TelemetrySnapshot(0L, Map.of(), Map.of(), null, null, null)
        );
        this.entitiesGaugeByWorld = new ConcurrentHashMap<>();
        this.chunksGaugeByWorld = new ConcurrentHashMap<>();
        this.paperEntityEventsAvailable = new AtomicBoolean(false);
    }

    public TelemetrySnapshot getSnapshot() {
        return snapshotRef.get();
    }

    public void setSnapshot(TelemetrySnapshot snapshot) {
        snapshotRef.set(snapshot);
    }

    public boolean isPaperEntityEventsAvailable() {
        return paperEntityEventsAvailable.get();
    }

    public void setPaperEntityEventsAvailable(boolean available) {
        paperEntityEventsAvailable.set(available);
    }

    public void incrementEntity(String worldName) {
        addGauge(entitiesGaugeByWorld, worldName, 1L);
    }

    public void decrementEntity(String worldName) {
        addGauge(entitiesGaugeByWorld, worldName, -1L);
    }

    public void incrementChunk(String worldName) {
        addGauge(chunksGaugeByWorld, worldName, 1L);
    }

    public void decrementChunk(String worldName) {
        addGauge(chunksGaugeByWorld, worldName, -1L);
    }

    public void baselineInit(Server server) {
        replaceGauge(entitiesGaugeByWorld, scanEntities(server));
        replaceGauge(chunksGaugeByWorld, scanChunks(server));
    }

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
                msptP95Nullable
        );
    }

    public Map<String, Long> scanEntities(Server server) {
        Map<String, Long> baseline = new HashMap<>();
        for (World world : server.getWorlds()) {
            baseline.put(world.getName(), (long) world.getEntities().size());
        }
        return baseline;
    }

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
