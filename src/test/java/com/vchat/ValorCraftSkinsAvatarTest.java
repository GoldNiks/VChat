package com.vchat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValorCraftSkinsAvatarTest {
    @Test
    void extractsPreparedHeadUrl() {
        assertEquals("https://valorcraft.ru/data/skins/test_head.png?t=1",
                ValorCraftSkinsAvatar.extractHeadUrl("""
                        {"head":{"url":"https://valorcraft.ru/data/skins/test_head.png?t=1"}}
                        """));
        assertEquals("", ValorCraftSkinsAvatar.extractHeadUrl("{\"head\":null}"));
    }
}
