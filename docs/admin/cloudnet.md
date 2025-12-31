# CloudNet Service Monitoring

MinecraftOTEL includes a lightweight CloudNet integration that enriches OpenTelemetry resources
with service metadata. It is intended for per-service monitoring and does not provide CloudNet
node or wrapper metrics.

## Requirements
- CloudNet v4 wrapper runtime
- `.wrapper/wrapper.json` present and readable in the service directory
- OpenTelemetry Java Agent attached to the JVM

## What It Does
When `.wrapper/wrapper.json` is available, MinecraftOTEL reads the `serviceConfiguration.serviceId`
block and sets these resource attributes:
- `service.name`
- `service.instance.id`
- `cloudnet.task.name`
- `cloudnet.service.name`

To apply these labels at the Java agent level, load `MinecraftOTEL.jar` as an agent extension.
See `docs/admin/installation.md`.

## Prometheus Labels
If you use the OpenTelemetry Collector Prometheus exporter, these resource attributes are typically
exposed as labels such as:
- `exported_job` (from `service.name`)
- `exported_instance` (from `service.instance.id`)

The Grafana dashboard in this repository uses `exported_job` and `exported_instance` for
selection and row grouping.

## Troubleshooting
- If no CloudNet labels appear, verify that `.wrapper/wrapper.json` exists in the service
  directory and is readable by the JVM user.
- This integration is inactive when the wrapper file is missing or invalid.
