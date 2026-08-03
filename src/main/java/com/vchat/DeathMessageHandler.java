package com.vchat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class DeathMessageHandler {
    private static final Set<UUID> MASKED_PLAYERS = new HashSet<>();

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!VChatTabConfig.hidePlayerHeadsInDeathMessages()
                || !(event.getEntity() instanceof ServerPlayer victim)) {
            return;
        }

        // CombatTracker can mention an indirect killer who is not present in
        // the current DamageSource (for example, a fall after being hit).
        // Mask every online player for this tick so Chat Heads cannot switch
        // from the victim to another player name in the same death message.
        for (ServerPlayer player : victim.getServer().getPlayerList().getPlayers()) {
            maskForCurrentTick(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onNameFormat(PlayerEvent.NameFormat event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !MASKED_PLAYERS.contains(player.getUUID())) {
            return;
        }

        event.setDisplayname(DeathMessageFormatter.hidePlayerName(
                event.getDisplayname(), player.getScoreboardName()));
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || MASKED_PLAYERS.isEmpty()) return;

        Set<UUID> playersToRestore = Set.copyOf(MASKED_PLAYERS);
        MASKED_PLAYERS.clear();
        for (UUID playerId : playersToRestore) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(playerId);
            if (player != null) player.refreshDisplayName();
        }
    }

    private static void maskForCurrentTick(ServerPlayer player) {
        if (MASKED_PLAYERS.add(player.getUUID())) {
            player.refreshDisplayName();
        }
    }
}
