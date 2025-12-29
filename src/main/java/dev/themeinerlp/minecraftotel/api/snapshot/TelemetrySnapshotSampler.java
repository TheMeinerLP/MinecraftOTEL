package dev.themeinerlp.minecraftotel.api.snapshot;

/**
 * Sampler that populates a platform-specific TelemetrySnapshotBuilder.
 *
 * @since 1.1.0
 * @version 1.1.0
 */
@FunctionalInterface
public interface TelemetrySnapshotSampler {
    /**
     * Populates the builder with platform snapshot values.
     *
     * @param builder platform-specific snapshot builder
     */
    void sample(TelemetrySnapshotBuilder builder);
}
