package dev.themeinerlp.minecraftotel.paper.sampler;

import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshotBuilder;
import dev.themeinerlp.minecraftotel.api.snapshot.TelemetrySnapshotSampler;
import dev.themeinerlp.minecraftotel.paper.config.PluginConfig;
import dev.themeinerlp.minecraftotel.paper.snapshot.PaperTelemetrySnapshotBuilder;
import dev.themeinerlp.minecraftotel.paper.state.TelemetryState;
import dev.themeinerlp.minecraftotel.paper.util.Percentiles;
import java.util.Arrays;
import java.util.Map;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import org.bukkit.Server;

/**
 * Builds telemetry snapshots for Paper using Paper or Spark sampling.
 */
public final class PaperSnapshotSampler implements TelemetrySnapshotSampler {
    private final Server server;
    private final PluginConfig config;
    private final TelemetryState state;
    private final TpsSampler tpsSampler;
    private long lastBaselineMillis;

    public PaperSnapshotSampler(Server server, PluginConfig config, TelemetryState state) {
        this.server = server;
        this.config = config;
        this.state = state;
        this.tpsSampler = new TpsSampler(config.enableTpsMspt && config.preferSpark, server);
    }

    @Override
    public void sample(TelemetrySnapshotBuilder builder) {
        if (!(builder instanceof PaperTelemetrySnapshotBuilder paperBuilder)) {
            return;
        }
        long playersOnline = server.getOnlinePlayers().size();
        SampleResult sampleResult = config.enableTpsMspt
                ? tpsSampler.sample(server)
                : SampleResult.empty();

        long now = System.currentTimeMillis();
        boolean baselineDue = lastBaselineMillis == 0L
                || now - lastBaselineMillis >= config.baselineScanIntervalSeconds * 1000L;
        if (baselineDue) {
            lastBaselineMillis = now;
        }

        Map<String, Long> baselineEntities = null;
        Map<String, Long> baselineEntityTypes = null;
        if (!config.enableEntities) {
            baselineEntities = Map.of();
            baselineEntityTypes = Map.of();
        } else if (baselineDue && !state.isEntityEventsAvailable()) {
            baselineEntities = state.scanEntities(server);
            baselineEntityTypes = state.scanEntitiesByType(server);
        }

        Map<String, Long> baselineChunks = null;
        if (!config.enableChunks) {
            baselineChunks = Map.of();
        } else if (baselineDue) {
            baselineChunks = state.scanChunks(server);
        }

        var snapshot = state.rebuildSnapshot(
                playersOnline,
                sampleResult.tpsNullable,
                sampleResult.msptAvgNullable,
                sampleResult.msptP95Nullable,
                baselineEntities,
                baselineEntityTypes,
                baselineChunks
        );

        paperBuilder.setPlayersOnline(snapshot.playersOnline());
        if (config.enableEntities) {
            snapshot.entitiesLoadedByWorld().ifPresent(paperBuilder::setEntitiesLoadedByWorld);
            snapshot.entitiesLoadedByType().ifPresent(paperBuilder::setEntitiesLoadedByType);
        }
        paperBuilder
                .setChunksLoadedByWorld(snapshot.chunksLoadedByWorld())
                .setExclusiveChunksLoaded(snapshot.exclusiveChunksLoaded())
                .setTps(snapshot.tpsNullable())
                .setMsptAvg(snapshot.msptAvgNullable())
                .setMsptP95(snapshot.msptP95Nullable());
    }

    public String samplerName() {
        return tpsSampler.getName();
    }

    private static final class SampleResult {
        private final double[] tpsNullable;
        private final Double msptAvgNullable;
        private final Double msptP95Nullable;

        private SampleResult(double[] tpsNullable, Double msptAvgNullable, Double msptP95Nullable) {
            this.tpsNullable = tpsNullable;
            this.msptAvgNullable = msptAvgNullable;
            this.msptP95Nullable = msptP95Nullable;
        }

        private static SampleResult empty() {
            return new SampleResult(null, null, null);
        }
    }

    private static final class TpsSampler {
        private final SparkBridge sparkBridge;

        private TpsSampler(boolean preferSpark, Server server) {
            this.sparkBridge = preferSpark && isSparkInstalled(server)
                    ? SparkBridge.tryCreate(server)
                    : null;
        }

        private SampleResult sample(Server server) {
            double[] tps = server.getTPS();
            double msptAvg = server.getAverageTickTime();
            Double msptP95 = Percentiles.p95Ms(server.getTickTimes());

            if (sparkBridge != null && sparkBridge.isAvailable()) {
                double[] sparkTps = sparkBridge.sampleTps();
                if (sparkTps != null) {
                    tps = sparkTps;
                }
                SparkBridge.MsptSample mspt = sparkBridge.sampleMspt();
                if (mspt != null) {
                    msptAvg = mspt.avg;
                    msptP95 = mspt.p95;
                }
            }

            return new SampleResult(tps, msptAvg, msptP95);
        }

        private String getName() {
            if (sparkBridge != null && sparkBridge.isAvailable()) {
                return "spark";
            }
            return "paper";
        }

        private static boolean isSparkInstalled(Server server) {
            try {
                return SparkProvider.get() != null;
            } catch (NoClassDefFoundError e) {
                return false;
            }
        }
    }

    private static final class SparkBridge {
        private final DoubleStatistic<StatisticWindow.TicksPerSecond> tpsStatistic;
        private final GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick> msptStatistic;

        private SparkBridge(
                DoubleStatistic<StatisticWindow.TicksPerSecond> tpsStatistic,
                GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick> msptStatistic
        ) {
            this.tpsStatistic = tpsStatistic;
            this.msptStatistic = msptStatistic;
        }

        private static SparkBridge tryCreate(Server server) {
            var plugin = server.getPluginManager().getPlugin("spark");
            if (plugin == null || !plugin.isEnabled()) {
                return null;
            }
            try {
                Spark spark = SparkProvider.get();
                var tps = spark.tps();
                var mspt = spark.mspt();
                if (tps == null && mspt == null) {
                    return null;
                }
                return new SparkBridge(tps, mspt);
            } catch (IllegalStateException | NoClassDefFoundError ignored) {
                return null;
            }
        }

        private boolean isAvailable() {
            return tpsStatistic != null || msptStatistic != null;
        }

        private double[] sampleTps() {
            if (tpsStatistic == null) {
                return null;
            }
            return new double[]{
                    tpsStatistic.poll(StatisticWindow.TicksPerSecond.MINUTES_1),
                    tpsStatistic.poll(StatisticWindow.TicksPerSecond.MINUTES_5),
                    tpsStatistic.poll(StatisticWindow.TicksPerSecond.MINUTES_15)
            };
        }

        private MsptSample sampleMspt() {
            if (msptStatistic == null) {
                return null;
            }
            var info = msptStatistic.poll(StatisticWindow.MillisPerTick.MINUTES_1);
            if (info == null) {
                return null;
            }
            return new MsptSample(info.mean(), info.percentile95th());
        }

        private record MsptSample(double avg, double p95) {
        }
    }
}
