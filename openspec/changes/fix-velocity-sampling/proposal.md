## Why

Every Velocity proxy fails to start MinecraftOTEL sampling. At startup the proxy logs
`Couldn't pass ProxyInitializeEvent to minecraftotel 1.6.0` with
`IllegalArgumentException: plugin is not registered`, because the sampling task is scheduled
with the telemetry service as owner instead of the plugin instance. One sample is taken before
the failure, so the metrics exist but keep their startup values forever: on the live network all
proxy gauges read 0 with no change over seven days while players were online. A proxy without
backends at startup never reports its per-server metrics at all.

Fixing the scheduler exposes two more defects in the same code path that were hidden so far:
per-server series keep being exported after a backend is unregistered, and the registered-server
gauge is not updated when the last backend goes away.

## What Changes

- Schedule the sampling task with the Velocity plugin instance as owner.
- Export per-server player series only for currently registered backends.
- Report the registered-server count on every sample while enabled, including 0.
- Add a default method to `TelemetryCollector` that replaces all series of a long gauge.

Ships as `fix(velocity)`.

## Capabilities

### New Capabilities

- `velocity-telemetry`: sampling lifecycle and metric reporting on Velocity proxies.

### Modified Capabilities

None.

## Impact

- Velocity platform code and `MeterTelemetryCollector`; `TelemetryCollector` gains an additive
  default method, so existing implementations keep compiling.
- Dashboards start showing real proxy values. Series of removed backends disappear.
- Adds the first automated tests to the repository (JUnit, Mockito, OpenTelemetry SDK testing),
  test scope only.
- Paper behaviour is unchanged.
