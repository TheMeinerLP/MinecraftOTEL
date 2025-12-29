package dev.themeinerlp.minecraftotel.api.state;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;

/**
 * Simple snapshot store implementation for platforms without extra state.
 *
 * @since 1.1.0
 * @version 1.1.0
 */
public final class TelemetrySnapshotStore implements TelemetryStateStore {
    private volatile TelemetrySnapshot<?> snapshot = TelemetrySnapshot.empty();

    @Override
    public TelemetrySnapshot<?> getSnapshot() {
        return snapshot;
    }

    @Override
    public void setSnapshot(TelemetrySnapshot<?> snapshot) {
        this.snapshot = snapshot;
    }
}
