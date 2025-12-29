package dev.themeinerlp.minecraftotel.api.snapshot;

import java.util.Map;
import java.util.Optional;

/**
 * Immutable snapshot of the latest telemetry values.
 *
 * @since 1.1.0
 * @version 1.1.0
 */
public abstract class TelemetrySnapshot {
    private final long playersOnline;
    private final Map<String, Long> entitiesLoadedByWorld;
    private final Map<String, Long> chunksLoadedByWorld;
    private final double[] tpsNullable;
    private final Double msptAvgNullable;
    private final Double msptP95Nullable;
    private final Map<String, Long> playersByServer;
    private final long registeredServers;
    private final Object platformData;

    protected TelemetrySnapshot(
            long playersOnline,
            Map<String, Long> entitiesLoadedByWorld,
            Map<String, Long> chunksLoadedByWorld,
            double[] tpsNullable,
            Double msptAvgNullable,
            Double msptP95Nullable,
            Map<String, Long> playersByServer,
            long registeredServers,
            Object platformData
    ) {
        this.playersOnline = playersOnline;
        this.entitiesLoadedByWorld = entitiesLoadedByWorld;
        this.chunksLoadedByWorld = chunksLoadedByWorld == null ? Map.of() : chunksLoadedByWorld;
        this.tpsNullable = tpsNullable;
        this.msptAvgNullable = msptAvgNullable;
        this.msptP95Nullable = msptP95Nullable;
        this.playersByServer = playersByServer == null ? Map.of() : playersByServer;
        this.registeredServers = registeredServers;
        this.platformData = platformData;
    }

    /**
     * Creates an empty snapshot with zeroed values.
     *
     * @return empty snapshot
     */
    public static TelemetrySnapshot empty() {
        return of(0L, null, Map.of(), null, null, null, Map.of(), 0L, null);
    }

    /**
     * Creates a snapshot with an optional platform payload.
     *
     * @param playersOnline total online players on the platform
     * @param entitiesLoadedByWorld entities loaded per world (Paper only)
     * @param chunksLoadedByWorld chunks loaded per world (Paper only)
     * @param tpsNullable TPS windows (1m/5m/15m) or null when unavailable
     * @param msptAvgNullable average MSPT or null when unavailable
     * @param msptP95Nullable p95 MSPT or null when unavailable
     * @param playersByServer players online per backend server (Velocity only)
     * @param registeredServers number of registered backend servers (Velocity only)
     * @param platformData platform-specific payload or null
     * @return immutable snapshot
     */
    public static TelemetrySnapshot of(
            long playersOnline,
            Map<String, Long> entitiesLoadedByWorld,
            Map<String, Long> chunksLoadedByWorld,
            double[] tpsNullable,
            Double msptAvgNullable,
            Double msptP95Nullable,
            Map<String, Long> playersByServer,
            long registeredServers,
            Object platformData
    ) {
        return new DefaultTelemetrySnapshot(
                playersOnline,
                entitiesLoadedByWorld,
                chunksLoadedByWorld,
                tpsNullable,
                msptAvgNullable,
                msptP95Nullable,
                playersByServer,
                registeredServers,
                platformData
        );
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
        return tpsNullable;
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
     * Returns players online per backend server.
     *
     * @return players per server
     */
    public Map<String, Long> playersByServer() {
        return playersByServer;
    }

    /**
     * Returns number of registered backend servers.
     *
     * @return registered server count
     */
    public long registeredServers() {
        return registeredServers;
    }

    /**
     * Returns platform-specific payload data.
     *
     * @return platform data or null
     */
    public Object platformData() {
        return platformData;
    }

    private static final class DefaultTelemetrySnapshot extends TelemetrySnapshot {
        private DefaultTelemetrySnapshot(
                long playersOnline,
                Map<String, Long> entitiesLoadedByWorld,
                Map<String, Long> chunksLoadedByWorld,
                double[] tpsNullable,
                Double msptAvgNullable,
                Double msptP95Nullable,
                Map<String, Long> playersByServer,
                long registeredServers,
                Object platformData
        ) {
            super(
                    playersOnline,
                    entitiesLoadedByWorld,
                    chunksLoadedByWorld,
                    tpsNullable,
                    msptAvgNullable,
                    msptP95Nullable,
                    playersByServer,
                    registeredServers,
                    platformData
            );
        }
    }
}
