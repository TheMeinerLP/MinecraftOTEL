package dev.themeinerlp.minecraftotel.paper.state;

import java.util.Map;

public record TelemetrySnapshot(
        long playersOnline,
        Map<String, Long> entitiesLoadedByWorld,
        Map<String, Long> chunksLoadedByWorld,
        double[] tpsNullable,
        Double msptAvgNullable,
        Double msptP95Nullable
) {
}
