package dev.themeinerlp.minecraftotel.api;

/**
 * Listener notified after each telemetry sample is taken.
 */
@FunctionalInterface
public interface TelemetryListener {
    /**
     * Called after a telemetry snapshot is updated.
     *
     * @param snapshot latest snapshot
     */
    void onSample(TelemetrySnapshot snapshot);
}
