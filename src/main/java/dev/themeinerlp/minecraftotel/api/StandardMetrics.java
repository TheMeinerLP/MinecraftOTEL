package dev.themeinerlp.minecraftotel.api;

import io.opentelemetry.api.common.AttributeKey;

/**
 * Standard metric names, units, and attribute keys used by MinecraftOTEL.
 */
public final class StandardMetrics {
    public static final String UNIT_COUNT = "1";
    public static final String UNIT_MILLIS = "ms";

    public static final String PLAYERS_ONLINE = "minecraft.players.online";
    public static final String ENTITIES_LOADED = "minecraft.entities.loaded";
    public static final String CHUNKS_LOADED = "minecraft.chunks.loaded";
    public static final String ENTITIES_ADDED_TOTAL = "minecraft.entities.added_total";
    public static final String ENTITIES_REMOVED_TOTAL = "minecraft.entities.removed_total";
    public static final String CHUNKS_LOAD_TOTAL = "minecraft.chunks.load_total";
    public static final String CHUNKS_UNLOAD_TOTAL = "minecraft.chunks.unload_total";
    public static final String TICK_DURATION = "minecraft.tick.duration";
    public static final String SERVER_TPS = "minecraft.server.tps";
    public static final String SERVER_MSPT_AVG = "minecraft.server.mspt.avg";
    public static final String SERVER_MSPT_P95 = "minecraft.server.mspt.p95";
    public static final String PROXY_PLAYERS_ONLINE = "minecraft.proxy.players.online";
    public static final String PROXY_SERVERS_REGISTERED = "minecraft.proxy.servers.registered";

    public static final AttributeKey<String> WORLD_KEY = AttributeKey.stringKey("world");
    public static final AttributeKey<String> WINDOW_KEY = AttributeKey.stringKey("window");
    public static final AttributeKey<String> SERVER_KEY = AttributeKey.stringKey("server");

    public static final String[] TPS_WINDOWS = {"1m", "5m", "15m"};

    private StandardMetrics() {
    }
}
