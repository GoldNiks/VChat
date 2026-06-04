package com.vchat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TabListHandler {
    private int tick = 0;

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            applyTeamPrefix(player);
            sendTabList(player);
            player.sendSystemMessage(HexUtil.fromLegacy(VChatTabConfig.joinMessage()));
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            removeTeamPrefix(player);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tick++;
            if (tick >= 20) {
                tick = 0;
                var server = event.getServer();
                if (server != null) {
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        sendTabList(player);
                    }
                }
            }
        }
    }

    private void applyTeamPrefix(ServerPlayer player) {
        String prefix = HexUtil.getLpPrefix(player);
        if (prefix.isEmpty()) return;

        Scoreboard board = player.getScoreboard();
        String teamName = "vc_" + player.getUUID().toString().replace("-", "").substring(0, 8);
        PlayerTeam team = board.getPlayerTeam(teamName);
        if (team == null) {
            team = board.addPlayerTeam(teamName);
        }

        team.setPlayerPrefix(HexUtil.fromLegacy(prefix));
        board.addPlayerToTeam(player.getScoreboardName(), team);
    }

    private void removeTeamPrefix(ServerPlayer player) {
        Scoreboard board = player.getScoreboard();
        String teamName = "vc_" + player.getUUID().toString().replace("-", "").substring(0, 8);
        PlayerTeam team = board.getPlayerTeam(teamName);
        if (team != null) {
            board.removePlayerFromTeam(player.getScoreboardName(), team);
            board.removePlayerTeam(team);
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
}
