package com.vchat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;

public final class DeathMessageFormatter {
    private static final String ZERO_WIDTH_SEPARATOR = "\u200B";

    private DeathMessageFormatter() {
    }

    public static Component hidePlayerName(Component source, String playerName) {
        if (source == null || playerName == null || playerName.isEmpty()) return source;
        String maskedName = playerName.substring(0, 1) + ZERO_WIDTH_SEPARATOR + playerName.substring(1);
        return transform(source, playerName, maskedName);
    }

    private static MutableComponent transform(Component source, String playerName, String maskedName) {
        ComponentContents contents = source.getContents();
        MutableComponent result;

        if (contents instanceof LiteralContents literal) {
            result = Component.literal(literal.text().replace(playerName, maskedName));
        } else if (contents instanceof TranslatableContents translatable) {
            Object[] args = translatable.getArgs().clone();
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Component component) {
                    args[i] = transform(component, playerName, maskedName);
                } else if (args[i] instanceof String text) {
                    args[i] = text.replace(playerName, maskedName);
                }
            }
            result = Component.translatableWithFallback(
                    translatable.getKey(), translatable.getFallback(), args);
        } else {
            result = MutableComponent.create(contents);
        }

        result.withStyle(source.getStyle());
        for (Component sibling : source.getSiblings()) {
            result.append(transform(sibling, playerName, maskedName));
        }
        return result;
    }
}
