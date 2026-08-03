package com.vchat;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageFormatter {
    private static final Pattern PLACEHOLDER = Pattern.compile(
            "<(prefix|suffix|name|display_name|message|group|world|channel)>"
    );

    private MessageFormatter() {
    }

    public static Component chat(String pattern, ServerPlayer player, String message, String channel) {
        LuckPermsBridge.PlayerData data = LuckPermsBridge.read(player);
        return format(pattern, player, data, message, channel);
    }

    public static Component player(String pattern, ServerPlayer player, LuckPermsBridge.PlayerData data) {
        return format(pattern, player, data, "", "tab");
    }

    private static Component format(String pattern, ServerPlayer player, LuckPermsBridge.PlayerData data,
                                    String message, String channel) {
        Map<String, String> values = Map.of(
                "prefix", VChatTabConfig.enableLuckPermsPrefixes() ? data.prefix() : "",
                "suffix", VChatTabConfig.enableLuckPermsSuffixes() ? data.suffix() : "",
                "name", player.getScoreboardName(),
                "display_name", player.getDisplayName().getString(),
                "message", message == null ? "" : message,
                "group", data.primaryGroup(),
                "world", player.serverLevel().dimension().location().toString(),
                "channel", channel == null ? "" : channel
        );

        Matcher matcher = PLACEHOLDER.matcher(pattern == null ? "" : pattern);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(values.get(matcher.group(1))));
        }
        matcher.appendTail(result);
        return HexUtil.fromLegacy(result.toString());
    }
}
