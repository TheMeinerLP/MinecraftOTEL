package dev.themeinerlp.minecraftotel.paper.tick;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Records per-tick durations using Paper tick start/end events.
 */
public final class TickDurationRecorder implements Listener {
    private final JavaPlugin plugin;
    private final TelemetryCollector collector;

    /**
     * Creates a tick duration recorder.
     *
     * @param plugin plugin instance
     * @param collector telemetry collector
     */
    public TickDurationRecorder(JavaPlugin plugin, TelemetryCollector collector) {
        this.plugin = plugin;
        this.collector = collector;
    }

    /**
     * Registers the listener with the Bukkit event system.
     */
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Unregisters the listener from the Bukkit event system.
     */
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Records the elapsed tick time to the histogram.
     *
     * @param event tick end event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTickEnd(ServerTickEndEvent event) {
        double durationMillis = event.getTickDuration();
        collector.recordDoubleHistogram(
                StandardMetrics.TICK_DURATION,
                durationMillis,
                StandardMetrics.UNIT_MILLIS,
                null
        );
    }
}
