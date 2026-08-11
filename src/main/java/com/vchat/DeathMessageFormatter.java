package com.vchat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;

public final class DeathMessageFormatter {
    // A legacy formatting reset is consumed by Minecraft's text renderer and
    // therefore has no glyph. Keeping it in a separate component also keeps
    // the original style on the following part of the player name.
    private static final String INVISIBLE_NAME_BREAK = "\u00A7r";

    private DeathMessageFormatter() {
    }

    public static Component hidePlayerName(Component source, String playerName) {
        if (source == null || playerName == null || playerName.isEmpty()) return source;
        return transform(source, playerName);
    }

    private static MutableComponent transform(Component source, String playerName) {
        ComponentContents contents = source.getContents();
        MutableComponent result;

        if (contents instanceof LiteralContents literal) {
            result = maskLiteral(literal.text(), playerName);
        } else if (contents instanceof TranslatableContents translatable) {
            Object[] args = translatable.getArgs().clone();
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Component component) {
                    args[i] = transform(component, playerName);
                } else if (args[i] instanceof String text) {
                    args[i] = maskedString(text, playerName);
                }
            }
            result = Component.translatableWithFallback(
                    translatable.getKey(), translatable.getFallback(), args);
        } else {
            result = MutableComponent.create(contents);
        }

        result.withStyle(source.getStyle());
        for (Component sibling : source.getSiblings()) {
            result.append(transform(sibling, playerName));
        }
        return result;
    }

    private static MutableComponent maskLiteral(String text, String playerName) {
        MutableComponent result = Component.empty();
        int from = 0;
        int match;
        while ((match = text.indexOf(playerName, from)) >= 0) {
            result.append(Component.literal(text.substring(from, match + 1)));
            result.append(Component.literal(INVISIBLE_NAME_BREAK));
            from = match + 1;
        }
        return result.append(Component.literal(text.substring(from)));
    }

    private static String maskedString(String text, String playerName) {
        return text.replace(playerName,
                playerName.substring(0, 1) + INVISIBLE_NAME_BREAK + playerName.substring(1));
    }
}
