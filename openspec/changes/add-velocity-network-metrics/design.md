## Context

Velocity sampling runs on a fixed interval and writes through `TelemetryCollector`. Connection
facts are events and need counters. `server` values are CloudNet service names, so their
cardinality is bounded by the running services.

## Goals / Non-Goals

**Goals:** answer who could not connect, why, and which backend is at fault, at low overhead.

**Non-Goals:** per-player attributes, Paper metrics, alert rules, traces.

## Decisions

- Metric names follow the existing style: counters end in `_total`, and no attribute holds a
  player name, UUID or IP address.
- The listeners subscribe with `PostOrder.LAST`, so a login denied by another plugin, such as a
  ban system, counts as denied.
- Session start is kept in a concurrent map keyed by UUID and removed on disconnect, so the map
  cannot grow beyond the online players. Time comes from an injected `java.time.Clock`.
- Player latency is recorded per player per sample as a histogram. Negative values mean unknown
  and are skipped.
- The backend probe has its own task, default every 30 seconds with a 5 second timeout, because
  pings cause network traffic and log lines on backends. Results are published with
  `replaceLongGauge` after all pings of a round finished, so removed backends disappear. An
  unreachable backend reports `up` 0 and has no latency series.
- Client versions use the most recent version name of the player's protocol, for example `1.21.4`.
- Each costly source can be disabled. Event counters are cheap and always on.

## Risks / Trade-offs

- Status pings show up in backend logs. Mitigated by the slow interval and the setting.
- Sampling latency once per interval misses short spikes. Acceptable for a fleet view.
