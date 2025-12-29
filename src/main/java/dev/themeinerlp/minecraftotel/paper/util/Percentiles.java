package dev.themeinerlp.minecraftotel.paper.util;

import java.util.Arrays;

/**
 * Utility helpers for percentile calculations.
 */
public final class Percentiles {
    private Percentiles() {
    }

    /**
     * Computes the 95th percentile in milliseconds for a nanosecond array.
     *
     * @param nanos tick times in nanoseconds
     * @return p95 in milliseconds, or null if the input is empty
     */
    public static Double p95Ms(long[] nanos) {
        if (nanos == null || nanos.length == 0) {
            return null;
        }
        long[] copy = Arrays.copyOf(nanos, nanos.length);
        Arrays.sort(copy);
        int index = (int) Math.ceil(0.95d * copy.length) - 1;
        index = Math.max(0, Math.min(index, copy.length - 1));
        return copy[index] / 1_000_000d;
    }
}
