package dev.themeinerlp.minecraftotel.api;

/**
 * Simple snapshot store implementation for platforms without extra state.
 */
public final class TelemetrySnapshotStore implements TelemetryStateStore {
    private volatile TelemetrySnapshot snapshot = TelemetrySnapshot.empty();

    @Override
    public TelemetrySnapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public void setSnapshot(TelemetrySnapshot snapshot) {
        this.snapshot = snapshot;
    }
}
