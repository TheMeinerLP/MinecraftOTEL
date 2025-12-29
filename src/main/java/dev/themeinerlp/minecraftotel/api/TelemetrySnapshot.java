package dev.themeinerlp.minecraftotel.api;

import java.util.Map;

/**
 * Immutable snapshot of the latest telemetry values.
 *
 * @param playersOnline total online players on the platform
 * @param entitiesLoadedByWorld entities loaded per world (Paper only)
 * @param chunksLoadedByWorld chunks loaded per world (Paper only)
 * @param tpsNullable TPS windows (1m/5m/15m) or null when unavailable
 * @param msptAvgNullable average MSPT or null when unavailable
 * @param msptP95Nullable p95 MSPT or null when unavailable
 * @param playersByServer players online per backend server (Velocity only)
 * @param registeredServers number of registered backend servers (Velocity only)
 */
public record TelemetrySnapshot(
        long playersOnline,
        Map<String, Long> entitiesLoadedByWorld,
        Map<String, Long> chunksLoadedByWorld,
        double[] tpsNullable,
        Double msptAvgNullable,
        Double msptP95Nullable,
        Map<String, Long> playersByServer,
        long registeredServers
) {
    /**
     * Creates an empty snapshot with zeroed values.
     *
     * @return empty snapshot
     */
    public static TelemetrySnapshot empty() {
        return new TelemetrySnapshot(0L, Map.of(), Map.of(), null, null, null, Map.of(), 0L);
    }
}
