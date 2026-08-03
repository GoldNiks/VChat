package com.vchat;

import net.minecraft.server.level.ServerPlayer;

public final class ChatFormattingPolicy {
    private ChatFormattingPolicy() {
    }

    public static String filter(ServerPlayer player, String message) {
        if (message == null || message.isEmpty()) return "";

        boolean enabled = VChatTabConfig.playerFormattingEnabled();
        boolean operator = player.hasPermissions(2);
        boolean colors = enabled && (operator || VChatTabConfig.colorsForEveryone()
                || LuckPermsBridge.hasPermission(player, "vchat.format.color"));
        boolean hex = enabled && (operator || VChatTabConfig.hexForEveryone()
                || LuckPermsBridge.hasPermission(player, "vchat.format.hex"));
        boolean styles = enabled && (operator || VChatTabConfig.stylesForEveryone()
                || LuckPermsBridge.hasPermission(player, "vchat.format.style"));
        boolean obfuscated = enabled && (operator || VChatTabConfig.obfuscatedForEveryone()
                || LuckPermsBridge.hasPermission(player, "vchat.format.obfuscated"));

        return FormattingSanitizer.filter(message, colors, hex, styles, obfuscated);
    }
}
