package dev.themeinerlp.minecraftotel.api;

import java.util.Map;

/**
 * Mutable builder for assembling a TelemetrySnapshot from multiple samplers.
 */
public final class TelemetrySnapshotBuilder {
    private Long playersOnline;
    private Map<String, Long> entitiesLoadedByWorld;
    private Map<String, Long> chunksLoadedByWorld;
    private double[] tps;
    private Double msptAvg;
    private Double msptP95;
    private Map<String, Long> playersByServer;
    private Long registeredServers;

    /**
     * Creates an empty builder.
     */
    public TelemetrySnapshotBuilder() {
    }

    public TelemetrySnapshotBuilder setPlayersOnline(long playersOnline) {
        this.playersOnline = playersOnline;
        return this;
    }

    public TelemetrySnapshotBuilder setEntitiesLoadedByWorld(Map<String, Long> entitiesLoadedByWorld) {
        this.entitiesLoadedByWorld = entitiesLoadedByWorld;
        return this;
    }

    public TelemetrySnapshotBuilder setChunksLoadedByWorld(Map<String, Long> chunksLoadedByWorld) {
        this.chunksLoadedByWorld = chunksLoadedByWorld;
        return this;
    }

    public TelemetrySnapshotBuilder setTps(double[] tps) {
        this.tps = tps;
        return this;
    }

    public TelemetrySnapshotBuilder setMsptAvg(Double msptAvg) {
        this.msptAvg = msptAvg;
        return this;
    }

    public TelemetrySnapshotBuilder setMsptP95(Double msptP95) {
        this.msptP95 = msptP95;
        return this;
    }

    public TelemetrySnapshotBuilder setPlayersByServer(Map<String, Long> playersByServer) {
        this.playersByServer = playersByServer;
        return this;
    }

    public TelemetrySnapshotBuilder setRegisteredServers(long registeredServers) {
        this.registeredServers = registeredServers;
        return this;
    }

    /**
     * Builds an immutable snapshot, filling missing values with defaults.
     *
     * @return telemetry snapshot
     */
    public TelemetrySnapshot build() {
        return new TelemetrySnapshot(
                playersOnline == null ? 0L : playersOnline,
                entitiesLoadedByWorld == null ? Map.of() : entitiesLoadedByWorld,
                chunksLoadedByWorld == null ? Map.of() : chunksLoadedByWorld,
                tps,
                msptAvg,
                msptP95,
                playersByServer == null ? Map.of() : playersByServer,
                registeredServers == null ? 0L : registeredServers
        );
    }
}
