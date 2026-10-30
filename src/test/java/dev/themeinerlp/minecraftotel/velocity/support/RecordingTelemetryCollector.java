package dev.themeinerlp.minecraftotel.velocity.support;

import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import io.opentelemetry.api.common.Attributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collector that keeps every recorded value in memory for assertions.
 */
public final class RecordingTelemetryCollector implements TelemetryCollector {
    /**
     * One recorded instrument call.
     *
     * @param kind instrument kind, such as counter or histogram
     * @param name metric name
     * @param value recorded value
     * @param unit unit passed by the caller
     * @param attributes attributes passed by the caller
     */
    public record Sample(String kind, String name, double value, String unit, Attributes attributes) {
    }

    private final List<Sample> samples = new ArrayList<>();
    private final Map<String, Map<Attributes, Long>> replaced = new HashMap<>();

    /**
     * Returns every single-value sample recorded so far.
     *
     * @return samples in recording order
     */
    public List<Sample> all() {
        return List.copyOf(samples);
    }

    /**
     * Returns every single-value sample recorded under the name.
     *
     * @param name metric name
     * @return samples in recording order
     */
    public List<Sample> samples(String name) {
        return samples.stream().filter(sample -> sample.name().equals(name)).toList();
    }

    /**
     * Sums counter deltas recorded under the name and attributes.
     *
     * @param name metric name
     * @param attributes exact attributes to match
     * @return summed delta
     */
    public long counterTotal(String name, Attributes attributes) {
        return samples(name).stream()
                .filter(sample -> sample.kind().equals("counter") && sample.attributes().equals(attributes))
                .mapToLong(sample -> (long) sample.value())
                .sum();
    }

    /**
     * Returns the values of the last {@code replaceLongGauge} call for the name.
     *
     * @param name metric name
     * @return replaced series, or null when never replaced
     */
    public Map<Attributes, Long> replaced(String name) {
        return replaced.get(name);
    }

    @Override
    public void recordLongGauge(String name, long value, String unit, Attributes attributes) {
        samples.add(new Sample("long-gauge", name, value, unit, attributes));
    }

    @Override
    public void recordDoubleGauge(String name, double value, String unit, Attributes attributes) {
        samples.add(new Sample("double-gauge", name, value, unit, attributes));
    }

    @Override
    public void recordLongCounter(String name, long delta, String unit, Attributes attributes) {
        samples.add(new Sample("counter", name, delta, unit, attributes));
    }

    @Override
    public void recordDoubleHistogram(String name, double value, String unit, Attributes attributes) {
        samples.add(new Sample("histogram", name, value, unit, attributes));
    }

    @Override
    public void replaceLongGauge(String name, Map<Attributes, Long> values, String unit) {
        replaced.put(name, Map.copyOf(values));
    }
}
