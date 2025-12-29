package dev.themeinerlp.minecraftotel.paper.tick;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.opentelemetry.api.metrics.DoubleHistogram;
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
    private final DoubleHistogram tickDurationHistogram;
    private long tickStartNanos;

    /**
     * Creates a tick duration recorder.
     *
     * @param plugin plugin instance
     * @param tickDurationHistogram histogram to record tick durations
     */
    public TickDurationRecorder(JavaPlugin plugin, DoubleHistogram tickDurationHistogram) {
        this.plugin = plugin;
        this.tickDurationHistogram = tickDurationHistogram;
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
     * Captures the tick start time in nanoseconds.
     *
     * @param event tick start event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTickStart(ServerTickStartEvent event) {
        tickStartNanos = System.nanoTime();
    }

    /**
     * Records the elapsed tick time to the histogram.
     *
     * @param event tick end event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTickEnd(ServerTickEndEvent event) {
        if (tickStartNanos == 0L) {
            return;
        }
        long durationNanos = System.nanoTime() - tickStartNanos;
        tickDurationHistogram.record(durationNanos / 1_000_000d);
        tickStartNanos = 0L;
    }
}
