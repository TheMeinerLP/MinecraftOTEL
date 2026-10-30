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
# players per client version (minecraft.proxy.players.by_version)
otel.enable.playersByVersion=true
# latency of every online player on each sample (minecraft.proxy.player.latency)
otel.enable.playerLatency=true
# status ping of every registered backend (minecraft.proxy.server.up and latency)
otel.enable.backendHealth=true
# seconds between backend probes, clamped to 5-300
backendHealth.intervalSeconds=30
```

Backend health sends one status ping per backend from each proxy every
`backendHealth.intervalSeconds`. The pings show up in the backend logs. Set
`otel.enable.backendHealth=false` to stop them. Existing files without the new keys use these
defaults.

## Dashboard
The current Grafana dashboard is CloudNet v4-specific and uses `exported_job` and
`exported_instance` for selection, repeating rows by both labels. A generic, non-CloudNet
dashboard is not available yet. The dashboard JSON is in
`dashboards/grafana/minecraftotel.json`.
