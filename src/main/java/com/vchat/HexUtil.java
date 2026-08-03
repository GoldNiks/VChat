package com.vchat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("(?:&%23|&#|#)([0-9a-fA-F]{6})");
    private static final Pattern AMP_PATTERN = Pattern.compile("&([0-9a-fA-Fk-oK-OrR])");
    public static Component fromLegacy(String input) {
        if (input == null) return Component.literal("");

        String processed = input;
        Matcher m = HEX_PATTERN.matcher(processed);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String hex = m.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        m.appendTail(sb);
        processed = sb.toString();
        processed = AMP_PATTERN.matcher(processed).replaceAll("§$1");

        Component result = Component.literal("");
        StringBuilder current = new StringBuilder();
        Style style = Style.EMPTY;

        for (int i = 0; i < processed.length(); i++) {
            char c = processed.charAt(i);
            if (c == '§' && i + 1 < processed.length()) {
                if (current.length() > 0) {
                    result = result.copy().append(Component.literal(current.toString()).withStyle(style));
                    current = new StringBuilder();
                }
                char code = Character.toLowerCase(processed.charAt(i + 1));
                style = applyFormat(style, code);
                i++;
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            result = result.copy().append(Component.literal(current.toString()).withStyle(style));
        }
        return result;
    }

    private static Style applyFormat(Style style, char code) {
        return switch (code) {
            case '0' -> style.withColor(net.minecraft.ChatFormatting.BLACK);
            case '1' -> style.withColor(net.minecraft.ChatFormatting.DARK_BLUE);
            case '2' -> style.withColor(net.minecraft.ChatFormatting.DARK_GREEN);
            case '3' -> style.withColor(net.minecraft.ChatFormatting.DARK_AQUA);
            case '4' -> style.withColor(net.minecraft.ChatFormatting.DARK_RED);
            case '5' -> style.withColor(net.minecraft.ChatFormatting.DARK_PURPLE);
            case '6' -> style.withColor(net.minecraft.ChatFormatting.GOLD);
            case '7' -> style.withColor(net.minecraft.ChatFormatting.GRAY);
            case '8' -> style.withColor(net.minecraft.ChatFormatting.DARK_GRAY);
            case '9' -> style.withColor(net.minecraft.ChatFormatting.BLUE);
            case 'a' -> style.withColor(net.minecraft.ChatFormatting.GREEN);
            case 'b' -> style.withColor(net.minecraft.ChatFormatting.AQUA);
            case 'c' -> style.withColor(net.minecraft.ChatFormatting.RED);
            case 'd' -> style.withColor(net.minecraft.ChatFormatting.LIGHT_PURPLE);
            case 'e' -> style.withColor(net.minecraft.ChatFormatting.YELLOW);
            case 'f' -> style.withColor(net.minecraft.ChatFormatting.WHITE);
            case 'k' -> style.withObfuscated(true);
            case 'l' -> style.withBold(true);
            case 'm' -> style.withStrikethrough(true);
            case 'n' -> style.withUnderlined(true);
            case 'o' -> style.withItalic(true);
            case 'r' -> Style.EMPTY;
            default -> style;
        };
    }

}
