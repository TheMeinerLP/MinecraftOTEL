package dev.themeinerlp.minecraftotel.api.snapshot;

/**
 * Sampler that populates a TelemetrySnapshotBuilder with platform data.
 *
 * @since 1.1.0
 * @version 1.1.0
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
