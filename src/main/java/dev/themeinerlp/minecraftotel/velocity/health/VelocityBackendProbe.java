package dev.themeinerlp.minecraftotel.velocity.health;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.PingOptions;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import io.opentelemetry.api.common.Attributes;
import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pings registered backends and reports their reachability and latency.
 *
 * @since 1.7.0
 */
public final class VelocityBackendProbe {
    private final ProxyServer proxyServer;
    private final TelemetryCollector collector;
    private final Clock clock;
    private final Duration timeout;
    private final AtomicBoolean roundInFlight = new AtomicBoolean(false);

    /**
     * Creates a probe for the proxy's registered backends.
     *
     * @param proxyServer Velocity proxy server
     * @param collector collector that receives the metrics
     * @param clock clock used to measure latency
     * @param timeout maximum time to wait for one backend
     * @since 1.7.0
     */
    public VelocityBackendProbe(ProxyServer proxyServer, TelemetryCollector collector, Clock clock, Duration timeout) {
        this.proxyServer = proxyServer;
        this.collector = collector;
        this.clock = clock;
        this.timeout = timeout;
    }

    /**
     * Starts one probe round. A round is skipped while the previous one is still waiting for pings.
     * Results replace the whole backend series once every ping of the round has finished.
     *
     * @return future that completes when the round has been published
     * @since 1.7.0
     */
    public CompletableFuture<Void> probe() {
        if (!roundInFlight.compareAndSet(false, true)) {
            return CompletableFuture.completedFuture(null);
        }
        try {
            Map<String, CompletableFuture<Long>> latencies = startPings();
            return CompletableFuture.allOf(latencies.values().toArray(CompletableFuture[]::new))
                    .thenRun(() -> publish(latencies))
                    .whenComplete((ignored, error) -> roundInFlight.set(false));
        } catch (RuntimeException exception) {
            roundInFlight.set(false);
            throw exception;
        }
    }

    private Map<String, CompletableFuture<Long>> startPings() {
        Map<String, CompletableFuture<Long>> latencies = new LinkedHashMap<>();
        for (RegisteredServer server : proxyServer.getAllServers()) {
            latencies.put(server.getServerInfo().getName(), pingLatency(server));
        }
        return latencies;
    }

    private CompletableFuture<Long> pingLatency(RegisteredServer server) {
        long started = clock.millis();
        CompletableFuture<ServerPing> ping;
        try {
            ping = server.ping(PingOptions.builder().timeout(timeout).build());
        } catch (RuntimeException exception) {
            ping = CompletableFuture.failedFuture(exception);
        }
        return ping.handle((result, error) -> error == null ? clock.millis() - started : null);
    }

    private void publish(Map<String, CompletableFuture<Long>> latencies) {
        Map<Attributes, Long> up = new HashMap<>();
        Map<Attributes, Long> latency = new HashMap<>();
        for (Map.Entry<String, CompletableFuture<Long>> entry : latencies.entrySet()) {
            Attributes attributes = Attributes.of(StandardMetrics.SERVER_KEY, entry.getKey());
            Long millis = entry.getValue().join();
            up.put(attributes, millis == null ? 0L : 1L);
            if (millis != null) {
                latency.put(attributes, millis);
            }
        }
        collector.replaceLongGauge(StandardMetrics.PROXY_SERVER_UP, up, StandardMetrics.UNIT_COUNT);
        collector.replaceLongGauge(StandardMetrics.PROXY_SERVER_LATENCY, latency, StandardMetrics.UNIT_MILLIS);
    }
}
