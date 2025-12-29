package dev.themeinerlp.minecraftotel.velocity.sampler;

import dev.themeinerlp.minecraftotel.velocity.state.VelocityTelemetrySnapshot;

/**
 * Builds telemetry snapshots from the current proxy state.
 */
public interface VelocitySampler {
    /**
     * Samples the proxy and returns a snapshot with the latest counts.
     *
     * @return immutable telemetry snapshot
     */
    VelocityTelemetrySnapshot sample();
}
