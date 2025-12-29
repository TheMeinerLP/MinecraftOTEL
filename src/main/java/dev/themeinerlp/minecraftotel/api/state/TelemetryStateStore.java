package dev.themeinerlp.minecraftotel.api.state;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;

/**
 * Common interface for storing and retrieving telemetry snapshots.
 *
 * @since 1.1.0
 * @version 1.1.0
 */
public interface TelemetryStateStore {
    /**
     * Returns the most recent snapshot.
     *
     * @return snapshot
     */
    TelemetrySnapshot<?> getSnapshot();

    /**
     * Updates the current snapshot.
     *
     * @param snapshot new snapshot
     */
    void setSnapshot(TelemetrySnapshot<?> snapshot);
}
