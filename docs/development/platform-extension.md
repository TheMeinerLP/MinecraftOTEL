# Platform Implementation Guide

This guide is for adding a new platform integration (a new server or proxy type) to
MinecraftOTEL itself. If you only want to add custom metrics from another plugin, use
`docs/development/integrations.md` instead.

## High-Level Steps
1. Create platform-specific snapshot types that implement `TelemetrySnapshot` and
   `TelemetrySnapshotBuilder`.
2. Implement `TelemetryService` for your platform. It should:
   - Build a `Meter` via `GlobalOpenTelemetry`.
   - Maintain any platform state required for sampling.
   - Emit standard metrics via `StandardSnapshotTelemetrySampler`.
   - Schedule the sampling task and emit snapshots.
3. Add a platform API implementation of `MinecraftOtelApi`.
4. Register the API in your plugin entrypoint and start the telemetry service.

## Reference Implementations
Use the existing implementations as a blueprint:
- Paper telemetry service: `src/main/java/dev/themeinerlp/minecraftotel/paper/PaperTelemetryService.java`
- Velocity telemetry service: `src/main/java/dev/themeinerlp/minecraftotel/velocity/service/VelocityTelemetryService.java`
- Paper API implementation: `src/main/java/dev/themeinerlp/minecraftotel/paper/api/PaperMinecraftOtelApi.java`
- Velocity API implementation: `src/main/java/dev/themeinerlp/minecraftotel/velocity/api/VelocityMinecraftOtelApi.java`
- Plugin entrypoints:
  - Paper: `src/main/java/dev/themeinerlp/minecraftotel/paper/MinecraftOTELPaperPlugin.java`
  - Velocity: `src/main/java/dev/themeinerlp/minecraftotel/velocity/MinecraftOTELVelocityPlugin.java`

## Minimal Plugin Entry Skeleton
```java
public final class MyPlatformPlugin {
    private MinecraftOtelApi api;
    private MyPlatformTelemetryService telemetryService;

    public void onEnable() {
        telemetryService = new MyPlatformTelemetryService(/* platform deps */);
        api = new MyPlatformMinecraftOtelApi("1.0.0", telemetryService);

        // Register with the platform services manager if available.
        // Example (Paper): getServer().getServicesManager().register(...)

        MinecraftOtelApiProvider.register(api);
        telemetryService.start();
    }

    public void onDisable() {
        if (telemetryService != null) {
            telemetryService.stop();
        }
        if (api != null) {
            MinecraftOtelApiProvider.unregister(api);
        }
    }
}
```

## Example: Minestom
Minestom extensions use `net.minestom.server.extensions.Extension`. Wire the telemetry service
to the extension lifecycle.

```java
import dev.themeinerlp.minecraftotel.api.core.MinecraftOtelApi;
import dev.themeinerlp.minecraftotel.api.core.MinecraftOtelApiProvider;
import net.minestom.server.extensions.Extension;

public final class MinestomOtelExtension extends Extension {
    private MinecraftOtelApi api;
    private MinestomTelemetryService telemetryService;

    @Override
    public void initialize() {
        telemetryService = new MinestomTelemetryService(/* server refs */);
        api = new MinestomMinecraftOtelApi(getOrigin().getVersion(), telemetryService);
        MinecraftOtelApiProvider.register(api);
        telemetryService.start();
    }

    @Override
    public void terminate() {
        if (telemetryService != null) {
            telemetryService.stop();
        }
        if (api != null) {
            MinecraftOtelApiProvider.unregister(api);
        }
    }
}
```

## Example: Geyser (Standalone)
For a standalone Geyser integration, wire the telemetry lifecycle into the Geyser bootstrap or
plugin entrypoint. If you run Geyser on top of Velocity/Paper, prefer the integration approach
in `docs/development/integrations.md`.

```java
import dev.themeinerlp.minecraftotel.api.core.MinecraftOtelApi;
import dev.themeinerlp.minecraftotel.api.core.MinecraftOtelApiProvider;

public final class GeyserOtelBootstrap {
    private MinecraftOtelApi api;
    private GeyserTelemetryService telemetryService;

    public void start(/* geyser runtime */) {
        telemetryService = new GeyserTelemetryService(/* geyser refs */);
        api = new GeyserMinecraftOtelApi("1.0.0", telemetryService);
        MinecraftOtelApiProvider.register(api);
        telemetryService.start();
    }

    public void stop() {
        if (telemetryService != null) {
            telemetryService.stop();
        }
        if (api != null) {
            MinecraftOtelApiProvider.unregister(api);
        }
    }
}
```

## Example: Platform Metric Extension
Platform modules can emit extra metrics by adding samplers after the service starts.

```java
telemetryService.addSampler((snapshot, collector) -> {
    // Example: export a custom gauge based on platform-native state.
    collector.recordLongGauge("myplatform.sessions", 42);
});
```

## Notes
- Use `StandardSnapshotTelemetrySampler` to keep metric naming consistent.
- Keep sampling work lightweight; avoid blocking operations in the sampler thread.
