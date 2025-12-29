package dev.themeinerlp.minecraftotel.api;

/**
 * Common interface for storing and retrieving telemetry snapshots.
 */
public interface TelemetryStateStore {
    /**
     * Returns the most recent snapshot.
     *
     * @return snapshot
     */
    TelemetrySnapshot getSnapshot();

    /**
     * Updates the current snapshot.
     *
     * @param snapshot new snapshot
     */
    void setSnapshot(TelemetrySnapshot snapshot);
}
