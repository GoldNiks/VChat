package com.vchat;

import java.util.Locale;

public final class FormattingSanitizer {
    private FormattingSanitizer() {
    }

    public static String filter(String message, boolean colors, boolean hex,
                                boolean styles, boolean obfuscated) {
        if (message == null || message.isEmpty()) return "";

        StringBuilder result = new StringBuilder(message.length());
        for (int i = 0; i < message.length();) {
            int hexLength = hexTokenLength(message, i);
            if (hexLength > 0) {
                if (hex) {
                    String digits = message.substring(i + hexLength - 6, i + hexLength);
                    result.append("&#").append(digits.toUpperCase(Locale.ROOT));
                } else if (message.charAt(i) == '#') {
                    // A normal hashtag such as #abcdef must remain readable for
                    // players who do not have HEX formatting permission.
                    result.append(message, i, i + hexLength);
                }
                i += hexLength;
                continue;
            }

            char marker = message.charAt(i);
            if ((marker == '&' || marker == '§') && i + 1 < message.length()) {
                char code = Character.toLowerCase(message.charAt(i + 1));
                boolean allowed = isColor(code) ? colors
                        : code == 'k' ? obfuscated
                        : isStyle(code) && styles;
                if (allowed) result.append('&').append(code);
                if (isFormattingCode(code)) {
                    i += 2;
                    continue;
                }
            }

            result.append(marker);
            i++;
        }
        return result.toString();
    }

    /** Removes Minecraft legacy/HEX formatting while preserving visible text. */
    public static String stripFormatting(String message) {
        if (message == null || message.isEmpty()) return "";

        StringBuilder result = new StringBuilder(message.length());
        for (int i = 0; i < message.length();) {
            int hexLength = hexTokenLength(message, i);
            if (hexLength > 0) {
                i += hexLength;
                continue;
            }

            char marker = message.charAt(i);
            if ((marker == '&' || marker == '§') && i + 1 < message.length()
                    && isFormattingCode(Character.toLowerCase(message.charAt(i + 1)))) {
                i += 2;
                continue;
            }

            result.append(marker);
            i++;
        }
        return result.toString();
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

    private static boolean isColor(char code) {
        return "0123456789abcdef".indexOf(code) >= 0;
    }

    private static boolean isStyle(char code) {
        return "lmnor".indexOf(code) >= 0;
    }

    private static boolean isFormattingCode(char code) {
        return isColor(code) || code == 'k' || isStyle(code);
    }
}
