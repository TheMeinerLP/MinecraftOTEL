# Integrations and API Usage

MinecraftOTEL exposes a small API so other plugins can create meters or react to telemetry
snapshots. The API is identical on Paper and Velocity.

## Common Pattern
- Resolve the API via `ServicesManager` or `MinecraftOtelApiProvider`.
- Use `api.getMeter("your-plugin")` for custom metrics.
- Use `api.getTelemetryService().addSampler(...)` to emit metrics on each sampling tick.
- Use `api.getTelemetryService().addListener(...)` to react to snapshots.
- Use `api.getTelemetryService().addSnapshotSampler(...)` to inject extra data into snapshots.

## Paper Example
```java
import dev.themeinerlp.minecraftotel.api.core.MinecraftOtelApi;
import dev.themeinerlp.minecraftotel.api.core.MinecraftOtelApiProvider;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import dev.themeinerlp.minecraftotel.paper.snapshot.PaperTelemetrySnapshot;
import dev.themeinerlp.minecraftotel.paper.snapshot.PaperTelemetrySnapshotBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        MinecraftOtelApi api = getServer().getServicesManager().load(MinecraftOtelApi.class);
        if (api == null) {
            api = MinecraftOtelApiProvider.get().orElse(null);
        }
        if (api == null) {
            getLogger().warning("MinecraftOTEL API not available.");
            return;
        }

        Meter meter = api.getMeter("my-plugin");
        LongCounter counter = meter.counterBuilder("myplugin.events_total").build();
        counter.add(1);

        api.getTelemetryService().addListener(snapshot -> {
            if (snapshot instanceof PaperTelemetrySnapshot paperSnapshot) {
                long players = paperSnapshot.playersOnline();
                // Use snapshot fields to enrich your own metrics or logs.
            }
        });

        api.getTelemetryService().addSampler((snapshot, collector) -> {
            if (snapshot instanceof PaperTelemetrySnapshot paperSnapshot) {
                collector.recordLongGauge(
                    "myplugin.players.last",
                    paperSnapshot.playersOnline(),
                    StandardMetrics.UNIT_COUNT,
                    io.opentelemetry.api.common.Attributes.empty()
                );
            }
        });

        api.getTelemetryService().addSnapshotSampler(builder -> {
            if (builder instanceof PaperTelemetrySnapshotBuilder paperBuilder) {
                paperBuilder.setPlayersOnline(123);
            }
        });
    }
}
```

## Velocity Example
```java
import dev.themeinerlp.minecraftotel.api.core.MinecraftOtelApiProvider;
import dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshot;
import dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshotBuilder;
import io.opentelemetry.api.metrics.Meter;

public final class MyPlugin {
    public void onProxyInitialize() {
        MinecraftOtelApiProvider.get().ifPresent(api -> {
            Meter meter = api.getMeter("my-proxy-plugin");
            api.getTelemetryService().addListener(snapshot -> {
                if (snapshot instanceof VelocityTelemetrySnapshot velocitySnapshot) {
                    long players = velocitySnapshot.playersOnline();
                    long backends = velocitySnapshot.registeredServers();
                }
            });
            api.getTelemetryService().addSampler((snapshot, collector) -> {
                if (snapshot instanceof VelocityTelemetrySnapshot velocitySnapshot) {
                    collector.recordLongGauge("myproxy.players", velocitySnapshot.playersOnline());
                }
            });
            api.getTelemetryService().addSnapshotSampler(builder -> {
                if (builder instanceof VelocityTelemetrySnapshotBuilder velocityBuilder) {
                    velocityBuilder.setRegisteredServers(5);
                }
            });
        });
    }
}
```

## Third-Party Plugin Example (BlueMap)
This is a minimal example that records the number of maps exposed by BlueMap. Adjust the API
calls to match your BlueMap version.

```java
import de.bluecolored.bluemap.api.BlueMapAPI;
import dev.themeinerlp.minecraftotel.api.core.MinecraftOtelApi;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import io.opentelemetry.api.common.Attributes;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlueMapOtelBridge extends JavaPlugin {
    @Override
    public void onEnable() {
        MinecraftOtelApi api = getServer().getServicesManager().load(MinecraftOtelApi.class);
        if (api == null) {
            return;
        }

        BlueMapAPI.onEnable(blueMap -> {
            api.getTelemetryService().addSampler((snapshot, collector) -> {
                long maps = blueMap.getMaps().size();
                collector.recordLongGauge(
                    "bluemap.maps",
                    maps,
                    StandardMetrics.UNIT_COUNT,
                    Attributes.empty()
                );
            });
        });
    }
}
```
