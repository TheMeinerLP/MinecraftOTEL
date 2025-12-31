# Configuration

## Paper
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

## Velocity
`velocity.properties` (in the plugin data folder):

```properties
# sampling.intervalSeconds = 1-60
sampling.intervalSeconds=1
# per-backend player counts
otel.enable.playersPerServer=true
# total registered backend servers
otel.enable.serverCount=true
```

## Dashboard
The current Grafana dashboard is CloudNet v4-specific and uses `exported_job` and
`exported_instance` for selection, repeating rows by both labels. A generic, non-CloudNet
dashboard is not available yet. The dashboard JSON is in
`dashboards/grafana/minecraftotel.json`.
