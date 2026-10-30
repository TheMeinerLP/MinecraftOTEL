package dev.themeinerlp.minecraftotel.api.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class MeterTelemetryCollectorTest {
    private static final AttributeKey<String> SERVER = AttributeKey.stringKey("server");
    private static final Attributes LOBBY = Attributes.of(SERVER, "lobby");
    private static final Attributes SURVIVAL = Attributes.of(SERVER, "survival");

    @Test
    void replaceLongGaugeDropsSeriesAbsentFromNextCall() {
        InMemoryMetricReader reader = InMemoryMetricReader.create();
        MeterTelemetryCollector collector = new MeterTelemetryCollector(meterFor(reader));

        collector.replaceLongGauge(StandardMetrics.PROXY_PLAYERS_ONLINE,
                Map.of(LOBBY, 3L, SURVIVAL, 5L), StandardMetrics.UNIT_COUNT);
        collector.replaceLongGauge(StandardMetrics.PROXY_PLAYERS_ONLINE,
                Map.of(LOBBY, 4L), StandardMetrics.UNIT_COUNT);

        assertEquals(
                Map.of(LOBBY, 4L),
                exportedPoints(reader, StandardMetrics.PROXY_PLAYERS_ONLINE),
                "series of a backend absent from the latest replace must no longer be exported"
        );
    }

    @Test
    void replaceLongGaugeWithEmptyMapExportsNoPoints() {
        InMemoryMetricReader reader = InMemoryMetricReader.create();
        MeterTelemetryCollector collector = new MeterTelemetryCollector(meterFor(reader));

        collector.replaceLongGauge(StandardMetrics.PROXY_PLAYERS_ONLINE,
                Map.of(LOBBY, 3L), StandardMetrics.UNIT_COUNT);
        collector.replaceLongGauge(StandardMetrics.PROXY_PLAYERS_ONLINE,
                Map.of(), StandardMetrics.UNIT_COUNT);

        assertTrue(
                exportedPoints(reader, StandardMetrics.PROXY_PLAYERS_ONLINE).isEmpty(),
                "an empty replace must leave no exported points"
        );
    }

    private static Meter meterFor(InMemoryMetricReader reader) {
        SdkMeterProvider provider = SdkMeterProvider.builder()
                .registerMetricReader(reader)
                .build();
        return provider.get("test");
    }

    private static Map<Attributes, Long> exportedPoints(InMemoryMetricReader reader, String name) {
        Optional<MetricData> metric = reader.collectAllMetrics().stream()
                .filter(data -> data.getName().equals(name))
                .findFirst();
        if (metric.isEmpty()) {
            return Map.of();
        }
        List<PointData> points = List.copyOf(metric.get().getLongGaugeData().getPoints());
        return points.stream().collect(Collectors.toMap(
                PointData::getAttributes,
                point -> ((LongPointData) point).getValue()
        ));
    }
}
