package dev.themeinerlp.minecraftotel.api.state;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshot;
import java.util.Objects;

/**
 * Simple snapshot store implementation for platforms without extra state.
 *
 * @since 1.1.0
 * @version 1.1.0
 */
public final class TelemetrySnapshotStore implements TelemetryStateStore {
    private volatile TelemetrySnapshot snapshot;

    /**
     * Creates a snapshot store initialized with a platform snapshot.
     *
     * @param snapshot initial snapshot
     */
    public TelemetrySnapshotStore(TelemetrySnapshot snapshot) {
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
    }

    @Override
    public TelemetrySnapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public void setSnapshot(TelemetrySnapshot snapshot) {
        this.snapshot = snapshot;
    }
}
