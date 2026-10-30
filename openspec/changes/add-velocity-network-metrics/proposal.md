## Why

Proxy telemetry shows how many players are online, but not how the network behaves. Staff cannot
see failed logins, why players drop, which backend refuses connections, how long players stay,
which client versions are in use or how much ping traffic hits the proxy. Backend outages only
show up as log lines such as `unable to connect to server`.

## What Changes

- Count logins, disconnects, backend connects, backend kicks and server list pings.
- Record session duration and player latency as histograms.
- Report players by client version.
- Probe registered backends on a slow interval and report reachability and latency.
- New settings in `velocity.properties`, existing files keep working through defaults.
- Add a Grafana dashboard for proxies.

Ships as `feat(velocity)`.

## Capabilities

### New Capabilities

- `velocity-network-metrics`: connection, session, client version and backend health metrics.

### Modified Capabilities

None.

## Impact

- New Velocity event listener and backend probe task; no change to existing metric names.
- Backends receive one status ping per probe interval from each proxy.
- Paper is unchanged.
