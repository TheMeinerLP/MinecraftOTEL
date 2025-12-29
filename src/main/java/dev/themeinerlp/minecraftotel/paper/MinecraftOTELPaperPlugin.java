package dev.themeinerlp.minecraftotel.paper;

import dev.themeinerlp.minecraftotel.api.MinecraftOtelApi;
import dev.themeinerlp.minecraftotel.api.MinecraftOtelApiProvider;
import dev.themeinerlp.minecraftotel.paper.api.PaperMinecraftOtelApi;
import dev.themeinerlp.minecraftotel.paper.config.PluginConfig;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;

/**
 * Paper plugin entrypoint for MinecraftOTEL.
 */
public final class MinecraftOTELPaperPlugin extends JavaPlugin {
    private MinecraftOtelApi api;
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
        api = new PaperMinecraftOtelApi(getDescription().getVersion(), telemetryService);
        getServer().getServicesManager().register(MinecraftOtelApi.class, api, this, ServicePriority.Normal);
        MinecraftOtelApiProvider.register(api);
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
        if (api != null) {
            getServer().getServicesManager().unregister(MinecraftOtelApi.class, api);
            MinecraftOtelApiProvider.unregister(api);
            api = null;
        }
    }
}
