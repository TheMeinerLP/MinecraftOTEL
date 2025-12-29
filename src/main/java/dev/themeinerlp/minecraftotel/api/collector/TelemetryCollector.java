package dev.themeinerlp.minecraftotel.api.collector;

import io.opentelemetry.api.common.Attributes;

/**
 * Collector for turning telemetry samples into OpenTelemetry instruments.
 *
 * @since 1.1.0
 * @version 1.1.0
 */
public interface TelemetryCollector {
    /**
     * Records a long gauge value.
     *
     * @param name metric name
     * @param value gauge value
     * @param unit unit string or null for default
     * @param attributes metric attributes
     */
    void recordLongGauge(String name, long value, String unit, Attributes attributes);

    /**
     * Records a double gauge value.
     *
     * @param name metric name
     * @param value gauge value
     * @param unit unit string or null for default
     * @param attributes metric attributes
     */
    void recordDoubleGauge(String name, double value, String unit, Attributes attributes);

    /**
     * Records a long counter delta.
     *
     * @param name metric name
     * @param delta increment amount
     * @param unit unit string or null for default
     * @param attributes metric attributes
     */
    void recordLongCounter(String name, long delta, String unit, Attributes attributes);

    /**
     * Records a double histogram value.
     *
     * @param name metric name
     * @param value histogram value
     * @param unit unit string or null for default
     * @param attributes metric attributes
     */
    void recordDoubleHistogram(String name, double value, String unit, Attributes attributes);

    /**
     * Records a long gauge value with default unit and no attributes.
     *
     * @param name metric name
     * @param value gauge value
     */
    default void recordLongGauge(String name, long value) {
        recordLongGauge(name, value, null, Attributes.empty());
    }

    /**
     * Records a double gauge value with default unit and no attributes.
     *
     * @param name metric name
     * @param value gauge value
     */
    default void recordDoubleGauge(String name, double value) {
        recordDoubleGauge(name, value, null, Attributes.empty());
    }

    /**
     * Records a long counter delta with default unit and no attributes.
     *
     * @param name metric name
     * @param delta increment amount
     */
    default void recordLongCounter(String name, long delta) {
        recordLongCounter(name, delta, null, Attributes.empty());
    }

    /**
     * Records a double histogram value with default unit and no attributes.
     *
     * @param name metric name
     * @param value histogram value
     */
    default void recordDoubleHistogram(String name, double value) {
        recordDoubleHistogram(name, value, null, Attributes.empty());
    }
}
