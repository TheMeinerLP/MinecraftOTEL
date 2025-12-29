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

## Local Observability Stack (Docker Compose)
This repository ships a local stack with OpenTelemetry Collector, Tempo, Loki, Grafana, and
Prometheus so you can validate OpenTelemetry output quickly.

Start the stack:
```
docker compose up -d
```

Download or update the Java Agent:
```
./scripts/update-java-agent.sh
# or pin a version:
./scripts/update-java-agent.sh v2.12.0
```

Example JVM flags (metrics + traces via OTLP to the collector):
```
-javaagent:./opentelemetry-javaagent.jar \
-Dotel.metrics.exporter=otlp \
-Dotel.traces.exporter=otlp \
-Dotel.exporter.otlp.endpoint=http://localhost:4317 \
-Dotel.exporter.otlp.protocol=grpc
```

Open Grafana at http://localhost:3000 (anonymous access is enabled).

Notes:
- The OpenTelemetry Collector listens on `4317/4318` for OTLP and forwards traces to Tempo.
- Prometheus scrapes metrics from the collector on `:8889`.
- Logs are forwarded to Loki via the collector (`/loki/api/v1/push`).

## API Usage (Paper + Velocity)
MinecraftOTEL exposes a small API so other plugins can create meters or react to
telemetry snapshots. The API is identical on Paper and Velocity.

Notes:
- `TelemetrySnapshot` is a marker interface; cast to the platform snapshot type to access fields.
- Paper snapshot fields (`PaperTelemetrySnapshot`): `entitiesLoadedByWorld`, `chunksLoadedByWorld`, `tpsNullable`, `msptAvgNullable`, `msptP95Nullable`.
- Velocity snapshot fields (`VelocityTelemetrySnapshot`): `playersByServer`, `registeredServers`.
- Custom sampling: register a sampler to emit gauges/counters/histograms via the shared collector.

### Paper example
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

### Velocity example
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

## Build
```
./gradlew build
```

## Notes
- `plugin.yml` is generated by the build; do not edit it manually.
- Spark sampling is used only when Spark is installed and `preferSpark` is true.
- `velocity-plugin.json` is generated with the build version.
