package dev.themeinerlp.minecraftotel.velocity.state;

import dev.themeinerlp.minecraftotel.api.TelemetrySnapshot;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe storage for the latest Velocity telemetry snapshot.
 */
public final class VelocityTelemetryState {
    private final AtomicReference<TelemetrySnapshot> snapshotRef;

    /**
     * Creates a state instance initialized with an empty snapshot.
     */
    public VelocityTelemetryState() {
        this.snapshotRef = new AtomicReference<>(TelemetrySnapshot.empty());
    }

    /**
     * Returns the most recent telemetry snapshot.
     *
     * @return current snapshot
     */
    public TelemetrySnapshot getSnapshot() {
        return snapshotRef.get();
    }

    /**
     * Replaces the current snapshot.
     *
     * @param snapshot new snapshot to expose
     */
    public void setSnapshot(TelemetrySnapshot snapshot) {
        snapshotRef.set(snapshot);
    }
}
