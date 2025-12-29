package dev.themeinerlp.minecraftotel.velocity.state;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe storage for the latest Velocity telemetry snapshot.
 */
public final class VelocityTelemetryState {
    private final AtomicReference<VelocityTelemetrySnapshot> snapshotRef;

    /**
     * Creates a state instance initialized with an empty snapshot.
     */
    public VelocityTelemetryState() {
        this.snapshotRef = new AtomicReference<>(VelocityTelemetrySnapshot.empty());
    }

    /**
     * Returns the most recent telemetry snapshot.
     *
     * @return current snapshot
     */
    public VelocityTelemetrySnapshot getSnapshot() {
        return snapshotRef.get();
    }

    /**
     * Replaces the current snapshot.
     *
     * @param snapshot new snapshot to expose
     */
    public void setSnapshot(VelocityTelemetrySnapshot snapshot) {
        snapshotRef.set(snapshot);
    }
}
