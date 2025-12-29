package dev.themeinerlp.minecraftotel.paper.sampler;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow.MillisPerTick;
import me.lucko.spark.api.statistic.StatisticWindow.TicksPerSecond;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import org.bukkit.Server;

/**
 * Samples TPS/MSPT from Spark with a fallback to Paper sampling.
 */
public final class SparkSampler implements ServerSampler {
    private final ServerSampler fallback;
    private final DoubleStatistic<TicksPerSecond> tpsStatistic;
    private final GenericStatistic<DoubleAverageInfo, MillisPerTick> msptStatistic;
    private final boolean available;

    /**
     * Creates a Spark sampler with a fallback sampler.
     *
     * @param fallback sampler used when Spark is unavailable
     */
    public SparkSampler(ServerSampler fallback) {
        this.fallback = fallback;
        DoubleStatistic<TicksPerSecond> tps = null;
        GenericStatistic<DoubleAverageInfo, MillisPerTick> mspt = null;
        boolean ok = false;
        try {
            Spark spark = SparkProvider.get();
            tps = spark.tps();
            mspt = spark.mspt();
            ok = tps != null || mspt != null;
        } catch (IllegalStateException | NoClassDefFoundError ignored) {
            ok = false;
        }
        this.tpsStatistic = tps;
        this.msptStatistic = mspt;
        this.available = ok;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public SampleResult sample(Server server) {
        SampleResult fallbackResult = fallback.sample(server);
        if (!available) {
            return fallbackResult;
        }

        double[] tps = fallbackResult.tpsNullable();
        Double msptAvg = fallbackResult.msptAvgNullable();
        Double msptP95 = fallbackResult.msptP95Nullable();

        if (tpsStatistic != null) {
            tps = new double[]{
                    tpsStatistic.poll(TicksPerSecond.MINUTES_1),
                    tpsStatistic.poll(TicksPerSecond.MINUTES_5),
                    tpsStatistic.poll(TicksPerSecond.MINUTES_15)
            };
        }

        if (msptStatistic != null) {
            DoubleAverageInfo info = msptStatistic.poll(MillisPerTick.MINUTES_1);
            if (info != null) {
                msptAvg = info.mean();
                msptP95 = info.percentile95th();
            }
        }

        return new SampleResult(tps, msptAvg, msptP95);
    }
}
