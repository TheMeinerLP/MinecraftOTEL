package dev.themeinerlp.minecraftotel.paper.sampler;

import org.bukkit.Server;

/**
 * Samples TPS and MSPT metrics from a server implementation.
 */
public interface ServerSampler {
    /**
     * Returns whether this sampler can produce values.
     *
     * @return true if sampling is available
     */
    boolean isAvailable();

    /**
     * Samples TPS and MSPT values.
     *
     * @param server Bukkit server
     * @return sample result
     */
    SampleResult sample(Server server);

    /**
     * Result container for TPS and MSPT samples.
     *
     * @param tpsNullable TPS windows or null
     * @param msptAvgNullable average MSPT or null
     * @param msptP95Nullable p95 MSPT or null
     */
    record SampleResult(double[] tpsNullable, Double msptAvgNullable, Double msptP95Nullable) {
        /**
         * Creates an empty result with all fields unset.
         *
         * @return empty sample result
         */
        public static SampleResult empty() {
            return new SampleResult(null, null, null);
        }
    }
}
