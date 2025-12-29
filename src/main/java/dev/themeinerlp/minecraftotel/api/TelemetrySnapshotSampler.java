package dev.themeinerlp.minecraftotel.api;

/**
 * Sampler that populates a TelemetrySnapshotBuilder with platform data.
 */
@FunctionalInterface
public interface TelemetrySnapshotSampler {
    /**
     * Populates the builder with snapshot values.
     *
     * @param builder snapshot builder
     */
    void sample(TelemetrySnapshotBuilder builder);
}
