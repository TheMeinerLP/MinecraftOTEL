package dev.themeinerlp.minecraftotel.paper.listeners;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import dev.themeinerlp.minecraftotel.paper.metrics.MetricsRegistry;
import dev.themeinerlp.minecraftotel.paper.state.TelemetryState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class EntityCounterListener implements Listener {
    private final TelemetryState state;
    private final MetricsRegistry metrics;

    public EntityCounterListener(TelemetryState state, MetricsRegistry metrics) {
        this.state = state;
        this.metrics = metrics;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityAdd(EntityAddToWorldEvent event) {
        String worldName = event.getEntity().getWorld().getName();
        state.incrementEntity(worldName);
        metrics.getEntitiesAddedCounter().add(1L, MetricsRegistry.worldAttributes(worldName));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemove(EntityRemoveFromWorldEvent event) {
        String worldName = event.getEntity().getWorld().getName();
        state.decrementEntity(worldName);
        metrics.getEntitiesRemovedCounter().add(1L, MetricsRegistry.worldAttributes(worldName));
    }
}
