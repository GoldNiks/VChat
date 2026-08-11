package com.vchat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatInputNormalizerTest {
    @Test
    void bangWithAndWithoutSpaceProducesSameMessage() {
        assertEquals("123", ChatInputNormalizer.globalMessage("!123"));
        assertEquals("123", ChatInputNormalizer.globalMessage("! 123"));
        assertEquals("123", ChatInputNormalizer.globalMessage("!   123"));
    }

    @Test
    void doesNotTrimOrdinaryLocalChat() {
        assertEquals("  local", ChatInputNormalizer.globalMessage("  local"));
    }
}
