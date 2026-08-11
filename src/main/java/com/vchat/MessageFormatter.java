package com.vchat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageFormatter {
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
        HoverEvent hoverEvent = hover == null ? null
                : new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
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

        // One pass over the pattern: legacy formatting is parsed continuously
        // (color state survives across placeholders), placeholders are replaced
        // in place, and name/display_name pieces get the FTB Teams hover.
        return formatValues(pattern, values, hoverEvent);
    }

    static Component formatValues(String pattern, Map<String, String> values, HoverEvent hoverEvent) {
        MutableComponent result = Component.empty();
        String text = pattern == null ? "" : pattern;
        Style current = Style.EMPTY;
        Matcher matcher = PLACEHOLDER.matcher(text);
        int cursor = 0;
        while (matcher.find()) {
            current = HexUtil.appendLegacy(result, text, cursor, matcher.start(), current);
            cursor = matcher.end();

            String key = matcher.group(1);
            String replacement = values.get(key);
            if (replacement == null || replacement.isEmpty()) continue;

            int piecesBefore = result.getSiblings().size();
            // The replacement is always parsed like pattern text: this keeps all
            // HEX forms (#RRGGBB, &#RRGGBB, &%23RRGGBB) and &-codes working
            // inside LuckPerms prefixes/suffixes and other values.
            current = HexUtil.appendLegacy(result, replacement, current);
            if (hoverEvent != null && ("name".equals(key) || "display_name".equals(key))) {
                List<Component> siblings = result.getSiblings();
                for (int i = piecesBefore; i < siblings.size(); i++) {
                    int pieceIndex = i;
                    siblings.set(pieceIndex, siblings.get(pieceIndex).copy()
                            .withStyle(style -> style.withHoverEvent(hoverEvent)));
                }
            }
        }
        HexUtil.appendLegacy(result, text, cursor, text.length(), current);
        return result;
    }
}
