package com.vchat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MentionProcessor {
    private MentionProcessor() {
    }

    public static Result process(MinecraftServer server, String message) {
        if (!VChatTabConfig.mentionsEnabled() || message == null || message.isEmpty()) {
            return new Result(message == null ? "" : message, Set.of());
        }

        String result = message;
        Set<UUID> mentionedPlayers = new HashSet<>();
        var players = server.getPlayerList().getPlayers().stream()
                .sorted(Comparator.comparingInt((ServerPlayer p) -> p.getScoreboardName().length()).reversed())
                .toList();

        for (ServerPlayer player : players) {
            String name = player.getScoreboardName();
            Pattern pattern = Pattern.compile("(?i)(?<![A-Za-z0-9_])@" + Pattern.quote(name)
                    + "(?![A-Za-z0-9_])");
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                mentionedPlayers.add(player.getUUID());
                String replacement = VChatTabConfig.mentionFormat().replace("<name>", name);
                result = matcher.replaceAll(Matcher.quoteReplacement(replacement));
            }
        }

        return new Result(result, Set.copyOf(mentionedPlayers));
    }

    public static void notify(ServerPlayer player) {
        if (!VChatTabConfig.mentionSoundEnabled()) return;

        SoundEvent sound = SoundEvents.EXPERIENCE_ORB_PICKUP;
        ResourceLocation soundId = ResourceLocation.tryParse(VChatTabConfig.mentionSound());
        if (soundId != null) {
            SoundEvent configuredSound = ForgeRegistries.SOUND_EVENTS.getValue(soundId);
            if (configuredSound != null) sound = configuredSound;
        }
        player.playNotifySound(sound, SoundSource.PLAYERS,
                VChatTabConfig.mentionVolume(), VChatTabConfig.mentionPitch());
    }

    public record Result(String message, Set<UUID> mentionedPlayers) {
    }
}
