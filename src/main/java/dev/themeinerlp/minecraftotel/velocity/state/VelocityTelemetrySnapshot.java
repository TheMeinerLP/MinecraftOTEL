package dev.themeinerlp.minecraftotel.velocity.state;

import java.util.Map;

/**
 * Immutable snapshot of Velocity proxy telemetry data.
 *
 * @param playersOnline total players connected to the proxy
 * @param playersByServer players connected per backend server
 * @param registeredServers number of registered backend servers
 */
public record VelocityTelemetrySnapshot(
        long playersOnline,
        Map<String, Long> playersByServer,
        long registeredServers
) {
    /**
     * Creates an empty snapshot with zeroed values.
     *
     * @return empty snapshot
     */
    public static VelocityTelemetrySnapshot empty() {
        return new VelocityTelemetrySnapshot(0L, Map.of(), 0L);
    }
}
