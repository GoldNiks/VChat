package com.vchat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormattingSanitizerTest {
    @Test
    void keepsOrdinaryHexLookingHashtagWithoutPermission() {
        assertEquals("цвет #abcdef", FormattingSanitizer.filter(
                "цвет #abcdef", false, false, false, false));
    }

    @Test
    void stripsExplicitUnauthorizedFormatting() {
        assertEquals("text", FormattingSanitizer.filter(
                "&#12AB34&ltext", false, false, false, false));
    }

    @Test
    void canonicalizesAllowedHex() {
        assertEquals("&#12AB34text", FormattingSanitizer.filter(
                "#12ab34text", false, true, false, false));
    }

    @Test
    void stripsMentionAndHexFormattingForDiscord() {
        assertEquals("@miitava, привет", FormattingSanitizer.stripFormatting(
                "&e&l@miitava&r&f, &#12AB34привет"));
        assertEquals("текст", FormattingSanitizer.stripFormatting(
                "&%23FF0000текст"));
    }
}
