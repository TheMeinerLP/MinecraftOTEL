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

    private static VelocityTelemetrySnapshot snapshot(Map<String, Long> playersByServer, long registeredServers) {
        return new VelocityTelemetrySnapshotBuilder()
                .setPlayersOnline(playersByServer.values().stream().mapToLong(Long::longValue).sum())
                .setPlayersByServer(playersByServer)
                .setRegisteredServers(registeredServers)
                .build();
    }

    private static final class RecordingCollector implements TelemetryCollector {
        private final Map<String, Map<Attributes, Long>> replaced = new HashMap<>();
        private final List<String> longGaugeNames = new ArrayList<>();
        private final List<Long> longGaugeValues = new ArrayList<>();

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
        }
    }
}
