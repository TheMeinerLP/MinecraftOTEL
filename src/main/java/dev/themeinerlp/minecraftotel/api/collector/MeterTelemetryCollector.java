package dev.themeinerlp.minecraftotel.api.collector;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TelemetryCollector backed by OpenTelemetry instruments.
 *
 * @since 1.1.0
 * @version 1.1.0
 */
public final class MeterTelemetryCollector implements TelemetryCollector {
    private final Meter meter;
    private final Map<String, LongGaugeStore> longGauges;
    private final Map<String, DoubleGaugeStore> doubleGauges;
    private final Map<String, LongCounter> longCounters;
    private final Map<String, DoubleHistogram> doubleHistograms;

    public MeterTelemetryCollector(Meter meter) {
        this.meter = meter;
        this.longGauges = new ConcurrentHashMap<>();
        this.doubleGauges = new ConcurrentHashMap<>();
        this.longCounters = new ConcurrentHashMap<>();
        this.doubleHistograms = new ConcurrentHashMap<>();
    }

    @Override
    public void recordLongGauge(String name, long value, String unit, Attributes attributes) {
        Attributes attrs = attributes == null ? Attributes.empty() : attributes;
        LongGaugeStore store = longGauges.computeIfAbsent(name, metricName ->
                new LongGaugeStore(meter, metricName, unit)
        );
        store.record(value, attrs);
    }

    @Override
    public void recordDoubleGauge(String name, double value, String unit, Attributes attributes) {
        Attributes attrs = attributes == null ? Attributes.empty() : attributes;
        DoubleGaugeStore store = doubleGauges.computeIfAbsent(name, metricName ->
                new DoubleGaugeStore(meter, metricName, unit)
        );
        store.record(value, attrs);
    }

    @Override
    public void recordLongCounter(String name, long delta, String unit, Attributes attributes) {
        Attributes attrs = attributes == null ? Attributes.empty() : attributes;
        LongCounter counter = longCounters.computeIfAbsent(name, metricName -> {
            LongCounterBuilder builder = meter.counterBuilder(metricName);
            if (unit != null && !unit.isBlank()) {
                builder.setUnit(unit);
            }
            return builder.build();
        });
        counter.add(delta, attrs);
    }

    @Override
    public void recordDoubleHistogram(String name, double value, String unit, Attributes attributes) {
        Attributes attrs = attributes == null ? Attributes.empty() : attributes;
        DoubleHistogram histogram = doubleHistograms.computeIfAbsent(name, metricName -> {
            DoubleHistogramBuilder builder = meter.histogramBuilder(metricName);
            if (unit != null && !unit.isBlank()) {
                builder.setUnit(unit);
            }
            return builder.build();
        });
        histogram.record(value, attrs);
    }

    private static final class LongGaugeStore {
        private final Map<Attributes, Long> values;

        private LongGaugeStore(Meter meter, String name, String unit) {
            this.values = new ConcurrentHashMap<>();
            var builder = meter.gaugeBuilder(name).ofLongs();
            if (unit != null && !unit.isBlank()) {
                builder.setUnit(unit);
            }
            builder.buildWithCallback(measurement -> {
                for (Map.Entry<Attributes, Long> entry : values.entrySet()) {
                    measurement.record(entry.getValue(), entry.getKey());
                }
            });
        }

        private void record(long value, Attributes attributes) {
            values.put(attributes, value);
        }
    }

    private static final class DoubleGaugeStore {
        private final Map<Attributes, Double> values;

        private DoubleGaugeStore(Meter meter, String name, String unit) {
            this.values = new ConcurrentHashMap<>();
            var builder = meter.gaugeBuilder(name);
            if (unit != null && !unit.isBlank()) {
                builder.setUnit(unit);
            }
            builder.buildWithCallback(measurement -> {
                for (Map.Entry<Attributes, Double> entry : values.entrySet()) {
                    measurement.record(entry.getValue(), entry.getKey());
                }
            });
        }

        private void record(double value, Attributes attributes) {
            values.put(attributes, value);
        }
    }
}
