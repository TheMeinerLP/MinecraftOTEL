package dev.themeinerlp.minecraftotel.paper;

import dev.themeinerlp.minecraftotel.paper.config.PluginConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftOTELPaperPlugin extends JavaPlugin {
    private PaperTelemetryService telemetryService;

    @Override
    public void onEnable() {
        reloadConfig();
        saveDefaultConfig();
        PluginConfig config = PluginConfig.load(this);
        telemetryService = new PaperTelemetryService(this, config);
        telemetryService.start();
    }

    @Override
    public void onDisable() {
        if (telemetryService != null) {
            telemetryService.stop();
            telemetryService = null;
        }
    }
}
