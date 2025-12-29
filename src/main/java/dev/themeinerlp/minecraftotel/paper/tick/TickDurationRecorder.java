package dev.themeinerlp.minecraftotel.paper.tick;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.opentelemetry.api.metrics.DoubleHistogram;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class TickDurationRecorder implements Listener {
    private final JavaPlugin plugin;
    private final DoubleHistogram tickDurationHistogram;
    private long tickStartNanos;

    public TickDurationRecorder(JavaPlugin plugin, DoubleHistogram tickDurationHistogram) {
        this.plugin = plugin;
        this.tickDurationHistogram = tickDurationHistogram;
    }

    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTickStart(ServerTickStartEvent event) {
        tickStartNanos = System.nanoTime();
    }

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
