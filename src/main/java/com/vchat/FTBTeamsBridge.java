package com.vchat;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;

/**
 * Optional FTB Teams integration without a hard runtime dependency.
 */
public final class FTBTeamsBridge {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("VChat");
    private static final String API_CLASS = "dev.ftb.mods.ftbteams.api.FTBTeamsAPI";
    private static final String API_INTERFACE = API_CLASS + "$API";
    private static final String TEAM_MANAGER_INTERFACE = "dev.ftb.mods.ftbteams.api.TeamManager";
    private static final String TEAM_INTERFACE = "dev.ftb.mods.ftbteams.api.Team";
    private static final String TEAM_RANK_CLASS = "dev.ftb.mods.ftbteams.api.TeamRank";
    private static boolean classMissing;
    private static boolean reflectionFailureLogged;

    private FTBTeamsBridge() {
    }

    public static Component createNameHover(ServerPlayer player) {
        if (!VChatTabConfig.ftbTeamsHoverEnabled()) return null;

        try {
            Object api = api();
            if (api == null || !invokeBoolean(api, API_INTERFACE, "isManagerLoaded")) return null;

            Object manager = invoke(api, API_INTERFACE, "getManager");
            Object team = findTeam(manager, player);
            if (team == null || invokeBoolean(team, TEAM_INTERFACE, "isPlayerTeam")) {
                return VChatTabConfig.ftbTeamsHideWithoutTeam()
                        ? null
                        : HexUtil.fromLegacy(VChatTabConfig.ftbTeamsNoTeamText());
            }

            Component hover = Component.empty();
            boolean hasLine = false;

            if (VChatTabConfig.ftbTeamsShowTeamName()) {
                Component teamName = component(team, TEAM_INTERFACE, "getColoredName");
                if (teamName != null) {
                    hover = appendLine(hover, hasLine,
                            HexUtil.fromLegacy(VChatTabConfig.ftbTeamsTeamLabel()).copy().append(teamName));
                    hasLine = true;
                }
            }

            if (VChatTabConfig.ftbTeamsShowPlayerRank()) {
                Object rank = invoke(team, TEAM_INTERFACE, "getRankForPlayer",
                        new Class<?>[]{java.util.UUID.class}, player.getUUID());
                Component rankName = rank == null ? null
                        : component(rank, TEAM_RANK_CLASS, "getDisplayName");
                if (rankName != null) {
                    hover = appendLine(hover, hasLine,
                            HexUtil.fromLegacy(VChatTabConfig.ftbTeamsRankLabel()).copy().append(rankName));
                    hasLine = true;
                }
            }

            if (VChatTabConfig.ftbTeamsShowMemberCount()) {
                Object members = invoke(team, TEAM_INTERFACE, "getMembers");
                if (members instanceof Collection<?> collection) {
                    Component memberLine = HexUtil.fromLegacy(VChatTabConfig.ftbTeamsMembersLabel())
                            .copy().append(Component.literal(String.valueOf(collection.size())));
                    hover = appendLine(hover, hasLine, memberLine);
                    hasLine = true;
                }
            }

            return hasLine ? hover : null;
        } catch (ReflectiveOperationException | LinkageError error) {
            if (!reflectionFailureLogged) {
                reflectionFailureLogged = true;
                LOGGER.warn("FTB Teams integration failed; name hover is temporarily unavailable", error);
            }
            return null;
        }
    }

    private static Object api() throws ReflectiveOperationException {
        if (classMissing) return null;

        try {
            Class<?> provider = Class.forName(API_CLASS);
            return provider.getMethod("api").invoke(null);
        } catch (ClassNotFoundException e) {
            classMissing = true;
            return null;
        }
    }

    private static Object findTeam(Object manager, ServerPlayer player) throws ReflectiveOperationException {
        Method method = Class.forName(TEAM_MANAGER_INTERFACE)
                .getMethod("getTeamForPlayer", ServerPlayer.class);
        Object value = method.invoke(manager, player);
        if (value instanceof Optional<?> optional) return optional.orElse(null);
        return null;
    }

    private static boolean invokeBoolean(Object target, String ownerClass, String method)
            throws ReflectiveOperationException {
        return (boolean) invoke(target, ownerClass, method);
    }

    private static Component component(Object target, String ownerClass, String method)
            throws ReflectiveOperationException {
        Object value = invoke(target, ownerClass, method);
        return value instanceof Component component ? component : null;
    }

    private static Object invoke(Object target, String ownerClass, String method)
            throws ReflectiveOperationException {
        return invoke(target, ownerClass, method, new Class<?>[0]);
    }

    private static Object invoke(Object target, String ownerClass, String method,
                                 Class<?>[] parameterTypes, Object... arguments)
            throws ReflectiveOperationException {
        return Class.forName(ownerClass).getMethod(method, parameterTypes).invoke(target, arguments);
    }

    private static Component appendLine(Component current, boolean prependNewline, Component line) {
        if (prependNewline) current = current.copy().append(Component.literal("\n"));
        return current.copy().append(line);
    }
}
