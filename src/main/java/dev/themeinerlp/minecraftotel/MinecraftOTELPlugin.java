package dev.themeinerlp.minecraftotel;

import dev.themeinerlp.minecraftotel.config.PluginConfig;
import dev.themeinerlp.minecraftotel.listeners.ChunkCounterListener;
import dev.themeinerlp.minecraftotel.listeners.EntityCounterListener;
import dev.themeinerlp.minecraftotel.metrics.MetricsRegistry;
import dev.themeinerlp.minecraftotel.sampler.PaperSampler;
import dev.themeinerlp.minecraftotel.sampler.ServerSampler;
import dev.themeinerlp.minecraftotel.sampler.SparkSampler;
import dev.themeinerlp.minecraftotel.state.TelemetrySnapshot;
import dev.themeinerlp.minecraftotel.state.TelemetryState;
import dev.themeinerlp.minecraftotel.tick.TickDurationRecorder;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftOTELPlugin extends JavaPlugin {
    private boolean openTelemetryAvailable = true;
    private PluginConfig config;
    private TelemetryState state;
    private MetricsRegistry metrics;
    private TickDurationRecorder tickDurationRecorder;
    private ServerSampler sampler;
    private long lastBaselineMillis;

    @Override
    public void onLoad() {
        openTelemetryAvailable = isOpenTelemetryAvailable();
        if (!openTelemetryAvailable) {
            getLogger().severe("OpenTelemetry API not found on the classpath. Plugin will be disabled.");
        }
    }

    @Override
    public void onEnable() {
        if (!openTelemetryAvailable) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        this.config = PluginConfig.load(this);

        this.state = new TelemetryState();
        this.state.baselineInit(getServer());

        this.metrics = new MetricsRegistry(this, state);

        if (config.enableChunks) {
            getServer().getPluginManager().registerEvents(
                    new ChunkCounterListener(state, metrics),
                    this
            );
        }

        boolean paperEntityEventsAvailable = config.enableEntities;
        if (config.enableEntities) {
            state.setPaperEntityEventsAvailable(true);
            getServer().getPluginManager().registerEvents(
                    new EntityCounterListener(state, metrics),
                    this
            );
        } else {
            state.setPaperEntityEventsAvailable(false);
        }

        boolean paperTickEventsAvailable = config.enableTick;
        if (config.enableTick) {
            tickDurationRecorder = new TickDurationRecorder(this, metrics.getTickDurationHistogram());
            tickDurationRecorder.start();
        }

        ServerSampler paperSampler = new PaperSampler();
        ServerSampler sparkSampler = new SparkSampler(getServer(), paperSampler);
        if (config.preferSpark && sparkSampler.isAvailable()) {
            sampler = sparkSampler;
        } else {
            sampler = paperSampler;
        }

        startSamplingTask();

        getLogger().info("Sampler: " + samplerName(paperSampler, sparkSampler));
        getLogger().info("Paper entity events available: " + paperEntityEventsAvailable);
        getLogger().info("Paper tick events available: " + paperTickEventsAvailable);
    }

    @Override
    public void onDisable() {
        if (tickDurationRecorder != null) {
            tickDurationRecorder.stop();
        }
        Bukkit.getScheduler().cancelTasks(this);
    }

    private void startSamplingTask() {
        long intervalTicks = config.intervalSeconds * 20L;
        Bukkit.getScheduler().runTaskTimer(
                this,
                () -> {
                    long playersOnline = getServer().getOnlinePlayers().size();
                    ServerSampler.SampleResult sampleResult = config.enableTpsMspt
                            ? sampler.sample(getServer())
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
                    } else if (baselineDue && !state.isPaperEntityEventsAvailable()) {
                        baselineEntities = state.scanEntities(getServer());
                    }

                    Map<String, Long> baselineChunks = null;
                    if (!config.enableChunks) {
                        baselineChunks = Map.of();
                    } else if (baselineDue) {
                        baselineChunks = state.scanChunks(getServer());
                    }

                    TelemetrySnapshot snapshot = state.rebuildSnapshot(
                            playersOnline,
                            sampleResult.tpsNullable(),
                            sampleResult.msptAvgNullable(),
                            sampleResult.msptP95Nullable(),
                            baselineEntities,
                            baselineChunks
                    );
                    state.setSnapshot(snapshot);
                },
                0L,
                intervalTicks
        );
    }

    private String samplerName(ServerSampler paperSampler, ServerSampler sparkSampler) {
        if (sampler == sparkSampler) {
            return "spark";
        }
        if (paperSampler.isAvailable()) {
            return "paper";
        }
        return "none";
    }

    private boolean isOpenTelemetryAvailable() {
        try {
            Class.forName("io.opentelemetry.api.GlobalOpenTelemetry", false, getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
