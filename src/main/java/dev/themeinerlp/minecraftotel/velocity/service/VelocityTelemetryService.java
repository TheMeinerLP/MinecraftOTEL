package dev.themeinerlp.minecraftotel.velocity.service;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.themeinerlp.minecraftotel.api.collector.MeterTelemetryCollector;
import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.sampler.TelemetrySampler;
import dev.themeinerlp.minecraftotel.api.service.TelemetryListener;
import dev.themeinerlp.minecraftotel.api.service.TelemetryService;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshotSampler;
import dev.themeinerlp.minecraftotel.api.state.TelemetrySnapshotStore;
import dev.themeinerlp.minecraftotel.api.state.TelemetryStateStore;
import dev.themeinerlp.minecraftotel.metrics.StandardSnapshotTelemetrySampler;
import dev.themeinerlp.minecraftotel.velocity.config.VelocityPluginConfig;
import dev.themeinerlp.minecraftotel.velocity.sampler.VelocitySnapshotSampler;
import dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshot;
import dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshotBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
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
    private final TelemetryCollector collector;
    private final List<TelemetrySnapshotSampler> snapshotSamplers;
    private final List<TelemetrySampler> samplers;
    private final List<TelemetryListener> listeners;
    private VelocityPluginConfig config;
    private final TelemetryStateStore state;
    private VelocitySnapshotSampler snapshotSampler;
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
        Meter meter = GlobalOpenTelemetry.get()
                .meterBuilder("minecraft-otel-velocity")
                .setInstrumentationVersion(version)
                .build();
        this.collector = new MeterTelemetryCollector(meter);
        this.snapshotSamplers = new CopyOnWriteArrayList<>();
        this.samplers = new CopyOnWriteArrayList<>();
        this.samplers.add(new StandardSnapshotTelemetrySampler());
        this.listeners = new CopyOnWriteArrayList<>();
        this.state = new TelemetrySnapshotStore(VelocityTelemetrySnapshot.empty());
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
        this.snapshotSampler = new VelocitySnapshotSampler(proxyServer, config);
        this.snapshotSamplers.add(snapshotSampler);

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
    public void addSnapshotSampler(TelemetrySnapshotSampler sampler) {
        if (sampler == null) {
            return;
        }
        snapshotSamplers.add(sampler);
    }

    @Override
    public void removeSnapshotSampler(TelemetrySnapshotSampler sampler) {
        snapshotSamplers.remove(sampler);
    }

    @Override
    public void addSampler(TelemetrySampler sampler) {
        if (sampler == null) {
            return;
        }
        samplers.add(sampler);
    }

    @Override
    public void removeSampler(TelemetrySampler sampler) {
        samplers.remove(sampler);
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
        VelocityTelemetrySnapshotBuilder builder = new VelocityTelemetrySnapshotBuilder();
        for (TelemetrySnapshotSampler sampler : snapshotSamplers) {
            sampler.sample(builder);
        }
        TelemetrySnapshot snapshot = builder.build();
        state.setSnapshot(snapshot);
        for (TelemetryListener listener : listeners) {
            listener.onSample(snapshot);
        }
        for (TelemetrySampler sampler : samplers) {
            sampler.sample(snapshot, collector);
        }
    }
}
