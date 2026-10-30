package dev.themeinerlp.minecraftotel.velocity.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshot;
import dev.themeinerlp.minecraftotel.velocity.snapshot.VelocityTelemetrySnapshotBuilder;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class VelocityStandardSnapshotTelemetrySamplerTest {
    private static final AttributeKey<String> SERVER = AttributeKey.stringKey("server");
    private static final AttributeKey<String> VERSION = AttributeKey.stringKey("version");

    @Test
    void replacedPerServerValuesOmitBackendThatWasUnregistered() {
        VelocityStandardSnapshotTelemetrySampler sampler = new VelocityStandardSnapshotTelemetrySampler(true);
        RecordingCollector collector = new RecordingCollector();

        sampler.sample(snapshot(Map.of("lobby", 3L, "survival", 5L), 2L), collector);
        sampler.sample(snapshot(Map.of("survival", 5L), 1L), collector);

        assertEquals(
                Map.of(Attributes.of(SERVER, "survival"), 5L),
                collector.lastReplaced(StandardMetrics.PROXY_PLAYERS_ONLINE),
                "per-server values of the latest sample must not include the unregistered backend"
        );
    }

    @Test
    void recordsServerCountOfZeroWhenEnabled() {
        VelocityStandardSnapshotTelemetrySampler sampler = new VelocityStandardSnapshotTelemetrySampler(true);
        RecordingCollector collector = new RecordingCollector();

        sampler.sample(snapshot(Map.of(), 0L), collector);

        assertEquals(
                List.of(0L),
                collector.longGaugeValues(StandardMetrics.PROXY_SERVERS_REGISTERED),
                "a registered server count of 0 must be recorded while the count is enabled"
        );
    }

    @Test
    void doesNotRecordServerCountWhenDisabled() {
        VelocityStandardSnapshotTelemetrySampler sampler = new VelocityStandardSnapshotTelemetrySampler(false);
        RecordingCollector collector = new RecordingCollector();

        sampler.sample(snapshot(Map.of("lobby", 3L), 1L), collector);

        assertFalse(
                collector.recordedLongGauge(StandardMetrics.PROXY_SERVERS_REGISTERED),
                "the registered server count must not be recorded while the count is disabled"
        );
    }

    @Test
    void versionSeriesOfVersionNoLongerInUseIsRemoved() {
        VelocityStandardSnapshotTelemetrySampler sampler = new VelocityStandardSnapshotTelemetrySampler(true);
        RecordingCollector collector = new RecordingCollector();

        sampler.sample(snapshot(Map.of(), 1L, Map.of("1.21.4", 2L, "1.21.5", 1L), List.of()), collector);
        sampler.sample(snapshot(Map.of(), 1L, Map.of("1.21.5", 1L), List.of()), collector);

        assertEquals(
                Map.of(Attributes.of(VERSION, "1.21.5"), 1L),
                collector.lastReplaced(StandardMetrics.PROXY_PLAYERS_BY_VERSION),
                "a version that no player uses any more must not stay exported"
        );
    }

    @Test
    void recordsEachPlayerLatencyAsMillisecondHistogramValue() {
        VelocityStandardSnapshotTelemetrySampler sampler = new VelocityStandardSnapshotTelemetrySampler(true);
        RecordingCollector collector = new RecordingCollector();

        sampler.sample(snapshot(Map.of(), 1L, Map.of(), List.of(42L, 7L)), collector);

        assertEquals(
                List.of(42.0, 7.0),
                collector.histogramValues(StandardMetrics.PROXY_PLAYER_LATENCY),
                "every known player latency must be recorded as one histogram value"
        );
        assertEquals(
                StandardMetrics.UNIT_MILLIS,
                collector.histogramUnit(StandardMetrics.PROXY_PLAYER_LATENCY),
                "player latency must be reported in milliseconds"
        );
    }

    private static VelocityTelemetrySnapshot snapshot(
            Map<String, Long> playersByServer,
            long registeredServers,
            Map<String, Long> playersByVersion,
            List<Long> playerLatencies
    ) {
        return new VelocityTelemetrySnapshotBuilder()
                .setPlayersOnline(playersByServer.values().stream().mapToLong(Long::longValue).sum())
                .setPlayersByServer(playersByServer)
                .setRegisteredServers(registeredServers)
                .setPlayersByVersion(playersByVersion)
                .setPlayerLatencies(playerLatencies)
                .build();
    }

    private static VelocityTelemetrySnapshot snapshot(Map<String, Long> playersByServer, long registeredServers) {
        return snapshot(playersByServer, registeredServers, Map.of(), List.of());
    }

    private static final class RecordingCollector implements TelemetryCollector {
        private final Map<String, Map<Attributes, Long>> replaced = new HashMap<>();
        private final List<String> longGaugeNames = new ArrayList<>();
        private final List<Long> longGaugeValues = new ArrayList<>();
        private final Map<String, List<Double>> histogramValues = new HashMap<>();
        private final Map<String, String> histogramUnits = new HashMap<>();

        Map<Attributes, Long> lastReplaced(String name) {
            return replaced.get(name);
        }

        List<Long> longGaugeValues(String name) {
            List<Long> values = new ArrayList<>();
            for (int i = 0; i < longGaugeNames.size(); i++) {
                if (longGaugeNames.get(i).equals(name)) {
                    values.add(longGaugeValues.get(i));
                }
            }
            return values;
        }

        List<Double> histogramValues(String name) {
            return histogramValues.getOrDefault(name, List.of());
        }

        String histogramUnit(String name) {
            return histogramUnits.get(name);
        }

        boolean recordedLongGauge(String name) {
            return longGaugeNames.contains(name);
        }

        @Override
        public void replaceLongGauge(String name, Map<Attributes, Long> values, String unit) {
            replaced.put(name, Map.copyOf(values));
        }

        @Override
        public void recordLongGauge(String name, long value, String unit, Attributes attributes) {
            longGaugeNames.add(name);
            longGaugeValues.add(value);
        }

        @Override
        public void recordDoubleGauge(String name, double value, String unit, Attributes attributes) {
        }

        @Override
        public void recordLongCounter(String name, long delta, String unit, Attributes attributes) {
        }

        @Override
        public void recordDoubleHistogram(String name, double value, String unit, Attributes attributes) {
            histogramValues.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
            histogramUnits.put(name, unit);
        }
    }
}
