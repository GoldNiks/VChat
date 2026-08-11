package com.vchat;

import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Optional VEconomy integration without a hard runtime dependency.
 * Resolves the player's formatted balance via the public {@code EconomyCore}
 * API. Returns "" when VEconomy is not installed or not started yet.
 */
public final class VEconomyBridge {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("VChat");
    private static final String ECONOMY_CORE_CLASS = "com.valorcraft.veconomy.EconomyCore";
    private static boolean classMissing;
    private static boolean formatterUnavailable;
    private static boolean reflectionFailureLogged;
    private static Object api;
    private static Method apiGetBalance;
    private static Method formatterFormat;

    private VEconomyBridge() {
    }

    /**
     * Formatted balance of the player in VEconomy display format (e.g.
     * "⛃1 234"), or "" when VEconomy is unavailable. Players without an
     * account report a balance of 0.
     */
    public static String balanceText(ServerPlayer player) {
        try {
            return formatBalance(getBalance(player.getUUID()));
        } catch (ReflectiveOperationException error) {
            logFailure(error);
            return "";
        }
    }

    private static long getBalance(UUID playerId) throws ReflectiveOperationException {
        if (classMissing || api == null && !initApi()) return 0;
        return (Long) apiGetBalance.invoke(api, playerId);
    }

    private static boolean initApi() throws ReflectiveOperationException {
        Class<?> coreClass = loadCore();
        if (coreClass == null) return false;
        api = coreClass.getMethod("api").invoke(null);
        if (api == null) return false;
        apiGetBalance = api.getClass().getMethod("getBalance", UUID.class);
        return true;
    }

    private static String formatBalance(long minor) {
        if (formatterUnavailable) return String.valueOf(minor);
        try {
            Class<?> coreClass = loadCore();
            if (coreClass == null) return String.valueOf(minor);
            Object formatter = coreClass.getMethod("formatter").invoke(null);
            if (formatter == null) {
                formatterUnavailable = true;
                return String.valueOf(minor);
            }
            if (formatterFormat == null) {
                formatterFormat = formatter.getClass().getMethod("format", long.class);
            }
            return (String) formatterFormat.invoke(formatter, minor);
        } catch (ReflectiveOperationException error) {
            formatterUnavailable = true;
            return String.valueOf(minor);
        }
    }

    private static Class<?> loadCore() {
        if (classMissing) return null;
        try {
            return Class.forName(ECONOMY_CORE_CLASS);
        } catch (ClassNotFoundException e) {
            classMissing = true;
            return null;
        }
    }

    private static void logFailure(Exception error) {
        if (reflectionFailureLogged) return;
        reflectionFailureLogged = true;
        LOGGER.warn("Could not read VEconomy balance", error);
    }
}