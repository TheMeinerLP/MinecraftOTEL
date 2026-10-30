## Context

`VelocityTelemetryService.startSamplingTask` passes `this` to `Scheduler.buildTask`. Velocity
requires a registered plugin instance there. Gauges are stored per attribute set in
`MeterTelemetryCollector` and read by an observable callback; entries are never removed.

## Goals / Non-Goals

**Goals:** sampling runs on Velocity; exported proxy gauges match the current proxy state.

**Non-Goals:** Paper series cleanup, the `unknown` instrumentation version, Velocity API version
bumps, a generic gauge removal API for double gauges.

## Decisions

- The plugin instance is passed into the `VelocityTelemetryService` constructor as an `Object`
  owner, matching the `Scheduler.buildTask` signature.
- `TelemetryCollector.replaceLongGauge(name, values, unit)` is a default method that records each
  entry, so third-party collectors keep working. `MeterTelemetryCollector` overrides it and drops
  series missing from `values`. A single replace call was chosen over a per-attribute remove call
  because the sampler already holds the complete desired state.
- `VelocityStandardSnapshotTelemetrySampler` takes a `recordServerCount` flag, since a count of 0
  cannot be told apart from a disabled count in the snapshot. It is therefore created in `start()`,
  after the configuration is loaded, instead of in the constructor.

## Risks / Trade-offs

- Replacing is not atomic against a concurrent export; one export may miss or keep a series for a
  single interval, which is acceptable for gauges.
