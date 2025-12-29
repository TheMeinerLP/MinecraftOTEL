package dev.themeinerlp.minecraftotel.velocity.state;

import dev.themeinerlp.minecraftotel.api.TelemetrySnapshot;

/**
 * Thread-safe storage for the latest Velocity telemetry snapshot.
 */
public final class VelocityTelemetryState {
    private volatile TelemetrySnapshot snapshot;

    /**
     * Creates a state instance initialized with an empty snapshot.
     */
    public VelocityTelemetryState() {
        this.snapshot = TelemetrySnapshot.empty();
    }

    /**
     * Returns the most recent telemetry snapshot.
     *
     * @return current snapshot
     */
    public TelemetrySnapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Replaces the current snapshot.
     *
     * @param snapshot new snapshot to expose
     */
    public void setSnapshot(TelemetrySnapshot snapshot) {
        this.snapshot = snapshot;
    }
}
