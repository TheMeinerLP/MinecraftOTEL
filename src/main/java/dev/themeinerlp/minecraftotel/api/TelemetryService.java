package dev.themeinerlp.minecraftotel.api;

/**
 * Common telemetry service contract for Paper and Velocity.
 */
public interface TelemetryService {
    /**
     * Returns the platform identifier (for example "paper" or "velocity").
     *
     * @return platform identifier
     */
    String getPlatform();

    /**
     * Returns whether the service is currently running.
     *
     * @return true when sampling is active
     */
    boolean isRunning();

    /**
     * Returns the most recent telemetry snapshot.
     *
     * @return snapshot
     */
    TelemetrySnapshot getSnapshot();

    /**
     * Adds a snapshot sampler to contribute to the next snapshot build.
     *
     * @param sampler snapshot sampler to add
     */
    void addSnapshotSampler(TelemetrySnapshotSampler sampler);

    /**
     * Removes a previously registered snapshot sampler.
     *
     * @param sampler snapshot sampler to remove
     */
    void removeSnapshotSampler(TelemetrySnapshotSampler sampler);

    /**
     * Registers an additional sampler to run each sampling interval.
     *
     * @param sampler sampler to add
     */
    void addSampler(TelemetrySampler sampler);

    /**
     * Removes a previously registered sampler.
     *
     * @param sampler sampler to remove
     */
    void removeSampler(TelemetrySampler sampler);

    /**
     * Adds a listener for snapshot updates.
     *
     * @param listener listener to add
     */
    void addListener(TelemetryListener listener);

    /**
     * Removes a listener for snapshot updates.
     *
     * @param listener listener to remove
     */
    void removeListener(TelemetryListener listener);

    /**
     * Starts sampling and metric registration.
     */
    void start();

    /**
     * Stops sampling and unregisters listeners.
     */
    void stop();
}
