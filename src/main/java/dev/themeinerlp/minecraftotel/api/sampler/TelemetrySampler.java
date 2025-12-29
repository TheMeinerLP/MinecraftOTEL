package dev.themeinerlp.minecraftotel.api.sampler;

import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;

/**
 * Sampler invoked each interval to emit metrics through a collector.
 *
 * @since 1.1.0
 * @version 1.1.0
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
