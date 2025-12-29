package dev.themeinerlp.minecraftotel.api.service;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;

/**
 * Listener notified after each telemetry sample is taken.
 *
 * @since 1.1.0
 * @version 1.1.0
 */
@FunctionalInterface
public interface TelemetryListener {
    /**
     * Called after a telemetry snapshot is updated.
     *
     * @param snapshot latest snapshot
     */
    void onSample(TelemetrySnapshot<?> snapshot);
}
