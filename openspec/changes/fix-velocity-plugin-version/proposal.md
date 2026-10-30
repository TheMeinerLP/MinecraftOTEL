## Why

On Velocity the plugin resolves its version from the jar manifest, which has no
`Implementation-Version`. Metrics from proxies therefore carry the instrumentation version
`unknown`, so a dashboard cannot tell which release a proxy runs.

## What Changes

- Write `Implementation-Version` into the manifest of the shaded jar.

Ships as `fix(velocity)`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `velocity-telemetry`: the instrumentation version equals the released plugin version.

## Impact

Build configuration only. No metric names change.
