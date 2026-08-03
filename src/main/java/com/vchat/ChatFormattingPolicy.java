package com.vchat;

import net.minecraft.server.level.ServerPlayer;

public final class ChatFormattingPolicy {
    private ChatFormattingPolicy() {
    }

    public static String filter(ServerPlayer player, String message) {
        if (message == null || message.isEmpty()) return "";

        Capabilities capabilities = capabilities(player);
        return FormattingSanitizer.filter(message, capabilities.colors(), capabilities.hex(),
                capabilities.styles(), capabilities.obfuscated());
    }

    public static Capabilities capabilities(ServerPlayer player) {
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
        return new Capabilities(colors, hex, styles, obfuscated);
    }

    public record Capabilities(boolean colors, boolean hex, boolean styles, boolean obfuscated) {
    }
}
