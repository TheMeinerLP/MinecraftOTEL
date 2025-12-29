package dev.themeinerlp.minecraftotel.velocity.snapshot;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshotBuilder;
import java.util.Map;

/**
 * Builder for Velocity telemetry snapshots.
 */
public final class VelocityTelemetrySnapshotBuilder implements TelemetrySnapshotBuilder {
    private Long playersOnline;
    private Map<String, Long> playersByServer;
    private Long registeredServers;

    /**
     * Sets the online player count.
     *
     * @param playersOnline online players
     * @return builder
     */
    public VelocityTelemetrySnapshotBuilder setPlayersOnline(long playersOnline) {
        this.playersOnline = playersOnline;
        return this;
    }

    /**
     * Sets players online per backend server.
     *
     * @param playersByServer players per server
     * @return builder
     */
    public VelocityTelemetrySnapshotBuilder setPlayersByServer(Map<String, Long> playersByServer) {
        this.playersByServer = playersByServer;
        return this;
    }

    /**
     * Sets number of registered backend servers.
     *
     * @param registeredServers server count
     * @return builder
     */
    public VelocityTelemetrySnapshotBuilder setRegisteredServers(long registeredServers) {
        this.registeredServers = registeredServers;
        return this;
    }

    /**
     * Builds the immutable Velocity snapshot.
     *
     * @return snapshot
     */
    public VelocityTelemetrySnapshot build() {
        return new VelocityTelemetrySnapshot(
                playersOnline == null ? 0L : playersOnline,
                playersByServer == null ? Map.of() : playersByServer,
                registeredServers == null ? 0L : registeredServers
        );
    }
}
