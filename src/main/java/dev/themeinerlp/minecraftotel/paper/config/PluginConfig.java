package dev.themeinerlp.minecraftotel.paper.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Immutable configuration for the Paper plugin loaded from config.yml.
 */
public final class PluginConfig {
    /** Enables tick duration histogram sampling. */
    public final boolean enableTick;
    /** Enables entity gauges and add/remove counters. */
    public final boolean enableEntities;
    /** Mode for entity type per chunk tracking. */
    public final EntitiesByChunkMode entitiesByChunkMode;
    /** Enables chunk gauges and load/unload counters. */
    public final boolean enableChunks;
    /** Enables TPS and MSPT sampling. */
    public final boolean enableTpsMspt;
    /** Prefers Spark as a sampler when available. */
    public final boolean preferSpark;
    /** Sampling interval in seconds. */
    public final int intervalSeconds;
    /** Baseline scan interval in seconds. */
    public final int baselineScanIntervalSeconds;

    private PluginConfig(
            boolean enableTick,
            boolean enableEntities,
            EntitiesByChunkMode entitiesByChunkMode,
            boolean enableChunks,
            boolean enableTpsMspt,
            boolean preferSpark,
            int intervalSeconds,
            int baselineScanIntervalSeconds
    ) {
        this.enableTick = enableTick;
        this.enableEntities = enableEntities;
        this.entitiesByChunkMode = entitiesByChunkMode;
        this.enableChunks = enableChunks;
        this.enableTpsMspt = enableTpsMspt;
        this.preferSpark = preferSpark;
        this.intervalSeconds = intervalSeconds;
        this.baselineScanIntervalSeconds = baselineScanIntervalSeconds;
    }

    /**
     * Loads the plugin configuration from Bukkit config.yml.
     *
     * @param plugin owning plugin instance
     * @return loaded configuration with defaults applied
     */
    public static PluginConfig load(JavaPlugin plugin) {
        FileConfiguration cfg = plugin.getConfig();
        boolean enableTick = cfg.getBoolean("otel.enable.tick", true);
        boolean enableEntities = cfg.getBoolean("otel.enable.entities", true);
        EntitiesByChunkMode entitiesByChunkMode = EntitiesByChunkMode.fromString(
                cfg.getString("otel.entitiesByChunk.mode", "")
        );
        if (entitiesByChunkMode == null) {
            boolean enableEntitiesByChunk = cfg.getBoolean("otel.enable.entitiesByChunk", false);
            entitiesByChunkMode = enableEntitiesByChunk
                    ? EntitiesByChunkMode.HEAVY
                    : EntitiesByChunkMode.OFF;
        }
        boolean enableChunks = cfg.getBoolean("otel.enable.chunks", true);
        boolean enableTpsMspt = cfg.getBoolean("otel.enable.tpsMspt", true);
        boolean preferSpark = cfg.getBoolean("otel.preferSpark", true);
        int intervalSeconds = clamp(cfg.getInt("sampling.intervalSeconds", 1), 1, 60);
        int baselineScanIntervalSeconds = clamp(
                cfg.getInt("sampling.baselineScanIntervalSeconds", 10),
                5,
                300
        );
        if (!enableEntities) {
            entitiesByChunkMode = EntitiesByChunkMode.OFF;
        }
        return new PluginConfig(
                enableTick,
                enableEntities,
                entitiesByChunkMode,
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
