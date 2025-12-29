package dev.themeinerlp.minecraftotel.api.metrics;

import io.opentelemetry.api.common.AttributeKey;

/**
 * Standard metric names, units, and attribute keys used by MinecraftOTEL.
 *
 * @since 1.1.0
 * @version 1.1.0
 */
public final class StandardMetrics {
    public static final String UNIT_COUNT = "1";
    public static final String UNIT_MILLIS = "ms";

    public static final String PLAYERS_ONLINE = "minecraft.players.online";
    public static final String ENTITIES_LOADED = "minecraft.entities.loaded";
    public static final String ENTITIES_LOADED_BY_TYPE = "minecraft.entities.loaded_by_type";
    public static final String ENTITIES_LOADED_BY_TYPE_CHUNK = "minecraft.entities.loaded_by_type_chunk";
    public static final String ENTITIES_PER_CHUNK = "minecraft.entities.per_chunk";
    public static final String CHUNKS_LOADED = "minecraft.chunks.loaded";
    public static final String CHUNKS_LOADED_PER_PLAYER = "minecraft.chunks.loaded_per_player";
    public static final String ENTITIES_ADDED_TOTAL = "minecraft.entities.added_total";
    public static final String ENTITIES_REMOVED_TOTAL = "minecraft.entities.removed_total";
    public static final String CHUNKS_LOAD_TOTAL = "minecraft.chunks.load_total";
    public static final String CHUNKS_UNLOAD_TOTAL = "minecraft.chunks.unload_total";
    public static final String CHUNKS_GENERATED_TOTAL = "minecraft.chunks.generated_total";
    public static final String TICK_DURATION = "minecraft.tick.duration";
    public static final String SERVER_TPS = "minecraft.server.tps";
    public static final String SERVER_MSPT_AVG = "minecraft.server.mspt.avg";
    public static final String SERVER_MSPT_P95 = "minecraft.server.mspt.p95";
    public static final String PROXY_PLAYERS_ONLINE = "minecraft.proxy.players.online";
    public static final String PROXY_SERVERS_REGISTERED = "minecraft.proxy.servers.registered";

    public static final AttributeKey<String> WORLD_KEY = AttributeKey.stringKey("world");
    public static final AttributeKey<String> WINDOW_KEY = AttributeKey.stringKey("window");
    public static final AttributeKey<String> SERVER_KEY = AttributeKey.stringKey("server");
    public static final AttributeKey<String> ENTITY_TYPE_KEY = AttributeKey.stringKey("entity_type");
    public static final AttributeKey<Long> CHUNK_X_KEY = AttributeKey.longKey("chunk_x");
    public static final AttributeKey<Long> CHUNK_Z_KEY = AttributeKey.longKey("chunk_z");

    public static final String[] TPS_WINDOWS = {"1m", "5m", "15m"};

    private StandardMetrics() {
    }
}
