package com.vchat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageFormatter {
    private static final String NAME_MARKER = "\uE000vchat_name\uE001";
    private static final String DISPLAY_NAME_MARKER = "\uE000vchat_display_name\uE001";
    private static final Pattern PLACEHOLDER = Pattern.compile(
            "<(prefix|suffix|name|display_name|message|group|world|channel|stage|balance|tps)>"
    );

    private MessageFormatter() {
    }

    private static boolean patternContainsStage(String pattern) {
        return pattern != null && pattern.contains("<stage>");
    }

    public static Component chat(String pattern, ServerPlayer player, String message, String channel) {
        LuckPermsBridge.PlayerData data = LuckPermsBridge.read(player);
        return format(pattern, player, data, message, channel, true);
    }

    public static Component player(String pattern, ServerPlayer player, LuckPermsBridge.PlayerData data) {
        return format(pattern, player, data, "", "tab", false);
    }

    private static Component format(String pattern, ServerPlayer player, LuckPermsBridge.PlayerData data,
                                    String message, String channel, boolean enableNameHover) {
        Component hover = enableNameHover ? FTBTeamsBridge.createNameHover(player) : null;
        String stage = VChatTabConfig.stagesEnabled()
                && (VChatTabConfig.stagesAppendToSuffix() || patternContainsStage(pattern))
                ? FTBQuestsStageBridge.stageText(player) : "";
        String suffix = VChatTabConfig.enableLuckPermsSuffixes() ? data.suffix() : "";
        if (!stage.isEmpty() && VChatTabConfig.stagesAppendToSuffix()) {
            String separator = VChatTabConfig.stageSeparator();
            if (suffix.isEmpty() || suffix.endsWith(" ") || stage.startsWith(" ")) {
                suffix = suffix + stage;
            } else {
                suffix = suffix + separator + stage;
            }
        }
        Map<String, String> values = Map.ofEntries(
                Map.entry("prefix", VChatTabConfig.enableLuckPermsPrefixes() ? data.prefix() : ""),
                Map.entry("suffix", suffix),
                Map.entry("name", player.getScoreboardName()),
                Map.entry("display_name", player.getDisplayName().getString()),
                Map.entry("message", message == null ? "" : message),
                Map.entry("group", data.primaryGroup()),
                Map.entry("world", player.serverLevel().dimension().location().toString()),
                Map.entry("channel", channel == null ? "" : channel),
                Map.entry("stage", stage),
                Map.entry("balance", VEconomyBridge.balanceText(player)),
                Map.entry("tps", TpsUtil.format(player.getServer().getAverageTickTime()))
        );

        Matcher matcher = PLACEHOLDER.matcher(pattern == null ? "" : pattern);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = values.get(placeholder);
            if (hover != null && "name".equals(placeholder)) replacement = NAME_MARKER;
            if (hover != null && "display_name".equals(placeholder)) replacement = DISPLAY_NAME_MARKER;
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        Component formatted = HexUtil.fromLegacy(result.toString());
        if (hover == null) return formatted;

        MutableComponent withNameHover = replaceMarker(formatted, NAME_MARKER,
                player.getScoreboardName(), hover);
        return replaceMarker(withNameHover, DISPLAY_NAME_MARKER,
                player.getDisplayName().getString(), hover);
    }

    private static MutableComponent replaceMarker(Component source, String marker,
                                                   String replacement, Component hover) {
        MutableComponent result = Component.empty().withStyle(source.getStyle());
        for (Component sibling : source.getSiblings()) {
            appendMarkedText(result, sibling.getString(), sibling, marker, replacement, hover);
        }
        return result;
    }

    private static void appendMarkedText(MutableComponent target, String text, Component styledSource,
                                         String marker, String replacement, Component hover) {
        int from = 0;
        int markerAt;
        while ((markerAt = text.indexOf(marker, from)) >= 0) {
            if (markerAt > from) {
                target.append(Component.literal(text.substring(from, markerAt)).withStyle(styledSource.getStyle()));
            }
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
            target.append(Component.literal(replacement).withStyle(styledSource.getStyle())
                    .withStyle(style -> style.withHoverEvent(hoverEvent)));
            from = markerAt + marker.length();
        }
        if (from < text.length()) {
            target.append(Component.literal(text.substring(from)).withStyle(styledSource.getStyle()));
        }
    }
}
