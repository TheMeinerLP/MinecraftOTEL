package dev.themeinerlp.minecraftotel.velocity.sampler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.themeinerlp.minecraftotel.velocity.config.VelocityPluginConfig;
import dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshot;
import dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshotBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.helpers.NOPLogger;

class VelocitySnapshotSamplerTest {
    @TempDir
    Path dataDirectory;

    @Test
    void playersAreCountedByMostRecentSupportedVersionName() throws IOException {
        ProxyServer proxyServer = proxyWith(
                player(ProtocolVersion.MINECRAFT_1_21_4, 20),
                player(ProtocolVersion.MINECRAFT_1_21_4, 30),
                player(ProtocolVersion.MINECRAFT_1_21, 40)
        );

        VelocityTelemetrySnapshot snapshot = sample(proxyServer, defaultConfig());

        assertEquals(
                Map.of("1.21.4", 2L, "1.21.1", 1L),
                snapshot.playersByVersion(),
                "players must be counted per the most recent supported version name of their protocol"
        );
    }

    @Test
    void negativePingIsSkippedFromLatencies() throws IOException {
        ProxyServer proxyServer = proxyWith(
                player(ProtocolVersion.MINECRAFT_1_21_4, 42),
                player(ProtocolVersion.MINECRAFT_1_21_4, -1)
        );

        VelocityTelemetrySnapshot snapshot = sample(proxyServer, defaultConfig());

        assertEquals(
                List.of(42L),
                snapshot.playerLatencies(),
                "a negative ping means unknown latency and must not produce a latency value"
        );
    }

    @Test
    void versionsAreEmptyWhenPlayersByVersionIsDisabled() throws IOException {
        ProxyServer proxyServer = proxyWith(player(ProtocolVersion.MINECRAFT_1_21_4, 20));
        VelocityPluginConfig config = config("otel.enable.playersByVersion=false\n");

        VelocityTelemetrySnapshot snapshot = sample(proxyServer, config);

        assertEquals(
                Map.of(),
                snapshot.playersByVersion(),
                "no client versions may be sampled while otel.enable.playersByVersion is false"
        );
    }

    @Test
    void latenciesAreEmptyWhenPlayerLatencyIsDisabled() throws IOException {
        ProxyServer proxyServer = proxyWith(player(ProtocolVersion.MINECRAFT_1_21_4, 20));
        VelocityPluginConfig config = config("otel.enable.playerLatency=false\n");

        VelocityTelemetrySnapshot snapshot = sample(proxyServer, config);

        assertEquals(
                List.of(),
                snapshot.playerLatencies(),
                "no player latencies may be sampled while otel.enable.playerLatency is false"
        );
    }

    @Test
    void missingFlagsDefaultToEnabled() throws IOException {
        ProxyServer proxyServer = proxyWith(player(ProtocolVersion.MINECRAFT_1_21_4, 20));

        VelocityTelemetrySnapshot snapshot = sample(proxyServer, config("sampling.intervalSeconds=1\n"));

        assertEquals(
                Map.of("1.21.4", 1L),
                snapshot.playersByVersion(),
                "an existing velocity.properties without the new keys must keep the versions enabled"
        );
    }

    private VelocityTelemetrySnapshot sample(ProxyServer proxyServer, VelocityPluginConfig config) {
        VelocityTelemetrySnapshotBuilder builder = new VelocityTelemetrySnapshotBuilder();
        new VelocitySnapshotSampler(proxyServer, config).sample(builder);
        return builder.build();
    }

    private VelocityPluginConfig defaultConfig() throws IOException {
        return config("");
    }

    private VelocityPluginConfig config(String content) throws IOException {
        Path file = dataDirectory.resolve(VelocityPluginConfig.CONFIG_FILE_NAME);
        Files.writeString(file, content);
        return VelocityPluginConfig.load(dataDirectory, NOPLogger.NOP_LOGGER, getClass().getClassLoader());
    }

    private static ProxyServer proxyWith(Player... players) {
        ProxyServer proxyServer = mock(ProxyServer.class);
        when(proxyServer.getAllPlayers()).thenReturn(List.of(players));
        when(proxyServer.getAllServers()).thenReturn(List.of());
        return proxyServer;
    }

    private static Player player(ProtocolVersion version, long ping) {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getProtocolVersion()).thenReturn(version);
        when(player.getPing()).thenReturn(ping);
        return player;
    }
}
