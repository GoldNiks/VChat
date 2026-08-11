package com.vchat;

import java.util.Locale;

/**
 * Pure formatting helpers for server TPS. Kept free of Minecraft classes so
 * the rounding logic can be unit tested off the server.
 */
public final class TpsUtil {

    private TpsUtil() {
    }

    /**
     * Converts an average tick time in milliseconds (see
     * {@code MinecraftServer.getAverageTickTime()}) into the standard
     * "20.0" style TPS string. Values at or below 0 (server just started)
     * report a perfect 20.0; the result is clamped to 20.0.
     */
    public static String format(float averageTickMillis) {
        float tps = 20f;
        if (averageTickMillis > 0f) {
            tps = Math.min(tps, 1000f / averageTickMillis);
        }
        return String.format(Locale.ROOT, "%.1f", tps);
    }
}