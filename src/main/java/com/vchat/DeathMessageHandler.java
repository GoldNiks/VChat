package com.vchat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
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
                || !(event.getEntity() instanceof ServerPlayer victim)
                || !victim.serverLevel().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)) {
            return;
        }

        maskForCurrentTick(victim);
        if (event.getSource().getEntity() instanceof ServerPlayer directKiller) {
            maskForCurrentTick(directKiller);
        }
        // CombatTracker retains indirect attackers, e.g. a player who pushed
        // the victim before a fall, even when DamageSource no longer has them.
        if (victim.getKillCredit() instanceof ServerPlayer creditedKiller) {
            maskForCurrentTick(creditedKiller);
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
