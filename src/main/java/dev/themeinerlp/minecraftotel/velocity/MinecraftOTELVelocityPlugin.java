package dev.themeinerlp.minecraftotel.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.themeinerlp.minecraftotel.api.core.MinecraftOtelApi;
import dev.themeinerlp.minecraftotel.api.core.MinecraftOtelApiProvider;
import dev.themeinerlp.minecraftotel.velocity.service.VelocityTelemetryService;
import dev.themeinerlp.minecraftotel.velocity.api.VelocityMinecraftOtelApi;
import java.nio.file.Path;
import org.slf4j.Logger;

/**
 * Velocity entrypoint that wires the telemetry service into the proxy lifecycle.
 */
public final class MinecraftOTELVelocityPlugin {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private MinecraftOtelApi api;
    private VelocityTelemetryService telemetryService;

    @Inject
    public MinecraftOTELVelocityPlugin(
            ProxyServer proxyServer,
            Logger logger,
            @DataDirectory Path dataDirectory
    ) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        String version = resolveVersion();
        telemetryService = new VelocityTelemetryService(
                proxyServer,
                logger,
                dataDirectory,
                version
        );
        api = new VelocityMinecraftOtelApi(version, telemetryService);
        MinecraftOtelApiProvider.register(api);
        telemetryService.start();

        // Example:
        // api.getTelemetryService().addSampler((snapshot, collector) -> {
        //     if (snapshot instanceof dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshot velocitySnapshot) {
        //         collector.recordLongGauge("myproxy.players", velocitySnapshot.playersOnline());
        //     }
        // });
        // api.getTelemetryService().addSnapshotSampler(builder -> {
        //     if (builder instanceof dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshotBuilder velocityBuilder) {
        //         velocityBuilder.setRegisteredServers(5);
        //     }
        // });
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (telemetryService != null) {
            telemetryService.stop();
            telemetryService = null;
        }
        if (api != null) {
            MinecraftOtelApiProvider.unregister(api);
            api = null;
        }
    }

    private String resolveVersion() {
        String version = getClass().getPackage().getImplementationVersion();
        return version == null ? "unknown" : version;
    }
}
