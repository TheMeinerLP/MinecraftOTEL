package dev.themeinerlp.minecraftotel.paper;

import dev.themeinerlp.minecraftotel.api.collector.MeterTelemetryCollector;
import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.sampler.TelemetrySampler;
import dev.themeinerlp.minecraftotel.api.service.TelemetryListener;
import dev.themeinerlp.minecraftotel.api.service.TelemetryService;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshotSampler;
import dev.themeinerlp.minecraftotel.paper.metrics.PaperStandardSnapshotTelemetrySampler;
import dev.themeinerlp.minecraftotel.paper.config.PluginConfig;
import dev.themeinerlp.minecraftotel.paper.listeners.ChunkCounterListener;
import dev.themeinerlp.minecraftotel.paper.listeners.EntityCounterListener;
import dev.themeinerlp.minecraftotel.paper.sampler.PaperSnapshotSampler;
import dev.themeinerlp.minecraftotel.paper.snapshot.PaperTelemetrySnapshotBuilder;
import dev.themeinerlp.minecraftotel.paper.state.TelemetryState;
import dev.themeinerlp.minecraftotel.paper.tick.TickDurationRecorder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Orchestrates sampling, listeners, and metrics for the Paper plugin.
 */
public final class PaperTelemetryService implements TelemetryService {
    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final TelemetryState state;
    private final TelemetryCollector collector;
    private final List<TelemetrySnapshotSampler> snapshotSamplers;
    private final List<TelemetrySampler> samplers;
    private final List<TelemetryListener> listeners;
    private TickDurationRecorder tickDurationRecorder;
    private PaperSnapshotSampler snapshotSampler;
    private volatile boolean running;

    /**
     * Creates a telemetry service bound to a plugin and configuration.
     *
     * @param plugin plugin instance
     * @param config loaded configuration
     */
    public PaperTelemetryService(JavaPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.state = new TelemetryState();
        Meter meter = GlobalOpenTelemetry.get()
                .meterBuilder("minecraft-otel-paper")
                .setInstrumentationVersion(plugin.getPluginMeta().getVersion())
                .build();
        this.collector = new MeterTelemetryCollector(meter);
        this.snapshotSamplers = new CopyOnWriteArrayList<>();
        this.samplers = new CopyOnWriteArrayList<>();
        this.samplers.add(new PaperStandardSnapshotTelemetrySampler());
        this.listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Initializes listeners, metrics, and periodic sampling.
     */
    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        state.setEntityTypeChunkMode(config.entitiesByChunkMode);
        state.baselineInit(plugin.getServer());

        if (config.enableChunks) {
            plugin.getServer().getPluginManager().registerEvents(
                    new ChunkCounterListener(state, collector),
                    plugin
            );
        }

        if (config.enableEntities) {
            state.setEntityEventsAvailable(true);
            plugin.getServer().getPluginManager().registerEvents(
                    new EntityCounterListener(state, collector),
                    plugin
            );
        } else {
            state.setEntityEventsAvailable(false);
        }

        if (config.enableTick) {
            tickDurationRecorder = new TickDurationRecorder(plugin, collector);
            tickDurationRecorder.start();
        }

        snapshotSampler = new PaperSnapshotSampler(plugin.getServer(), config, state);
        snapshotSamplers.add(snapshotSampler);

        startSamplingTask();

        plugin.getSLF4JLogger().info("Sampler: {}", snapshotSampler.samplerName());
        plugin.getSLF4JLogger().info("Paper entity events enabled: {}", config.enableEntities);
        plugin.getSLF4JLogger().info("Paper tick events enabled: {}", config.enableTick);
    }

    @Override
    public String getPlatform() {
        return "paper";
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
     * Stops sampling and unregisters listeners.
     */
    @Override
    public void stop() {
        running = false;
        if (tickDurationRecorder != null) {
            tickDurationRecorder.stop();
        }
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    private void startSamplingTask() {
        long intervalTicks = config.intervalSeconds * 20L;
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {
                    PaperTelemetrySnapshotBuilder builder = new PaperTelemetrySnapshotBuilder();
                    for (TelemetrySnapshotSampler sampler : snapshotSamplers) {
                        sampler.sample(builder);
                    }
                    updateSnapshot(builder.build());
                },
                0L,
                intervalTicks
        );
    }

    private void updateSnapshot(TelemetrySnapshot snapshot) {
        state.setSnapshot(snapshot);
        for (TelemetryListener listener : listeners) {
            listener.onSample(snapshot);
        }
        runSamplers(snapshot);
    }

    private void runSamplers(TelemetrySnapshot snapshot) {
        for (TelemetrySampler sampler : samplers) {
            sampler.sample(snapshot, collector);
        }
    }
}
