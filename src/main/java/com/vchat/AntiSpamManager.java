package com.vchat;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class AntiSpamManager {
    private static final Map<UUID, MessageState> STATES = new HashMap<>();

    private AntiSpamManager() {
    }

    public static boolean allow(ServerPlayer player, String message) {
        if (message == null || message.isBlank()) {
            send(player, VChatTabConfig.emptyMessage());
            return false;
        }
        if (!VChatTabConfig.antiSpamEnabled() || player.hasPermissions(2)
                || LuckPermsBridge.hasPermission(player, "vchat.antispam.bypass")) {
            return true;
        }

        int length = message == null ? 0 : message.codePointCount(0, message.length());
        int maxLength = VChatTabConfig.maxMessageLength();
        if (length > maxLength) {
            send(player, VChatTabConfig.tooLongMessage().replace("<max>", String.valueOf(maxLength)));
            return false;
        }

        long now = System.nanoTime();
        MessageState previous = STATES.get(player.getUUID());
        if (previous != null) {
            long elapsedMillis = (now - previous.sentAtNanos()) / 1_000_000L;
            long remaining = VChatTabConfig.cooldownMillis() - elapsedMillis;
            if (remaining > 0) {
                String seconds = String.format(Locale.ROOT, "%.1f", remaining / 1000.0);
                send(player, VChatTabConfig.tooFastMessage().replace("<seconds>", seconds));
                return false;
            }

            String normalized = normalize(message);
            if (VChatTabConfig.blockRepeatedMessages()
                    && elapsedMillis <= VChatTabConfig.repeatWindowMillis()
                    && normalized.equals(previous.normalizedMessage())) {
                send(player, VChatTabConfig.repeatedMessage());
                return false;
            }
        }

        STATES.put(player.getUUID(), new MessageState(normalize(message), now));
        return true;
    }

    public static void clear(UUID playerId) {
        STATES.remove(playerId);
    }

    private static String normalize(String message) {
        return message == null ? "" : message.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static void send(ServerPlayer player, String message) {
        player.sendSystemMessage(HexUtil.fromLegacy(message));
    }

    private record MessageState(String normalizedMessage, long sentAtNanos) {
    }
}
