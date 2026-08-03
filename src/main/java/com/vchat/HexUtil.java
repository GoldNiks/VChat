package com.vchat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class HexUtil {
    public static Component fromLegacy(String input) {
        if (input == null) return Component.literal("");

        MutableComponent result = Component.empty();
        StringBuilder current = new StringBuilder();
        Style style = Style.EMPTY;

        for (int i = 0; i < input.length();) {
            int hexLength = hexTokenLength(input, i);
            if (hexLength > 0) {
                append(result, current, style);
                int rgb = Integer.parseInt(input.substring(i + hexLength - 6, i + hexLength), 16);
                style = Style.EMPTY.withColor(TextColor.fromRgb(rgb));
                i += hexLength;
                continue;
            }

            char c = input.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < input.length()
                    && isFormattingCode(input.charAt(i + 1))) {
                append(result, current, style);
                char code = Character.toLowerCase(input.charAt(i + 1));
                style = applyFormat(style, code);
                i += 2;
            } else {
                current.append(c);
                i++;
            }
        }
        append(result, current, style);
        return result;
    }

    private static void append(MutableComponent result, StringBuilder text, Style style) {
        if (text.length() == 0) return;
        result.append(Component.literal(text.toString()).withStyle(style));
        text.setLength(0);
    }

    private static int hexTokenLength(String input, int index) {
        if (input.startsWith("&%23", index) && hasHexDigits(input, index + 4)) return 10;
        if (input.startsWith("&#", index) && hasHexDigits(input, index + 2)) return 8;
        if (input.charAt(index) == '#' && hasHexDigits(input, index + 1)) return 7;
        return 0;
    }

    private static boolean hasHexDigits(String input, int start) {
        if (start + 6 > input.length()) return false;
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
            case '0' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.BLACK);
            case '1' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.DARK_BLUE);
            case '2' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.DARK_GREEN);
            case '3' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.DARK_AQUA);
            case '4' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.DARK_RED);
            case '5' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.DARK_PURPLE);
            case '6' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.GOLD);
            case '7' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.GRAY);
            case '8' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.DARK_GRAY);
            case '9' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.BLUE);
            case 'a' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.GREEN);
            case 'b' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.AQUA);
            case 'c' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.RED);
            case 'd' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.LIGHT_PURPLE);
            case 'e' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.YELLOW);
            case 'f' -> Style.EMPTY.withColor(net.minecraft.ChatFormatting.WHITE);
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
