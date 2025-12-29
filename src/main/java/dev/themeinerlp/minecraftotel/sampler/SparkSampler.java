package dev.themeinerlp.minecraftotel.sampler;

import org.bukkit.Server;

public final class SparkSampler implements ServerSampler {
    private final ServerSampler fallback;
    private final boolean available;

    public SparkSampler(Server server, ServerSampler fallback) {
        this.fallback = fallback;
        this.available = server.getPluginManager().getPlugin("spark") != null;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public SampleResult sample(Server server) {
        if (!available) {
            return fallback.sample(server);
        }
        return fallback.sample(server);
    }
}
