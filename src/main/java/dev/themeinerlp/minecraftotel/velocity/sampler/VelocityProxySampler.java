package dev.themeinerlp.minecraftotel.velocity.sampler;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.themeinerlp.minecraftotel.velocity.config.VelocityPluginConfig;
import dev.themeinerlp.minecraftotel.velocity.state.VelocityTelemetrySnapshot;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples player and backend server counts from a Velocity proxy.
 */
public final class VelocityProxySampler implements VelocitySampler {
    private final ProxyServer proxyServer;
    private final boolean includePlayersPerServer;
    private final boolean includeServerCount;

    /**
     * Creates a sampler bound to a proxy server and config flags.
     *
     * @param proxyServer Velocity proxy server
     * @param config config with sampling feature flags
     */
    public VelocityProxySampler(ProxyServer proxyServer, VelocityPluginConfig config) {
        this.proxyServer = proxyServer;
        this.includePlayersPerServer = config.enablePlayersPerServer;
        this.includeServerCount = config.enableServerCount;
    }

    @Override
    public VelocityTelemetrySnapshot sample() {
        long playersOnline = proxyServer.getAllPlayers().size();
        Collection<RegisteredServer> servers = proxyServer.getAllServers();
        long registeredServers = includeServerCount ? servers.size() : 0L;
        Map<String, Long> playersByServer = includePlayersPerServer
                ? mapPlayersByServer(servers)
                : Map.of();

        return new VelocityTelemetrySnapshot(
                playersOnline,
                playersByServer,
                registeredServers
        );
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
