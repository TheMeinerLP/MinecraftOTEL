# Installation

This guide covers building the JAR, installing it on Paper or Velocity, and configuring the
OpenTelemetry Java Agent.

## Release Artifacts
GitHub releases include the built `MinecraftOTEL.jar` asset (no version in the filename).
You can also build locally if you prefer.

## Build From Source
If you prefer to build the JAR yourself, see `docs/development/building.md`.

## Paper Installation
1. Build the JAR (see above).
2. Copy the JAR into your Paper server `plugins/` directory.
3. Start the server to generate `config.yml`.
4. Configure the OpenTelemetry Java Agent (below).

## Velocity Installation
1. Build the JAR (see above).
2. Copy the JAR into your Velocity `plugins/` directory.
3. Start the proxy to generate `velocity.properties`.
4. Configure the OpenTelemetry Java Agent (below).

## OpenTelemetry Java Agent
MinecraftOTEL does not configure exporters. Attach the Java Agent to your JVM and configure it
as usual.

Example (OTLP exporter to a local collector):
```
-javaagent:/path/to/opentelemetry-javaagent.jar \
-Dotel.metrics.exporter=otlp \
-Dotel.traces.exporter=otlp \
-Dotel.exporter.otlp.endpoint=http://localhost:4317 \
-Dotel.exporter.otlp.protocol=grpc
```

### Load MinecraftOTEL as a Java Agent Extension
To load the MinecraftOTEL jar as an OpenTelemetry Java Agent extension (for example, to apply
CloudNet service labels at the agent level), pass the extension property:

```
-javaagent:/path/to/opentelemetry-javaagent.jar \
-Dotel.javaagent.extensions=/path/to/MinecraftOTEL.jar
```

You can also set `OTEL_JAVAAGENT_EXTENSIONS` to a jar or a directory containing extension jars.

OpenTelemetry references:
- Java agent configuration: https://opentelemetry.io/docs/zero-code/java/agent/configuration/
- Java agent extensions: https://opentelemetry.io/docs/zero-code/java/agent/extensions/

## Local Observability Stack
This repository ships a local stack with OpenTelemetry Collector, Tempo, Loki, Grafana, and
Prometheus.

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

Open Grafana at http://localhost:3000 (anonymous access is enabled).
The prebuilt dashboard is in `dashboards/grafana/minecraftotel.json`.
