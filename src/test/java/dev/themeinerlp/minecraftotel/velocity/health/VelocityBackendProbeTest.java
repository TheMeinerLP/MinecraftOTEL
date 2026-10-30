package dev.themeinerlp.minecraftotel.velocity.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.PingOptions;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import dev.themeinerlp.minecraftotel.velocity.support.MutableClock;
import dev.themeinerlp.minecraftotel.velocity.support.RecordingTelemetryCollector;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class VelocityBackendProbeTest {
    private static final AttributeKey<String> SERVER = AttributeKey.stringKey("server");

    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-30T12:00:00Z"));
    private final RecordingTelemetryCollector collector = new RecordingTelemetryCollector();
    private final ProxyServer proxyServer = mock(ProxyServer.class);
    private final VelocityBackendProbe probe = new VelocityBackendProbe(
            proxyServer,
            collector,
            clock,
            Duration.ofSeconds(5)
    );

    @Test
    void reachableBackendReportsUpAndItsLatency() {
        RegisteredServer lobby = backend("lobby");
        when(proxyServer.getAllServers()).thenReturn(List.of(lobby));
        when(lobby.ping(any(PingOptions.class))).thenAnswer(invocation -> {
            clock.advance(Duration.ofMillis(42));
            return CompletableFuture.completedFuture(mock(ServerPing.class));
        });

        probe.probe();

        assertEquals(
                Map.of(attributes("lobby"), 1L),
                collector.replaced(StandardMetrics.PROXY_SERVER_UP),
                "a backend that answers the ping must report up 1"
        );
        assertEquals(
                Map.of(attributes("lobby"), 42L),
                collector.replaced(StandardMetrics.PROXY_SERVER_LATENCY),
                "a backend that answers the ping must report its latency in milliseconds"
        );
    }

    @Test
    void unreachableBackendReportsDownWithoutLatencyWhileOthersStillReport() {
        RegisteredServer lobby = backend("lobby");
        RegisteredServer survival = backend("survival");
        when(proxyServer.getAllServers()).thenReturn(List.of(lobby, survival));
        when(lobby.ping(any(PingOptions.class))).thenReturn(CompletableFuture.completedFuture(mock(ServerPing.class)));
        when(survival.ping(any(PingOptions.class)))
                .thenReturn(CompletableFuture.failedFuture(new TimeoutException("no answer")));

        probe.probe();

        assertEquals(
                Map.of(attributes("lobby"), 1L, attributes("survival"), 0L),
                collector.replaced(StandardMetrics.PROXY_SERVER_UP),
                "a failed ping must report up 0 without stopping the other backends"
        );
        assertFalse(
                collector.replaced(StandardMetrics.PROXY_SERVER_LATENCY).containsKey(attributes("survival")),
                "an unreachable backend must have no latency series"
        );
    }

    @Test
    void removedBackendSeriesAreDroppedInTheNextRound() {
        RegisteredServer lobby = backend("lobby");
        RegisteredServer survival = backend("survival");
        when(lobby.ping(any(PingOptions.class))).thenReturn(CompletableFuture.completedFuture(mock(ServerPing.class)));
        when(survival.ping(any(PingOptions.class))).thenReturn(CompletableFuture.completedFuture(mock(ServerPing.class)));
        when(proxyServer.getAllServers()).thenReturn(List.of(lobby, survival));
        probe.probe();

        when(proxyServer.getAllServers()).thenReturn(List.of(survival));
        probe.probe();

        assertEquals(
                Map.of(attributes("survival"), 1L),
                collector.replaced(StandardMetrics.PROXY_SERVER_UP),
                "a backend that is no longer registered must not stay in the up series"
        );
        assertEquals(
                Map.of(attributes("survival"), 0L),
                collector.replaced(StandardMetrics.PROXY_SERVER_LATENCY),
                "a backend that is no longer registered must not stay in the latency series"
        );
    }

    @Test
    void overlappingRoundIsSkippedUntilPreviousRoundFinishes() {
        RegisteredServer lobby = backend("lobby");
        when(proxyServer.getAllServers()).thenReturn(List.of(lobby));
        CompletableFuture<ServerPing> firstPing = new CompletableFuture<>();
        when(lobby.ping(any(PingOptions.class))).thenReturn(firstPing);

        probe.probe();
        probe.probe();
        verify(lobby, times(1)).ping(any(PingOptions.class));

        firstPing.complete(mock(ServerPing.class));
        probe.probe();
        verify(lobby, times(2)).ping(any(PingOptions.class));
    }

    @Test
    void pingUsesFiveSecondTimeout() {
        RegisteredServer lobby = backend("lobby");
        when(proxyServer.getAllServers()).thenReturn(List.of(lobby));
        when(lobby.ping(any(PingOptions.class))).thenReturn(CompletableFuture.completedFuture(mock(ServerPing.class)));

        probe.probe();

        ArgumentCaptor<PingOptions> options = ArgumentCaptor.forClass(PingOptions.class);
        verify(lobby).ping(options.capture());
        assertEquals(
                5000L,
                options.getValue().getTimeout(),
                "each backend ping must time out after 5 seconds"
        );
    }

    private static RegisteredServer backend(String name) {
        RegisteredServer server = mock(RegisteredServer.class);
        when(server.getServerInfo()).thenReturn(new ServerInfo(name, new InetSocketAddress("127.0.0.1", 25565)));
        return server;
    }

    private static Attributes attributes(String server) {
        return Attributes.of(SERVER, server);
    }
}
