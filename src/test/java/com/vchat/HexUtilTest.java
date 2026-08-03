package com.vchat;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HexUtilTest {
    @Test
    void appliesTrueRgbAndResetsDecorationsOnColor() {
        Component component = HexUtil.fromLegacy("&#12AB34Hex &lBold &fPlain");

        assertEquals("Hex Bold Plain", component.getString());
        assertEquals(0x12AB34, component.getSiblings().get(0).getStyle().getColor().getValue());
        assertTrue(component.getSiblings().get(1).getStyle().isBold());
        assertFalse(component.getSiblings().get(2).getStyle().isBold());
    }
}
