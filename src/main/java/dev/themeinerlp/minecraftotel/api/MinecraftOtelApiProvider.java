package dev.themeinerlp.minecraftotel.api;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Static access point for the MinecraftOTEL API.
 */
public final class MinecraftOtelApiProvider {
    private static final AtomicReference<MinecraftOtelApi> API = new AtomicReference<>();

    private MinecraftOtelApiProvider() {
    }

    /**
     * Registers the API instance.
     *
     * @param api api implementation
     */
    public static void register(MinecraftOtelApi api) {
        API.set(api);
    }

    /**
     * Unregisters the API instance if it matches the current provider.
     *
     * @param api api implementation
     */
    public static void unregister(MinecraftOtelApi api) {
        API.compareAndSet(api, null);
    }

    /**
     * Returns the current API instance if available.
     *
     * @return optional api instance
     */
    public static Optional<MinecraftOtelApi> get() {
        return Optional.ofNullable(API.get());
    }
}
