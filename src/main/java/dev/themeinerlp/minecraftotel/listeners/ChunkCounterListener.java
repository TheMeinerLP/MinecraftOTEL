package dev.themeinerlp.minecraftotel.listeners;

import dev.themeinerlp.minecraftotel.metrics.MetricsRegistry;
import dev.themeinerlp.minecraftotel.state.TelemetryState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public final class ChunkCounterListener implements Listener {
    private final TelemetryState state;
    private final MetricsRegistry metrics;

    public ChunkCounterListener(TelemetryState state, MetricsRegistry metrics) {
        this.state = state;
        this.metrics = metrics;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        String worldName = event.getWorld().getName();
        state.incrementChunk(worldName);
        metrics.getChunksLoadCounter().add(1L, MetricsRegistry.worldAttributes(worldName));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        String worldName = event.getWorld().getName();
        state.decrementChunk(worldName);
        metrics.getChunksUnloadCounter().add(1L, MetricsRegistry.worldAttributes(worldName));
    }
}
