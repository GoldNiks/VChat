package com.vchat;

import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.InvocationTargetException;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * Optional LuckPerms integration without a hard runtime dependency.
 */
public final class LuckPermsBridge {
    private static Object api;
    private static boolean classMissing;

    private LuckPermsBridge() {
    }

    public static PlayerData read(ServerPlayer player) {
        Object luckPerms = getApi();
        if (luckPerms == null) return PlayerData.EMPTY;

        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class)
                    .invoke(userManager, player.getUUID());
            if (user == null) return PlayerData.EMPTY;

            String primaryGroup = stringValue(user.getClass().getMethod("getPrimaryGroup").invoke(user));
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);

            String prefix = stringValue(metaData.getClass().getMethod("getPrefix").invoke(metaData));
            String metaOrder = readMetaValue(metaData, VChatTabConfig.tabOrderMetaKey());
            Integer groupWeight = readGroupWeight(luckPerms, primaryGroup);

            return new PlayerData(prefix, primaryGroup, parseInteger(metaOrder), groupWeight);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return PlayerData.EMPTY;
        }
    }

    private static Object getApi() {
        if (api != null) return api;
        if (classMissing) return null;

        try {
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            api = provider.getMethod("get").invoke(null);
            return api;
        } catch (ClassNotFoundException e) {
            classMissing = true;
            return null;
        } catch (ReflectiveOperationException | LinkageError e) {
            if (e instanceof InvocationTargetException) {
                // LuckPerms may not be ready during early startup. Retry on the next refresh.
                return null;
            }
            return null;
        }
    }

    private static String readMetaValue(Object metaData, String key) throws ReflectiveOperationException {
        if (key == null || key.isBlank()) return "";
        Object value = metaData.getClass().getMethod("getMetaValue", String.class).invoke(metaData, key);
        return stringValue(value);
    }

    private static Integer readGroupWeight(Object luckPerms, String primaryGroup)
            throws ReflectiveOperationException {
        if (primaryGroup.isBlank()) return null;

        Object groupManager = luckPerms.getClass().getMethod("getGroupManager").invoke(luckPerms);
        Object group = groupManager.getClass().getMethod("getGroup", String.class)
                .invoke(groupManager, primaryGroup);
        if (group == null) return null;

        Object value = group.getClass().getMethod("getWeight").invoke(group);
        if (value instanceof OptionalInt optional && optional.isPresent()) {
            return optional.getAsInt();
        }
        return null;
    }

    private static Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    public record PlayerData(String prefix, String primaryGroup, Integer metaOrder, Integer groupWeight) {
        private static final PlayerData EMPTY = new PlayerData("", "", null, null);
    }
}
