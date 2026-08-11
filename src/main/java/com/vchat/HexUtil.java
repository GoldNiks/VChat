package com.vchat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class HexUtil {

    public static Component fromLegacy(String input) {
        MutableComponent result = Component.empty();
        appendLegacy(result, input, 0, input == null ? 0 : input.length(), Style.EMPTY);
        return result;
    }

    /**
     * Parses legacy color/format codes (and HEX) from {@code input} into
     * {@code target}, continuing the formatting carried by {@code start}.
     * Returns the style in effect after the last parsed token, so callers can
     * keep the formatting state across independently parsed fragments.
     */
    public static Style appendLegacy(MutableComponent target, String input, Style start) {
        return appendLegacy(target, input, 0, input.length(), start);
    }

    public static Style appendLegacy(MutableComponent target, String input, int from, int to, Style start) {
        if (input == null || from >= to) return start;
        StringBuilder current = new StringBuilder();
        Style style = start;
        int i = from;
        while (i < to) {
            int hexLength = hexTokenLength(input, i, to);
            if (hexLength > 0) {
                append(target, current, style);
                int rgb = Integer.parseInt(input.substring(i + hexLength - 6, i + hexLength), 16);
                style = Style.EMPTY.withColor(TextColor.fromRgb(rgb));
                i += hexLength;
                continue;
            }

            char c = input.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < to
                    && isFormattingCode(input.charAt(i + 1))) {
                append(target, current, style);
                char code = Character.toLowerCase(input.charAt(i + 1));
                style = applyFormat(style, code);
                i += 2;
            } else {
                current.append(c);
                i++;
            }
        }
        append(target, current, style);
        return style;
    }

    private static void append(MutableComponent result, StringBuilder text, Style style) {
        if (text.length() == 0) return;
        result.append(Component.literal(text.toString()).withStyle(style));
        text.setLength(0);
    }

    private static int hexTokenLength(String input, int index, int limit) {
        if (input.startsWith("&%23", index) && hasHexDigits(input, index + 4, limit)) return 10;
        if (input.startsWith("&#", index) && hasHexDigits(input, index + 2, limit)) return 8;
        if (input.charAt(index) == '#' && hasHexDigits(input, index + 1, limit)) return 7;
        return 0;
    }

    private static boolean hasHexDigits(String input, int start, int limit) {
        if (start + 6 > limit || start + 6 > input.length()) return false;
        for (int i = start; i < start + 6; i++) {
            if (Character.digit(input.charAt(i), 16) < 0) return false;
        }
        return true;
    }

    private static boolean isFormattingCode(char rawCode) {
        char code = Character.toLowerCase(rawCode);
        return "0123456789abcdefklmnor".indexOf(code) >= 0;
    }

    private static Style applyFormat(Style style, char code) {
        return switch (code) {
            case '0' -> Style.EMPTY.withColor(ChatFormatting.BLACK);
            case '1' -> Style.EMPTY.withColor(ChatFormatting.DARK_BLUE);
            case '2' -> Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);
            case '3' -> Style.EMPTY.withColor(ChatFormatting.DARK_AQUA);
            case '4' -> Style.EMPTY.withColor(ChatFormatting.DARK_RED);
            case '5' -> Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE);
            case '6' -> Style.EMPTY.withColor(ChatFormatting.GOLD);
            case '7' -> Style.EMPTY.withColor(ChatFormatting.GRAY);
            case '8' -> Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
            case '9' -> Style.EMPTY.withColor(ChatFormatting.BLUE);
            case 'a' -> Style.EMPTY.withColor(ChatFormatting.GREEN);
            case 'b' -> Style.EMPTY.withColor(ChatFormatting.AQUA);
            case 'c' -> Style.EMPTY.withColor(ChatFormatting.RED);
            case 'd' -> Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE);
            case 'e' -> Style.EMPTY.withColor(ChatFormatting.YELLOW);
            case 'f' -> Style.EMPTY.withColor(ChatFormatting.WHITE);
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
