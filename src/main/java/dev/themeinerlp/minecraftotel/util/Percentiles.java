package dev.themeinerlp.minecraftotel.util;

import java.util.Arrays;

public final class Percentiles {
    private Percentiles() {
    }

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
