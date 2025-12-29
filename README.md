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
- `minecraft.players.online` (gauge) - online players.
- `minecraft.entities.loaded` (gauge, `world`) - loaded entities per world.
- `minecraft.entities.loaded_by_type` (gauge, `entity_type`) - loaded entities by type (all worlds).
- `minecraft.entities.loaded_by_type_chunk` (gauge, `world`, `chunk_x`, `chunk_z`, `entity_type`) - optional, per-chunk entity types.
  - `light`: `entity_type` is `hostile|passive`.
  - `heavy`: `entity_type` is the exact entity type key.
- `minecraft.entities.per_chunk` (gauge) - avg entities per loaded chunk (all worlds).
- `minecraft.entities.added_total` (counter, `world`) - entities added to world.
- `minecraft.entities.removed_total` (counter, `world`) - entities removed from world.
- `minecraft.chunks.loaded` (gauge, `world`) - loaded chunks per world.
- `minecraft.chunks.loaded_per_player` (gauge) - ratio of chunks visible to exactly one player vs total loaded chunks.
- `minecraft.chunks.load_total` (counter, `world`) - chunk loads.
- `minecraft.chunks.unload_total` (counter, `world`) - chunk unloads.
- `minecraft.chunks.generated_total` (counter, `world`) - newly generated chunks.
- `minecraft.tick.duration` (histogram, ms) - per-tick duration.
- `minecraft.server.tps` (gauge, `window` = `1m|5m|15m`) - TPS per window.
- `minecraft.server.mspt.avg` (gauge, ms) - avg MSPT.
- `minecraft.server.mspt.p95` (gauge, ms) - p95 MSPT.

### Velocity
- `minecraft.players.online` (gauge)
- `minecraft.proxy.players.online` (gauge, attribute: `server`)
- `minecraft.proxy.servers.registered` (gauge)

Prometheus names replace dots with underscores (example: `minecraft.players.online` -> `minecraft_players_online`).

Chunk-level entity metrics are high-cardinality and disabled by default.

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
  entitiesByChunk:
    mode: off # off|light|heavy
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
- The prebuilt dashboard is in `dashboards/grafana/minecraftotel.json` and is auto-provisioned by Docker Compose.

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
