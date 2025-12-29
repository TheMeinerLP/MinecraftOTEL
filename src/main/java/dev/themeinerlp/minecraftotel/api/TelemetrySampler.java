package dev.themeinerlp.minecraftotel.api;

/**
 * Sampler invoked each interval to emit metrics through a collector.
 */
@FunctionalInterface
public interface TelemetrySampler {
    /**
     * Runs a sample and records values through the collector.
     *
     * @param snapshot latest telemetry snapshot
     * @param collector telemetry collector
     */
    void sample(TelemetrySnapshot snapshot, TelemetryCollector collector);
}
