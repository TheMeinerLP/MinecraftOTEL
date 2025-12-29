package dev.themeinerlp.minecraftotel.api.core;

import java.util.Optional;

/**
 * Static access point for the MinecraftOTEL API.
 *
 * @since 1.1.0
 * @version 1.1.0
 */
public final class MinecraftOtelApiProvider {
    private static volatile MinecraftOtelApi api;

    private MinecraftOtelApiProvider() {
    }

    /**
     * Registers the API instance.
     *
     * @param api api implementation
     */
    public static void register(MinecraftOtelApi api) {
        synchronized (MinecraftOtelApiProvider.class) {
            MinecraftOtelApiProvider.api = api;
        }
    }

    /**
     * Unregisters the API instance if it matches the current provider.
     *
     * @param api api implementation
     */
    public static void unregister(MinecraftOtelApi api) {
        synchronized (MinecraftOtelApiProvider.class) {
            if (MinecraftOtelApiProvider.api == api) {
                MinecraftOtelApiProvider.api = null;
            }
        }
    }

    /**
     * Returns the current API instance if available.
     *
     * @return optional api instance
     */
    public static Optional<MinecraftOtelApi> get() {
        return Optional.ofNullable(api);
    }
}
