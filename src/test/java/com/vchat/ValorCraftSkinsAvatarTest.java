package com.vchat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValorCraftSkinsAvatarTest {
    @Test
    void extractsValorCraftSkinUrl() {
        assertEquals("https://valorcraft.ru/data/skins/test.png",
                ValorCraftSkinsAvatar.extractSkinUrl("""
                        {"skin":{"url":"https://valorcraft.ru/data/skins/test.png"}}
                        """));
        assertEquals("", ValorCraftSkinsAvatar.extractSkinUrl("{\"error\":\"not found\"}"));
    }

    @Test
    void safelyBuildsHeadProxyUrl() {
        String result = ValorCraftSkinsAvatar.buildHeadUrl(
                "https://images.example/?url={skinUrl}&name={player}",
                "https://valorcraft.ru/data/skins/a b.png", "Player_1");
        assertTrue(result.contains("url=https%3A%2F%2Fvalorcraft.ru%2Fdata%2Fskins%2Fa%20b.png"));
        assertTrue(result.endsWith("name=Player_1"));
    }
}
