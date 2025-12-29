package dev.themeinerlp.minecraftotel.velocity.snapshot;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;
import java.util.Map;

/**
 * Velocity-specific telemetry snapshot data.
 */
public final class VelocityTelemetrySnapshot implements TelemetrySnapshot {
    private final long playersOnline;
    private final Map<String, Long> playersByServer;
    private final long registeredServers;

    VelocityTelemetrySnapshot(
            long playersOnline,
            Map<String, Long> playersByServer,
            long registeredServers
    ) {
        this.playersOnline = playersOnline;
        this.playersByServer = playersByServer == null ? Map.of() : Map.copyOf(playersByServer);
        this.registeredServers = registeredServers;
    }

    /**
     * Returns an empty snapshot.
     *
     * @return empty snapshot
     */
    public static VelocityTelemetrySnapshot empty() {
        return new VelocityTelemetrySnapshot(0L, Map.of(), 0L);
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
}
