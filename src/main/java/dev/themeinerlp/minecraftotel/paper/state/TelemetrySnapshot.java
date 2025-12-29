package dev.themeinerlp.minecraftotel.paper.state;

import java.util.Map;

/**
 * Immutable snapshot of Paper telemetry values for metric callbacks.
 *
 * @param playersOnline total online players
 * @param entitiesLoadedByWorld entities loaded per world
 * @param chunksLoadedByWorld chunks loaded per world
 * @param tpsNullable TPS windows (1m/5m/15m) or null when unavailable
 * @param msptAvgNullable average MSPT or null when unavailable
 * @param msptP95Nullable p95 MSPT or null when unavailable
 */
public record TelemetrySnapshot(
        long playersOnline,
        Map<String, Long> entitiesLoadedByWorld,
        Map<String, Long> chunksLoadedByWorld,
        double[] tpsNullable,
        Double msptAvgNullable,
        Double msptP95Nullable
) {
}
