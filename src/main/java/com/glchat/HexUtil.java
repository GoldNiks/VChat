package com.vchat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("(?:&#|#|&%23)([0-9a-fA-F]{6})");

    public static Component resolveDisplayName(ServerPlayer player) {
        Team team = player.getTeam();
        String name = player.getName().getString();
        String prefix = "";
        String suffix = "";

        if (team instanceof PlayerTeam pt) {
            prefix = pt.getPlayerPrefix().getString();
            suffix = pt.getPlayerSuffix().getString();
        }

        return fromLegacy(prefix + name + suffix);
    }

    public static Component fromLegacy(String input) {
        String processed = convertCodes(input);
        MutableComponent result = Component.literal("");
        StringBuilder buf = new StringBuilder();
        Style style = Style.EMPTY;

        for (int i = 0; i < processed.length(); i++) {
            char c = processed.charAt(i);
            if (c == '\u00A7' && i + 1 < processed.length()) {
                if (!buf.isEmpty()) {
                    result.append(Component.literal(buf.toString()).withStyle(style));
                    buf = new StringBuilder();
                }
                char code = processed.charAt(i + 1);
                i++;
                if (code == 'x' && i + 12 < processed.length()) {
                    try {
                        StringBuilder hex = new StringBuilder("#");
                        for (int j = 0; j < 6; j++) {
                            int pos = i + 1 + j * 2;
                            if (pos + 1 < processed.length() && processed.charAt(pos) == '\u00A7') {
                                hex.append(processed.charAt(pos + 1));
                            }
                        }
                        TextColor color = TextColor.parseColor(hex.toString());
                        style = style.withColor(color);
                        i += 12;
                    } catch (Exception e) {
                        style = style.withColor(ChatFormatting.WHITE);
                    }
                } else {
                    if (code == 'r') {
                        style = Style.EMPTY;
                    } else {
                        ChatFormatting fmt = ChatFormatting.getByCode(code);
                        if (fmt != null) {
                            style = applyFormat(style, fmt);
                        }
                    }
                }
            } else {
                buf.append(c);
            }
        }
        if (!buf.isEmpty()) {
            result.append(Component.literal(buf.toString()).withStyle(style));
        }
        return result;
    }

    private static String convertCodes(String input) {
        if (input == null || input.isEmpty()) return input;
        String result = input.replace('&', '\u00A7');
        Matcher m = HEX_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String hex = m.group(1);
            StringBuilder replacement = new StringBuilder("\u00A7x");
            for (char ch : hex.toCharArray()) {
                replacement.append('\u00A7').append(ch);
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String getLpPrefix(ServerPlayer player) {
        try {
            Object lp = Class.forName("net.luckperms.api.LuckPermsProvider")
                .getMethod("get").invoke(null);
            Object um = lp.getClass().getMethod("getUserManager").invoke(lp);
            Object user = um.getClass().getMethod("getUser", UUID.class)
                .invoke(um, player.getUUID());
            if (user == null) return "";
            Object cd = user.getClass().getMethod("getCachedData").invoke(user);
            Object meta = cd.getClass().getMethod("getMetaData").invoke(cd);
            return (String) meta.getClass().getMethod("getPrefix").invoke(meta);
        } catch (Exception e) {
            return "";
        }
    }

    private static Style applyFormat(Style style, ChatFormatting fmt) {
        return switch (fmt) {
            case OBFUSCATED -> style.withObfuscated(true);
            case BOLD -> style.withBold(true);
            case STRIKETHROUGH -> style.withStrikethrough(true);
            case UNDERLINE -> style.withUnderlined(true);
            case ITALIC -> style.withItalic(true);
            default -> style.withColor(fmt);
        };
    }
}
