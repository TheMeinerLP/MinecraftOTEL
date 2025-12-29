package dev.themeinerlp.minecraftotel.paper.listeners;

import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import io.opentelemetry.api.common.Attributes;
import dev.themeinerlp.minecraftotel.paper.state.TelemetryState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Records chunk load/unload events to update gauges and counters.
 */
public final class ChunkCounterListener implements Listener {
    private final TelemetryState state;
    private final TelemetryCollector collector;

    /**
     * Creates a chunk counter listener.
     *
     * @param state telemetry state
     * @param collector telemetry collector
     */
    public ChunkCounterListener(TelemetryState state, TelemetryCollector collector) {
        this.state = state;
        this.collector = collector;
    }

    /**
     * Handles chunk load events.
     *
     * @param event chunk load event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        String worldName = event.getWorld().getName();
        state.incrementChunk(worldName);
        collector.recordLongCounter(
                StandardMetrics.CHUNKS_LOAD_TOTAL,
                1L,
                StandardMetrics.UNIT_COUNT,
                Attributes.of(StandardMetrics.WORLD_KEY, worldName)
        );
        if (event.isNewChunk()) {
            collector.recordLongCounter(
                    StandardMetrics.CHUNKS_GENERATED_TOTAL,
                    1L,
                    StandardMetrics.UNIT_COUNT,
                    Attributes.of(StandardMetrics.WORLD_KEY, worldName)
            );
        }
    }

    /**
     * Handles chunk unload events.
     *
     * @param event chunk unload event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        String worldName = event.getWorld().getName();
        state.decrementChunk(worldName);
        collector.recordLongCounter(
                StandardMetrics.CHUNKS_UNLOAD_TOTAL,
                1L,
                StandardMetrics.UNIT_COUNT,
                Attributes.of(StandardMetrics.WORLD_KEY, worldName)
        );
    }

    /**
     * Tracks per-player chunk visibility on chunk send.
     *
     * @param event player chunk load event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChunkLoad(PlayerChunkLoadEvent event) {
        var chunk = event.getChunk();
        state.recordPlayerChunkLoad(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    /**
     * Tracks per-player chunk visibility on chunk unload.
     *
     * @param event player chunk unload event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChunkUnload(PlayerChunkUnloadEvent event) {
        var chunk = event.getChunk();
        state.recordPlayerChunkUnload(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }
}
