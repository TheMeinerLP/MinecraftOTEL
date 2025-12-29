package dev.themeinerlp.minecraftotel.paper.listeners;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import io.opentelemetry.api.common.Attributes;
import dev.themeinerlp.minecraftotel.paper.state.TelemetryState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Records entity add/remove events to update gauges and counters.
 */
public final class EntityCounterListener implements Listener {
    private final TelemetryState state;
    private final TelemetryCollector collector;

    /**
     * Creates an entity counter listener.
     *
     * @param state telemetry state
     * @param collector telemetry collector
     */
    public EntityCounterListener(TelemetryState state, TelemetryCollector collector) {
        this.state = state;
        this.collector = collector;
    }

    /**
     * Handles entity add events.
     *
     * @param event entity add event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityAdd(EntityAddToWorldEvent event) {
        String worldName = event.getEntity().getWorld().getName();
        var typeKeyObj = event.getEntity().getType().getKey();
        String typeKey = typeKeyObj == null ? "" : typeKeyObj.toString();
        state.incrementEntity(worldName);
        state.incrementEntityType(typeKey);
        state.incrementEntityTypeInChunk(event.getEntity());
        collector.recordLongCounter(
                StandardMetrics.ENTITIES_ADDED_TOTAL,
                1L,
                StandardMetrics.UNIT_COUNT,
                Attributes.of(StandardMetrics.WORLD_KEY, worldName)
        );
    }

    /**
     * Handles entity remove events.
     *
     * @param event entity remove event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemove(EntityRemoveFromWorldEvent event) {
        String worldName = event.getEntity().getWorld().getName();
        var typeKeyObj = event.getEntity().getType().getKey();
        String typeKey = typeKeyObj == null ? "" : typeKeyObj.toString();
        state.decrementEntity(worldName);
        state.decrementEntityType(typeKey);
        state.decrementEntityTypeInChunk(event.getEntity());
        collector.recordLongCounter(
                StandardMetrics.ENTITIES_REMOVED_TOTAL,
                1L,
                StandardMetrics.UNIT_COUNT,
                Attributes.of(StandardMetrics.WORLD_KEY, worldName)
        );
    }
}
