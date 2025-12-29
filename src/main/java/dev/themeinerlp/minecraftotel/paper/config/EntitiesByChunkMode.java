package dev.themeinerlp.minecraftotel.paper.config;

import java.util.Locale;

/**
 * Mode for entity type per chunk metrics.
 */
public enum EntitiesByChunkMode {
    OFF,
    LIGHT,
    HEAVY;

    public static EntitiesByChunkMode fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "off", "false", "0", "disabled" -> OFF;
            case "light", "lite" -> LIGHT;
            case "heavy", "full", "on", "true", "1", "enabled" -> HEAVY;
            default -> null;
        };
    }
}
