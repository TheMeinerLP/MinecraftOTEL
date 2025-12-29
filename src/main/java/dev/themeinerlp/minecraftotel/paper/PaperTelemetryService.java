package dev.themeinerlp.minecraftotel.paper;

import dev.themeinerlp.minecraftotel.api.TelemetryListener;
import dev.themeinerlp.minecraftotel.api.TelemetryService;
import dev.themeinerlp.minecraftotel.api.TelemetrySnapshot;
import dev.themeinerlp.minecraftotel.paper.config.PluginConfig;
import dev.themeinerlp.minecraftotel.paper.listeners.ChunkCounterListener;
import dev.themeinerlp.minecraftotel.paper.listeners.EntityCounterListener;
import dev.themeinerlp.minecraftotel.paper.metrics.MetricsRegistry;
import dev.themeinerlp.minecraftotel.paper.sampler.PaperSampler;
import dev.themeinerlp.minecraftotel.paper.sampler.ServerSampler;
import dev.themeinerlp.minecraftotel.paper.sampler.SparkSampler;
import dev.themeinerlp.minecraftotel.paper.state.TelemetryState;
import dev.themeinerlp.minecraftotel.paper.tick.TickDurationRecorder;
import java.util.List;
import java.util.Map;
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
    private final List<TelemetryListener> listeners;
    private TickDurationRecorder tickDurationRecorder;
    private ServerSampler sampler;
    private long lastBaselineMillis;
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
        state.baselineInit(plugin.getServer());
        MetricsRegistry metrics = new MetricsRegistry(plugin, state);

        if (config.enableChunks) {
            plugin.getServer().getPluginManager().registerEvents(
                    new ChunkCounterListener(state, metrics),
                    plugin
            );
        }

        if (config.enableEntities) {
            state.setEntityEventsAvailable(true);
            plugin.getServer().getPluginManager().registerEvents(
                    new EntityCounterListener(state, metrics),
                    plugin
            );
        } else {
            state.setEntityEventsAvailable(false);
        }

        if (config.enableTick) {
            tickDurationRecorder = new TickDurationRecorder(plugin, metrics.getTickDurationHistogram());
            tickDurationRecorder.start();
        }

        ServerSampler paperSampler = new PaperSampler();
        ServerSampler sparkSampler = null;
        if (config.preferSpark && isSparkInstalled()) {
            sparkSampler = new SparkSampler(paperSampler);
        }
        if (sparkSampler != null && sparkSampler.isAvailable()) {
            sampler = sparkSampler;
        } else {
            sampler = paperSampler;
        }

        startSamplingTask();

        plugin.getLogger().info("Sampler: " + samplerName(paperSampler, sparkSampler));
        plugin.getLogger().info("Paper entity events enabled: " + config.enableEntities);
        plugin.getLogger().info("Paper tick events enabled: " + config.enableTick);
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
                    long playersOnline = plugin.getServer().getOnlinePlayers().size();
                    ServerSampler.SampleResult sampleResult = config.enableTpsMspt
                            ? sampler.sample(plugin.getServer())
                            : ServerSampler.SampleResult.empty();

                    long now = System.currentTimeMillis();
                    boolean baselineDue = lastBaselineMillis == 0L
                            || now - lastBaselineMillis >= config.baselineScanIntervalSeconds * 1000L;
                    if (baselineDue) {
                        lastBaselineMillis = now;
                    }

                    Map<String, Long> baselineEntities = null;
                    if (!config.enableEntities) {
                        baselineEntities = Map.of();
                    } else if (baselineDue && !state.isEntityEventsAvailable()) {
                        baselineEntities = state.scanEntities(plugin.getServer());
                    }

                    Map<String, Long> baselineChunks = null;
                    if (!config.enableChunks) {
                        baselineChunks = Map.of();
                    } else if (baselineDue) {
                        baselineChunks = state.scanChunks(plugin.getServer());
                    }

                    TelemetrySnapshot snapshot = state.rebuildSnapshot(
                            playersOnline,
                            sampleResult.tpsNullable(),
                            sampleResult.msptAvgNullable(),
                            sampleResult.msptP95Nullable(),
                            baselineEntities,
                            baselineChunks
                    );
                    updateSnapshot(snapshot);
                },
                0L,
                intervalTicks
        );
    }

    private String samplerName(ServerSampler paperSampler, ServerSampler sparkSampler) {
        if (sparkSampler != null && sampler == sparkSampler) {
            return "spark";
        }
        if (paperSampler.isAvailable()) {
            return "paper";
        }
        return "none";
    }

    private boolean isSparkInstalled() {
        var plugin = this.plugin.getServer().getPluginManager().getPlugin("spark");
        return plugin != null && plugin.isEnabled();
    }

    private void updateSnapshot(TelemetrySnapshot snapshot) {
        state.setSnapshot(snapshot);
        for (TelemetryListener listener : listeners) {
            listener.onSample(snapshot);
        }
    }
}
