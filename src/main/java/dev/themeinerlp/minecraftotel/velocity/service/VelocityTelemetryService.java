package dev.themeinerlp.minecraftotel.velocity.service;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.themeinerlp.minecraftotel.velocity.config.VelocityPluginConfig;
import dev.themeinerlp.minecraftotel.velocity.metrics.VelocityMetricsRegistry;
import dev.themeinerlp.minecraftotel.velocity.sampler.VelocityProxySampler;
import dev.themeinerlp.minecraftotel.velocity.sampler.VelocitySampler;
import dev.themeinerlp.minecraftotel.velocity.state.VelocityTelemetryState;
import java.nio.file.Path;
import java.time.Duration;
import org.slf4j.Logger;

/**
 * Coordinates config loading, metrics registration, and sampling for Velocity.
 */
public final class VelocityTelemetryService {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private final String version;
    private VelocityPluginConfig config;
    private VelocityTelemetryState state;
    private VelocitySampler sampler;
    private ScheduledTask samplingTask;

    /**
     * Creates a telemetry coordinator for a Velocity proxy instance.
     *
     * @param proxyServer Velocity proxy server
     * @param logger logger for lifecycle messages
     * @param dataDirectory data directory for config persistence
     * @param version plugin version for instrumentation metadata
     */
    public VelocityTelemetryService(
            ProxyServer proxyServer,
            Logger logger,
            Path dataDirectory,
            String version
    ) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.version = version;
    }

    /**
     * Initializes metrics and starts the periodic sampling task.
     */
    public void start() {
        this.config = VelocityPluginConfig.load(
                dataDirectory,
                logger,
                getClass().getClassLoader()
        );
        this.state = new VelocityTelemetryState();
        this.sampler = new VelocityProxySampler(proxyServer, config);
        new VelocityMetricsRegistry(version, state, config);

        sampleOnce();
        startSamplingTask();

        logger.info("Velocity sampling interval: {}s", config.intervalSeconds);
        logger.info("Velocity per-server players enabled: {}", config.enablePlayersPerServer);
        logger.info("Velocity server count enabled: {}", config.enableServerCount);
    }

    /**
     * Stops the periodic sampling task.
     */
    public void stop() {
        if (samplingTask != null) {
            samplingTask.cancel();
            samplingTask = null;
        }
    }

    private void startSamplingTask() {
        samplingTask = proxyServer.getScheduler()
                .buildTask(this, this::sampleOnce)
                .repeat(Duration.ofSeconds(config.intervalSeconds))
                .schedule();
    }

    private void sampleOnce() {
        state.setSnapshot(sampler.sample());
    }
}
