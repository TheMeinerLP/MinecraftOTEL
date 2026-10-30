package dev.themeinerlp.minecraftotel.velocity.snapshot;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;
import java.util.List;
import java.util.Map;

/**
 * Velocity-specific telemetry snapshot data.
 */
public final class VelocityTelemetrySnapshot implements TelemetrySnapshot {
    private final long playersOnline;
    private final Map<String, Long> playersByServer;
    private final long registeredServers;
    private final Map<String, Long> playersByVersion;
    private final List<Long> playerLatencies;

    VelocityTelemetrySnapshot(
            long playersOnline,
            Map<String, Long> playersByServer,
            long registeredServers,
            Map<String, Long> playersByVersion,
            List<Long> playerLatencies
    ) {
        this.playersOnline = playersOnline;
        this.playersByServer = playersByServer == null ? Map.of() : Map.copyOf(playersByServer);
        this.registeredServers = registeredServers;
        this.playersByVersion = playersByVersion == null ? Map.of() : Map.copyOf(playersByVersion);
        this.playerLatencies = playerLatencies == null ? List.of() : List.copyOf(playerLatencies);
    }

    /**
     * Returns an empty snapshot.
     *
     * @return empty snapshot
     */
    public static VelocityTelemetrySnapshot empty() {
        return new VelocityTelemetrySnapshot(0L, Map.of(), 0L, Map.of(), List.of());
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
     * Returns online players per client version name, empty when disabled.
     *
     * @return players per version
     * @since 1.7.0
     */
    public Map<String, Long> playersByVersion() {
        return playersByVersion;
    }

    /**
     * Returns the known latency in milliseconds of every online player, empty when disabled.
     *
     * @return player latencies
     * @since 1.7.0
     */
    public List<Long> playerLatencies() {
        return playerLatencies;
    }
}
