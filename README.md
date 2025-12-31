# MinecraftOTEL

OpenTelemetry-first metrics for Paper servers and Velocity proxies.

Download `MinecraftOTEL.jar` from GitHub Releases (no version in the filename), drop it into your
`plugins/` folder, and attach the OpenTelemetry Java Agent. That's it.

Jump to: [Installation](#installation) | [Admins](#for-server-admins) | [Developers](#for-developers)

## Installation
1. Download `MinecraftOTEL.jar` from GitHub Releases.
2. Copy it into `plugins/` (Paper or Velocity).
3. Attach the OpenTelemetry Java Agent to your JVM.
4. Start the server/proxy and verify metrics in your exporter.

Full guide and agent examples: [docs/admin/installation.md](docs/admin/installation.md)

## Feature Matrix (Monitoring Plugins)
High-level overview only. Verify details in each project's docs. `*` = partial or varies by platform/fork.

| Feature | MinecraftOTEL | Unified Metrics | Spark | Prometheus Exporter |
| --- | --- | --- | --- | --- |
| OpenTelemetry native (OTLP via Java Agent) | ✅ | ❌ | ❌ | ❌ |
| Prometheus scrape endpoint | ❌ | * | ❌ | ✅ |
| Paper support | ✅ | * | * | * |
| Velocity support | ✅ | * | * | ❌ |
| Custom metrics API | ✅ | * | ❌ | ❌ |
| Profiling | * | ❌ | ✅ | ❌ |
| Dashboards included | * | ❌ | ❌ | ❌ |
| Built-in UI | ❌ | ❌ | ✅ | ❌ |
| CloudNet service labels | ✅ | ❌ | ❌ | ❌ |

Docs:
- MinecraftOTEL: [Admins](#for-server-admins), [Devs](#for-developers)
- Unified Metrics: https://github.com/plan-player-analytics/UnifiedMetrics
- Spark: https://spark.lucko.me/
- Prometheus Exporter: https://github.com/slok/minecraft-prometheus-exporter

If you use different projects with the same names, swap the doc links accordingly.

## Dashboard Preview
![Dashboard - Fleet Overview](docs/images/MinecraftOTEL%20-%20Fleet%20Overview-1767198873812.png)
![Dashboard - Server Detail](docs/images/MinecraftOTEL%20-%20Server%20Detail%20%28Overview%20%2B%20Drilldown%29-1767200016006.png)
![Dashboard - JVM](docs/images/MinecraftOTEL%20-%20JVM-1767200033793.png)

## For Server Admins
- Installation and Java Agent setup: [docs/admin/installation.md](docs/admin/installation.md)
- Configuration and dashboard notes: [docs/admin/configuration.md](docs/admin/configuration.md)
- Metrics reference: [docs/admin/metrics.md](docs/admin/metrics.md)
- CloudNet service monitoring: [docs/admin/cloudnet.md](docs/admin/cloudnet.md)

## For Developers
- API usage and third-party integrations: [docs/development/integrations.md](docs/development/integrations.md)
- Platform implementation guide: [docs/development/platform-extension.md](docs/development/platform-extension.md)
- Branching model: [docs/development/branching-model.md](docs/development/branching-model.md)
- Building from source: [docs/development/building.md](docs/development/building.md)

## Releases
GitHub releases include the built `MinecraftOTEL.jar` asset.
