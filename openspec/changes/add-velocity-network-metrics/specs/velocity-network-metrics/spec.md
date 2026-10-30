## ADDED Requirements

### Requirement: Connection counters
The system SHALL count `minecraft.proxy.logins_total` with `result` = `allowed|denied`,
`minecraft.proxy.disconnects_total` with `reason` set to the lower-case login status,
`minecraft.proxy.server_connects_total` with `server` and `result` = `success|failed`,
`minecraft.proxy.server_kicks_total` with `server` and `minecraft.proxy.pings_total`.

#### Scenario: Another plugin denies a login
- **WHEN** a login is denied by any plugin
- **THEN** the login counts as `denied`

#### Scenario: Backend refuses a connection
- **WHEN** a player is kicked while connecting to a backend
- **THEN** `server_connects_total` counts `failed` for it and `server_kicks_total` is unchanged

#### Scenario: Player is kicked from a backend
- **WHEN** a connected player is kicked from a backend
- **THEN** `server_kicks_total` counts it for that backend

#### Scenario: No personal data
- **WHEN** any connection metric is recorded
- **THEN** no attribute contains a player name, UUID or IP address

### Requirement: Session duration
The system SHALL record `minecraft.proxy.session.duration` in seconds when a player who completed
login disconnects.

#### Scenario: Player leaves
- **WHEN** a player disconnects 90 seconds after login
- **THEN** a value of 90 is recorded and the tracked session is removed

#### Scenario: Login never completed
- **WHEN** a disconnect arrives for a player without a tracked session
- **THEN** no duration is recorded

### Requirement: Players by client version
While `otel.enable.playersByVersion` is true, the system SHALL report
`minecraft.proxy.players.by_version` with `version` for the versions currently in use.

#### Scenario: Last player of a version leaves
- **WHEN** no player uses a version any more
- **THEN** its series is no longer exported

### Requirement: Player latency
While `otel.enable.playerLatency` is true, the system SHALL record `minecraft.proxy.player.latency`
in milliseconds for every player with a known latency on each sample.

#### Scenario: Unknown latency
- **WHEN** a player reports a negative latency
- **THEN** no value is recorded for that player

### Requirement: Backend health
While `otel.enable.backendHealth` is true, the system SHALL probe every registered backend each
`backendHealth.intervalSeconds` (default 30, range 5 to 300) and report `minecraft.proxy.server.up`
and `minecraft.proxy.server.latency` in milliseconds, both with `server`.

#### Scenario: Backend answers
- **WHEN** a backend answers the ping
- **THEN** `up` is 1 and its latency is reported

#### Scenario: Backend is unreachable
- **WHEN** a ping fails or times out
- **THEN** `up` is 0 and no latency series exists for it

#### Scenario: Backend is unregistered
- **WHEN** a backend is no longer registered
- **THEN** none of its health series are exported

#### Scenario: Probe is disabled
- **WHEN** `otel.enable.backendHealth` is false
- **THEN** no probe task is scheduled
