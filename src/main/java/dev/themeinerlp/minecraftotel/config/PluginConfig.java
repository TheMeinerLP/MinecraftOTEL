package dev.themeinerlp.minecraftotel.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginConfig {
    public final boolean enableTick;
    public final boolean enableEntities;
    public final boolean enableChunks;
    public final boolean enableTpsMspt;
    public final boolean preferSpark;
    public final int intervalSeconds;
    public final int baselineScanIntervalSeconds;

    private PluginConfig(
            boolean enableTick,
            boolean enableEntities,
            boolean enableChunks,
            boolean enableTpsMspt,
            boolean preferSpark,
            int intervalSeconds,
            int baselineScanIntervalSeconds
    ) {
        this.enableTick = enableTick;
        this.enableEntities = enableEntities;
        this.enableChunks = enableChunks;
        this.enableTpsMspt = enableTpsMspt;
        this.preferSpark = preferSpark;
        this.intervalSeconds = intervalSeconds;
        this.baselineScanIntervalSeconds = baselineScanIntervalSeconds;
    }

    public static PluginConfig load(JavaPlugin plugin) {
        FileConfiguration cfg = plugin.getConfig();
        boolean enableTick = cfg.getBoolean("otel.enable.tick", true);
        boolean enableEntities = cfg.getBoolean("otel.enable.entities", true);
        boolean enableChunks = cfg.getBoolean("otel.enable.chunks", true);
        boolean enableTpsMspt = cfg.getBoolean("otel.enable.tpsMspt", true);
        boolean preferSpark = cfg.getBoolean("otel.preferSpark", true);
        int intervalSeconds = clamp(cfg.getInt("sampling.intervalSeconds", 1), 1, 60);
        int baselineScanIntervalSeconds = clamp(
                cfg.getInt("sampling.baselineScanIntervalSeconds", 10),
                5,
                300
        );
        return new PluginConfig(
                enableTick,
                enableEntities,
                enableChunks,
                enableTpsMspt,
                preferSpark,
                intervalSeconds,
                baselineScanIntervalSeconds
        );
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
