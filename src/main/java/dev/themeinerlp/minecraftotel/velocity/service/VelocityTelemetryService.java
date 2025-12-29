package dev.themeinerlp.minecraftotel.velocity.service;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.themeinerlp.minecraftotel.api.TelemetryListener;
import dev.themeinerlp.minecraftotel.api.TelemetryService;
import dev.themeinerlp.minecraftotel.api.TelemetrySnapshot;
import dev.themeinerlp.minecraftotel.velocity.config.VelocityPluginConfig;
import dev.themeinerlp.minecraftotel.velocity.metrics.VelocityMetricsRegistry;
import dev.themeinerlp.minecraftotel.velocity.sampler.VelocityProxySampler;
import dev.themeinerlp.minecraftotel.velocity.sampler.VelocitySampler;
import dev.themeinerlp.minecraftotel.velocity.state.VelocityTelemetryState;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;

/**
 * Coordinates config loading, metrics registration, and sampling for Velocity.
 */
public final class VelocityTelemetryService implements TelemetryService {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private final String version;
    private final List<TelemetryListener> listeners;
    private VelocityPluginConfig config;
    private VelocityTelemetryState state;
    private VelocitySampler sampler;
    private ScheduledTask samplingTask;
    private volatile boolean running;

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
        this.listeners = new CopyOnWriteArrayList<>();
        this.state = new VelocityTelemetryState();
    }

    /**
     * Initializes metrics and starts the periodic sampling task.
     */
    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        this.config = VelocityPluginConfig.load(
                dataDirectory,
                logger,
                getClass().getClassLoader()
        );
        this.sampler = new VelocityProxySampler(proxyServer, config);
        new VelocityMetricsRegistry(version, state, config);

        sampleOnce();
        startSamplingTask();

        logger.info("Velocity sampling interval: {}s", config.intervalSeconds);
        logger.info("Velocity per-server players enabled: {}", config.enablePlayersPerServer);
        logger.info("Velocity server count enabled: {}", config.enableServerCount);
    }

    @Override
    public String getPlatform() {
        return "velocity";
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public TelemetrySnapshot getSnapshot() {
        return state.getSnapshot();
    }

    @Override
    public void addListener(TelemetryListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }

    @Override
    public void removeListener(TelemetryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Stops the periodic sampling task.
     */
    @Override
    public void stop() {
        running = false;
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
        TelemetrySnapshot snapshot = sampler.sample();
        state.setSnapshot(snapshot);
        for (TelemetryListener listener : listeners) {
            listener.onSample(snapshot);
        }
    }
}
