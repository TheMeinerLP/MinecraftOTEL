# Metrics

Prometheus names replace dots with underscores (example: `minecraft.players.online` ->
`minecraft_players_online`).

Chunk-level entity metrics are high-cardinality and disabled by default.

## Paper
- `minecraft.players.online` (gauge) - online players.
- `minecraft.entities.loaded` (gauge, `world`) - loaded entities per world.
- `minecraft.entities.loaded_by_type` (gauge, `entity_type`) - loaded entities by type (all worlds).
- `minecraft.entities.loaded_by_type_chunk` (gauge, `world`, `chunk_x`, `chunk_z`, `entity_type`) - optional, per-chunk entity types.
  - `light`: `entity_type` is `hostile|passive`.
  - `heavy`: `entity_type` is the exact entity type key.
- `minecraft.entities.per_chunk` (gauge) - avg entities per loaded chunk (all worlds).
- `minecraft.entities.added_total` (counter, `world`) - entities added to world.
- `minecraft.entities.removed_total` (counter, `world`) - entities removed from world.
- `minecraft.chunks.loaded` (gauge, `world`) - loaded chunks per world.
- `minecraft.chunks.loaded_per_player` (gauge) - ratio of chunks visible to exactly one player vs total loaded chunks.
- `minecraft.chunks.load_total` (counter, `world`) - chunk loads.
- `minecraft.chunks.unload_total` (counter, `world`) - chunk unloads.
- `minecraft.chunks.generated_total` (counter, `world`) - newly generated chunks.
- `minecraft.tick.duration` (histogram, ms) - per-tick duration.
- `minecraft.server.tps` (gauge, `window` = `1m|5m|15m`) - TPS per window.
- `minecraft.server.mspt.avg` (gauge, ms) - avg MSPT.
- `minecraft.server.mspt.p95` (gauge, ms) - p95 MSPT.

## Velocity
- `minecraft.players.online` (gauge)
- `minecraft.proxy.players.online` (gauge, attribute: `server`)
- `minecraft.proxy.servers.registered` (gauge)
- `minecraft.proxy.logins_total` (counter, `result` = `allowed|denied`) - logins after all other plugins decided, so a ban denies a login here.
- `minecraft.proxy.disconnects_total` (counter, `reason` = lower-case login status, for example `successful_login`).
- `minecraft.proxy.server_connects_total` (counter, `server`, `result` = `success|failed`) - connects to a backend; `failed` when the backend refused the player while connecting.
- `minecraft.proxy.server_kicks_total` (counter, `server`) - players kicked from a backend they were connected to.
- `minecraft.proxy.pings_total` (counter) - server list pings.
- `minecraft.proxy.session.duration` (histogram, s) - time from login to disconnect.
- `minecraft.proxy.players.by_version` (gauge, `version`) - players per client version name, while `otel.enable.playersByVersion` is true.
- `minecraft.proxy.player.latency` (histogram, ms) - latency of each online player on each sample, while `otel.enable.playerLatency` is true. Unknown latency is skipped.
- `minecraft.proxy.server.up` (gauge, `server`) - 1 when the backend answered the status ping, 0 when not, while `otel.enable.backendHealth` is true.
- `minecraft.proxy.server.latency` (gauge, ms, `server`) - status ping latency. A backend that did not answer has no series.

`minecraft.proxy.players.online` only has series for backends that are registered at the latest
sample; a removed backend's series disappears. `minecraft.proxy.servers.registered` is reported on
every sample while `otel.enable.serverCount` is true, including a value of 0. The health and
version series of a removed backend or of a version no player uses any more disappear the same way.

No proxy metric carries a player name, UUID or IP address.

Prometheus adds unit suffixes to names, for example `minecraft_proxy_session_duration_seconds_bucket`
for the session histogram and `minecraft_proxy_server_latency_milliseconds` for backend latency.
