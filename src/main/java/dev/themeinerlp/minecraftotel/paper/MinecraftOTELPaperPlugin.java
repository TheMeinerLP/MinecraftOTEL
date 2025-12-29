package dev.themeinerlp.minecraftotel.paper;

import dev.themeinerlp.minecraftotel.paper.config.PluginConfig;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper plugin entrypoint for MinecraftOTEL.
 */
public final class MinecraftOTELPaperPlugin extends JavaPlugin {
    private PaperTelemetryService telemetryService;

    /**
     * Initializes configuration and starts telemetry collection.
     */
    @Override
    public void onEnable() {
        reloadConfig();
        saveDefaultConfig();
        PluginConfig config = PluginConfig.load(this);
        telemetryService = new PaperTelemetryService(this, config);
        telemetryService.start();
    }

    /**
     * Stops telemetry collection and cleans up resources.
     */
    @Override
    public void onDisable() {
        if (telemetryService != null) {
            telemetryService.stop();
            telemetryService = null;
        }
    }
}
