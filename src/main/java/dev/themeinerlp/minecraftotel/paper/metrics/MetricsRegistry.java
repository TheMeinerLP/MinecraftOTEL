package dev.themeinerlp.minecraftotel.paper.metrics;

import dev.themeinerlp.minecraftotel.paper.state.TelemetrySnapshot;
import dev.themeinerlp.minecraftotel.paper.state.TelemetryState;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import java.util.Map;
import org.bukkit.plugin.java.JavaPlugin;

public final class MetricsRegistry {
    public static final AttributeKey<String> WORLD_KEY = AttributeKey.stringKey("world");
    public static final AttributeKey<String> WINDOW_KEY = AttributeKey.stringKey("window");
    private static final String[] TPS_WINDOWS = {"1m", "5m", "15m"};

    private final LongCounter entitiesAddedCounter;
    private final LongCounter entitiesRemovedCounter;
    private final LongCounter chunksLoadCounter;
    private final LongCounter chunksUnloadCounter;
    private final DoubleHistogram tickDurationHistogram;

    public MetricsRegistry(JavaPlugin plugin, TelemetryState state) {
        String version = plugin.getDescription().getVersion();
        Meter meter = GlobalOpenTelemetry.get().meterBuilder("minecraft-otel-bukkit")
                .setInstrumentationVersion(version).build();

        this.entitiesAddedCounter = meter
                .counterBuilder("minecraft.entities.added_total")
                .setUnit("1")
                .build();
        this.entitiesRemovedCounter = meter
                .counterBuilder("minecraft.entities.removed_total")
                .setUnit("1")
                .build();
        this.chunksLoadCounter = meter
                .counterBuilder("minecraft.chunks.load_total")
                .setUnit("1")
                .build();
        this.chunksUnloadCounter = meter
                .counterBuilder("minecraft.chunks.unload_total")
                .setUnit("1")
                .build();
        this.tickDurationHistogram = meter
                .histogramBuilder("minecraft.tick.duration")
                .setUnit("ms")
                .build();

        meter.gaugeBuilder("minecraft.players.online")
                .setUnit("1")
                .ofLongs()
                .buildWithCallback(measurement -> recordPlayersOnline(measurement, state));

        meter.gaugeBuilder("minecraft.entities.loaded")
                .setUnit("1")
                .ofLongs()
                .buildWithCallback(measurement -> recordEntitiesLoaded(measurement, state));

        meter.gaugeBuilder("minecraft.chunks.loaded")
                .setUnit("1")
                .ofLongs()
                .buildWithCallback(measurement -> recordChunksLoaded(measurement, state));

        meter.gaugeBuilder("minecraft.server.tps")
                .setUnit("1")
                .buildWithCallback(measurement -> recordTps(measurement, state));

        meter.gaugeBuilder("minecraft.server.mspt.avg")
                .setUnit("ms")
                .buildWithCallback(measurement -> recordMsptAvg(measurement, state));

        meter.gaugeBuilder("minecraft.server.mspt.p95")
                .setUnit("ms")
                .buildWithCallback(measurement -> recordMsptP95(measurement, state));
    }

    public LongCounter getEntitiesAddedCounter() {
        return entitiesAddedCounter;
    }

    public LongCounter getEntitiesRemovedCounter() {
        return entitiesRemovedCounter;
    }

    public LongCounter getChunksLoadCounter() {
        return chunksLoadCounter;
    }

    public LongCounter getChunksUnloadCounter() {
        return chunksUnloadCounter;
    }

    public DoubleHistogram getTickDurationHistogram() {
        return tickDurationHistogram;
    }

    public static Attributes worldAttributes(String worldName) {
        return Attributes.of(WORLD_KEY, worldName);
    }

    private static void recordPlayersOnline(
            ObservableLongMeasurement measurement,
            TelemetryState state
    ) {
        TelemetrySnapshot snapshot = state.getSnapshot();
        measurement.record(snapshot.playersOnline());
    }

    private static void recordEntitiesLoaded(
            ObservableLongMeasurement measurement,
            TelemetryState state
    ) {
        TelemetrySnapshot snapshot = state.getSnapshot();
        for (Map.Entry<String, Long> entry : snapshot.entitiesLoadedByWorld().entrySet()) {
            measurement.record(entry.getValue(), Attributes.of(WORLD_KEY, entry.getKey()));
        }
    }

    private static void recordChunksLoaded(
            ObservableLongMeasurement measurement,
            TelemetryState state
    ) {
        TelemetrySnapshot snapshot = state.getSnapshot();
        for (Map.Entry<String, Long> entry : snapshot.chunksLoadedByWorld().entrySet()) {
            measurement.record(entry.getValue(), Attributes.of(WORLD_KEY, entry.getKey()));
        }
    }

    private static void recordTps(ObservableDoubleMeasurement measurement, TelemetryState state) {
        TelemetrySnapshot snapshot = state.getSnapshot();
        double[] tps = snapshot.tpsNullable();
        if (tps == null) {
            return;
        }
        int limit = Math.min(tps.length, TPS_WINDOWS.length);
        for (int i = 0; i < limit; i++) {
            measurement.record(tps[i], Attributes.of(WINDOW_KEY, TPS_WINDOWS[i]));
        }
    }

    private static void recordMsptAvg(ObservableDoubleMeasurement measurement, TelemetryState state) {
        TelemetrySnapshot snapshot = state.getSnapshot();
        Double msptAvg = snapshot.msptAvgNullable();
        if (msptAvg != null) {
            measurement.record(msptAvg);
        }
    }

    private static void recordMsptP95(ObservableDoubleMeasurement measurement, TelemetryState state) {
        TelemetrySnapshot snapshot = state.getSnapshot();
        Double msptP95 = snapshot.msptP95Nullable();
        if (msptP95 != null) {
            measurement.record(msptP95);
        }
    }
}
