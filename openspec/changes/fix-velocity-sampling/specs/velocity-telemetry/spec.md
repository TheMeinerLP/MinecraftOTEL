## ADDED Requirements

### Requirement: Sampling task runs under the plugin
The Velocity telemetry service SHALL schedule its repeating sampling task with the Velocity plugin
instance as owner.

#### Scenario: Proxy starts
- **WHEN** the proxy fires the initialize event
- **THEN** the sampling task is scheduled for the plugin instance and startup completes without error

### Requirement: Per-server series follow registered backends
The system SHALL export `minecraft.proxy.players.online` only for backends registered at the latest sample.

#### Scenario: Backend is unregistered
- **WHEN** a backend present in one sample is absent from the next sample
- **THEN** the series of that backend is no longer exported

#### Scenario: Per-server metrics are disabled
- **WHEN** `otel.enable.playersPerServer` is false
- **THEN** no `minecraft.proxy.players.online` series is exported

### Requirement: Registered server count stays current
While `otel.enable.serverCount` is true, the system SHALL report `minecraft.proxy.servers.registered`
on every sample, including a count of 0.

#### Scenario: Last backend is removed
- **WHEN** the number of registered backends drops to 0
- **THEN** the exported value is 0

#### Scenario: Count is disabled
- **WHEN** `otel.enable.serverCount` is false
- **THEN** `minecraft.proxy.servers.registered` is not recorded
