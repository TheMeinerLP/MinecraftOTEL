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
