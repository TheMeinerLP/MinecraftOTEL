# MinecraftOTEL (Paper + Velocity)

OpenTelemetry metrics for Paper servers and Velocity proxies. This plugin only records server-side
metrics and relies on the OpenTelemetry Java Agent for exporting.

## Features

### Paper
- Players online (gauge)
- Entities loaded per world (gauge) + add/remove counters
- Chunks loaded per world (gauge) + load/unload counters
- Tick duration histogram (ms)
- TPS and MSPT (avg + p95)
- Optional Spark-backed sampling (preferSpark)

### Velocity
- Players online (gauge)
- Players online per backend server (gauge)
- Registered backend servers (gauge)

## Requirements

### Paper
- Java 21
- Paper server (Paper API)
- OpenTelemetry Java Agent attached to the JVM

### Velocity
- Java 21
- Velocity proxy
- OpenTelemetry Java Agent attached to the JVM

## Metrics

### Paper
- `minecraft.players.online` (gauge)
- `minecraft.entities.loaded` (gauge, attribute: `world`)
- `minecraft.chunks.loaded` (gauge, attribute: `world`)
- `minecraft.entities.added_total` (counter, attribute: `world`)
- `minecraft.entities.removed_total` (counter, attribute: `world`)
- `minecraft.chunks.load_total` (counter, attribute: `world`)
- `minecraft.chunks.unload_total` (counter, attribute: `world`)
- `minecraft.tick.duration` (histogram, ms)
- `minecraft.server.tps` (gauge, attribute: `window` = `1m|5m|15m`)
- `minecraft.server.mspt.avg` (gauge, ms)
- `minecraft.server.mspt.p95` (gauge, ms)

### Velocity
- `minecraft.players.online` (gauge)
- `minecraft.proxy.players.online` (gauge, attribute: `server`)
- `minecraft.proxy.servers.registered` (gauge)

No high-cardinality labels are used (no player UUID/name, no chunk coordinates).

## Configuration

### Paper
`config.yml` (Bukkit default config):

```yaml
otel:
  enable:
    tick: true
    entities: true
    chunks: true
    tpsMspt: true
  preferSpark: true
sampling:
  intervalSeconds: 1
  baselineScanIntervalSeconds: 10
```

### Velocity
`velocity.properties` (in the plugin data folder):

```properties
# sampling.intervalSeconds = 1-60
sampling.intervalSeconds=1
# per-backend player counts
otel.enable.playersPerServer=true
# total registered backend servers
otel.enable.serverCount=true
```

## Using the OpenTelemetry Java Agent
This plugin does not initialize any OpenTelemetry SDK or exporter. Attach the
Java Agent to your server startup and configure it normally.

Example (adjust exporter settings):
```
-javaagent:/path/to/opentelemetry-javaagent.jar
```

## API Usage (Paper + Velocity)
MinecraftOTEL exposes a small API so other plugins can create meters or react to
telemetry snapshots. The API is identical on Paper and Velocity.

Notes:
- Paper-only fields: `entitiesLoadedByWorld`, `chunksLoadedByWorld`, `tpsNullable`, `msptAvgNullable`, `msptP95Nullable`.
- Velocity-only fields: `playersByServer`, `registeredServers`.
- Custom sampling: register a sampler to emit gauges/counters/histograms via the shared collector.

### Paper example
```java
import dev.themeinerlp.minecraftotel.api.MinecraftOtelApi;
import dev.themeinerlp.minecraftotel.api.MinecraftOtelApiProvider;
import dev.themeinerlp.minecraftotel.api.StandardMetrics;
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
            long players = snapshot.playersOnline();
            // Use snapshot fields to enrich your own metrics or logs.
        });

        api.getTelemetryService().addSampler((snapshot, collector) -> {
            collector.recordLongGauge(
                "myplugin.players.last",
                snapshot.playersOnline(),
                StandardMetrics.UNIT_COUNT,
                io.opentelemetry.api.common.Attributes.empty()
            );
        });
    }
}
```

### Velocity example
```java
import dev.themeinerlp.minecraftotel.api.MinecraftOtelApiProvider;
import io.opentelemetry.api.metrics.Meter;

public final class MyPlugin {
    public void onProxyInitialize() {
        MinecraftOtelApiProvider.get().ifPresent(api -> {
            Meter meter = api.getMeter("my-proxy-plugin");
            api.getTelemetryService().addListener(snapshot -> {
                long players = snapshot.playersOnline();
                long backends = snapshot.registeredServers();
            });
            api.getTelemetryService().addSampler((snapshot, collector) -> {
                collector.recordLongGauge("myproxy.players", snapshot.playersOnline());
            });
        });
    }
}
```

## Build
```
./gradlew build
```

## Notes
- `plugin.yml` is generated by the build; do not edit it manually.
- Spark sampling is used only when Spark is installed and `preferSpark` is true.
- `velocity-plugin.json` is generated with the build version.
