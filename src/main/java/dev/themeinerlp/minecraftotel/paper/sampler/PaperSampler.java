package dev.themeinerlp.minecraftotel.paper.sampler;

import dev.themeinerlp.minecraftotel.paper.util.Percentiles;
import org.bukkit.Server;

/**
 * Samples TPS and MSPT from the Paper API directly.
 */
public final class PaperSampler implements ServerSampler {
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public SampleResult sample(Server server) {
        double[] tps = server.getTPS();
        double msptAvg = server.getAverageTickTime();
        Double msptP95 = Percentiles.p95Ms(server.getTickTimes());
        return new SampleResult(tps, msptAvg, msptP95);
    }
}
