package com.vchat;

/** Normalizes chat shortcuts before anti-spam and formatting are applied. */
public final class ChatInputNormalizer {
    private ChatInputNormalizer() {
    }

    public static String globalMessage(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        if (raw.charAt(0) != '!') return raw;
        return raw.substring(1).stripLeading();
    }
}
