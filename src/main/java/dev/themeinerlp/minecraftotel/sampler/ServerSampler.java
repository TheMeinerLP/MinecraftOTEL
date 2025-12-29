package dev.themeinerlp.minecraftotel.sampler;

import org.bukkit.Server;

public interface ServerSampler {
    boolean isAvailable();

    SampleResult sample(Server server);

    record SampleResult(double[] tpsNullable, Double msptAvgNullable, Double msptP95Nullable) {
        public static SampleResult empty() {
            return new SampleResult(null, null, null);
        }
    }
}
