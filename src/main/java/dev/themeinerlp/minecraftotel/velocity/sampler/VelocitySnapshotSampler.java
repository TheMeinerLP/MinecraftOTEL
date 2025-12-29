package dev.themeinerlp.minecraftotel.velocity.sampler;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshotBuilder;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshotSampler;
import dev.themeinerlp.minecraftotel.velocity.config.VelocityPluginConfig;
import dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshotBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds telemetry snapshots for Velocity proxies.
 */
public final class VelocitySnapshotSampler implements TelemetrySnapshotSampler {
    private final ProxyServer proxyServer;
    private final VelocityPluginConfig config;

    public VelocitySnapshotSampler(ProxyServer proxyServer, VelocityPluginConfig config) {
        this.proxyServer = proxyServer;
        this.config = config;
    }

    @Override
    public void sample(TelemetrySnapshotBuilder builder) {
        if (!(builder instanceof VelocityTelemetrySnapshotBuilder velocityBuilder)) {
            return;
        }
        long playersOnline = proxyServer.getAllPlayers().size();
        Collection<RegisteredServer> servers = proxyServer.getAllServers();
        long registeredServers = config.enableServerCount ? servers.size() : 0L;
        Map<String, Long> playersByServer = config.enablePlayersPerServer
                ? mapPlayersByServer(servers)
                : Map.of();

        velocityBuilder
                .setPlayersOnline(playersOnline)
                .setPlayersByServer(playersByServer)
                .setRegisteredServers(registeredServers);
    }

    private static Map<String, Long> mapPlayersByServer(Collection<RegisteredServer> servers) {
        Map<String, Long> playersByServer = new HashMap<>();
        for (RegisteredServer server : servers) {
            String name = server.getServerInfo().getName();
            playersByServer.put(name, (long) server.getPlayersConnected().size());
        }
        return Map.copyOf(playersByServer);
    }
}
