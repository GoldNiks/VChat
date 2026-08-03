package com.vchat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TabListHandler {
    private static final String TEAM_NAMESPACE = "vch";
    private static final int MAX_TAB_ORDER = 9999;
    private static final Map<UUID, PlayerTabState> PLAYER_STATES = new HashMap<>();
    private int tick = 0;

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refreshPlayerTeam(player, true);
            sendTabList(player);
            player.sendSystemMessage(HexUtil.fromLegacy(VChatTabConfig.joinMessage()));
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            removeManagedTeam(player);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tick++;
            if (tick >= VChatTabConfig.tabUpdateIntervalTicks()) {
                tick = 0;
                var server = event.getServer();
                if (server != null) {
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        refreshPlayerTeam(player, false);
                        sendTabList(player);
                    }
                }
            }
        }
    }

    public static void refreshAll(net.minecraft.server.MinecraftServer server, boolean force) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            refreshPlayerTeam(player, force);
            sendTabList(player);
        }
    }

    private static void refreshPlayerTeam(ServerPlayer player, boolean force) {
        boolean showPrefix = VChatTabConfig.enableLuckPermsPrefixes();
        boolean sortPlayers = VChatTabConfig.enableTabSorting();
        if (!showPrefix && !sortPlayers) {
            removeManagedTeam(player);
            return;
        }

        LuckPermsBridge.PlayerData luckPerms = LuckPermsBridge.read(player);
        String prefix = showPrefix ? luckPerms.prefix() : "";
        int order = sortPlayers ? resolveTabOrder(luckPerms) : MAX_TAB_ORDER;

        Scoreboard board = player.getScoreboard();
        String teamName = buildTeamName(board, player, order);
        PlayerTabState desired = new PlayerTabState(teamName, prefix);
        PlayerTabState current = PLAYER_STATES.get(player.getUUID());

        PlayerTeam existing = board.getPlayerTeam(teamName);
        boolean teamIsValid = existing != null && existing.getPlayers().contains(player.getScoreboardName());
        if (!force && desired.equals(current) && teamIsValid) return;

        removeManagedTeam(player);

        PlayerTeam team = board.getPlayerTeam(teamName);
        if (team == null) {
            team = board.addPlayerTeam(teamName);
        }

        team.setPlayerPrefix(HexUtil.fromLegacy(prefix));
        board.addPlayerToTeam(player.getScoreboardName(), team);
        PLAYER_STATES.put(player.getUUID(), desired);
    }

    private static int resolveTabOrder(LuckPermsBridge.PlayerData data) {
        if (data.groupWeight() == null) return MAX_TAB_ORDER;
        int weight = clampOrder(data.groupWeight());
        return VChatTabConfig.higherWeightFirst() ? MAX_TAB_ORDER - weight : weight;
    }

    private static String buildTeamName(Scoreboard board, ServerPlayer player, int order) {
        String orderPart = String.format(Locale.ROOT, "%04d", clampOrder(order));
        int playerPartLength = 16 - TEAM_NAMESPACE.length() - orderPart.length();
        String playerPart = player.getScoreboardName().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "");
        if (playerPart.isEmpty()) {
            playerPart = player.getUUID().toString().replace("-", "");
        }
        playerPart = playerPart.substring(0, Math.min(playerPart.length(), playerPartLength));

        String candidate = TEAM_NAMESPACE + orderPart + playerPart;
        PlayerTeam existing = board.getPlayerTeam(candidate);
        if (existing == null || existing.getPlayers().contains(player.getScoreboardName())) {
            return candidate;
        }

        String hash = Integer.toUnsignedString(player.getUUID().hashCode(), 36);
        hash = hash.substring(Math.max(0, hash.length() - 2));
        int readableLength = Math.max(0, playerPartLength - hash.length());
        return TEAM_NAMESPACE + orderPart
                + playerPart.substring(0, Math.min(playerPart.length(), readableLength)) + hash;
    }

    private static int clampOrder(int order) {
        return Math.max(0, Math.min(MAX_TAB_ORDER, order));
    }

    private static void removeManagedTeam(ServerPlayer player) {
        Scoreboard board = player.getScoreboard();
        PlayerTabState state = PLAYER_STATES.remove(player.getUUID());
        PlayerTeam team = state == null ? board.getPlayersTeam(player.getScoreboardName())
                : board.getPlayerTeam(state.teamName());
        if (team != null && team.getName().startsWith(TEAM_NAMESPACE)
                && team.getPlayers().contains(player.getScoreboardName())) {
            board.removePlayerFromTeam(player.getScoreboardName(), team);
            if (team.getPlayers().isEmpty()) {
                board.removePlayerTeam(team);
            }
        }
    }

    public static void sendTabList(ServerPlayer player) {
        int online = player.getServer().getPlayerList().getPlayerCount();
        int max = player.getServer().getPlayerList().getMaxPlayers();
        String playerName = player.getDisplayName().getString();

        String h = VChatTabConfig.header()
                .replace("%online%", String.valueOf(online))
                .replace("%max%", String.valueOf(max))
                .replace("%player%", playerName);
        String f = VChatTabConfig.footer()
                .replace("%online%", String.valueOf(online))
                .replace("%max%", String.valueOf(max))
                .replace("%player%", playerName);

        player.connection.send(new ClientboundTabListPacket(HexUtil.fromLegacy(h), HexUtil.fromLegacy(f)));
    }

    private record PlayerTabState(String teamName, String prefix) {
    }
}
