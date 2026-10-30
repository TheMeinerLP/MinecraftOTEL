package dev.themeinerlp.minecraftotel.api.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TelemetryCollectorTest {
    @Test
    void defaultReplaceLongGaugeRecordsEveryEntry() {
        RecordingCollector collector = new RecordingCollector();
        Attributes lobby = Attributes.of(AttributeKey.stringKey("server"), "lobby");
        Attributes survival = Attributes.of(AttributeKey.stringKey("server"), "survival");

        collector.replaceLongGauge("test.gauge", Map.of(lobby, 3L, survival, 5L), "1");

        assertEquals(2, collector.longGauges.size(), "every entry must be recorded as a long gauge");
        assertEquals(
                Map.of(lobby, 3L, survival, 5L),
                collector.recordedLongGauges(),
                "default replaceLongGauge must record each attribute set with its value"
        );
        assertEquals("test.gauge", collector.longGauges.get(0).name(), "recorded name must match the replaced gauge");
        assertEquals("1", collector.longGauges.get(0).unit(), "recorded unit must match the replaced gauge");
    }

    private record LongGaugeCall(String name, long value, String unit, Attributes attributes) {
    }

    private static final class RecordingCollector implements TelemetryCollector {
        private final List<LongGaugeCall> longGauges = new ArrayList<>();

        Map<Attributes, Long> recordedLongGauges() {
            Map<Attributes, Long> values = new HashMap<>();
            for (LongGaugeCall call : longGauges) {
                values.put(call.attributes(), call.value());
            }
            return values;
        }

        @Override
        public void recordLongGauge(String name, long value, String unit, Attributes attributes) {
            longGauges.add(new LongGaugeCall(name, value, unit, attributes));
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
