package dev.themeinerlp.minecraftotel.velocity.snapshot;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshotBuilder;
import java.util.List;
import java.util.Map;

/**
 * Builder for Velocity telemetry snapshots.
 */
public final class VelocityTelemetrySnapshotBuilder implements TelemetrySnapshotBuilder {
    private Long playersOnline;
    private Map<String, Long> playersByServer;
    private Long registeredServers;
    private Map<String, Long> playersByVersion;
    private List<Long> playerLatencies;

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
     * Sets online players per client version name.
     *
     * @param playersByVersion players per version
     * @return builder
     * @since 1.7.0
     */
    public VelocityTelemetrySnapshotBuilder setPlayersByVersion(Map<String, Long> playersByVersion) {
        this.playersByVersion = playersByVersion;
        return this;
    }

    /**
     * Sets the known latency in milliseconds of online players.
     *
     * @param playerLatencies latencies, one per player with a known ping
     * @return builder
     * @since 1.7.0
     */
    public VelocityTelemetrySnapshotBuilder setPlayerLatencies(List<Long> playerLatencies) {
        this.playerLatencies = playerLatencies;
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
                registeredServers == null ? 0L : registeredServers,
                playersByVersion == null ? Map.of() : playersByVersion,
                playerLatencies == null ? List.of() : playerLatencies
        );
    }
}
