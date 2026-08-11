package com.vchat;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeathMessageFormatterTest {
    @Test
    void breaksPlayerNameWithoutUnicodeZeroWidthGlyph() {
        Component result = DeathMessageFormatter.hidePlayerName(
                Component.literal("dragonexo был пронзён"), "dragonexo");

        assertEquals("d§rragonexo был пронзён", result.getString());
        assertFalse(result.getString().contains("\u200B"));
        assertTrue(result.getString().contains("§r"));
    }
}
