package com.vchat;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscordBotTest {
    @Test
    void fallsBackToUsernameWhenGlobalNameIsNull() {
        JsonObject author = new JsonObject();
        author.add("global_name", null);
        author.addProperty("username", "sintez");

        assertEquals("sintez", DiscordBot.resolveUsername(author));
    }

    @Test
    void prefersNonBlankGlobalName() {
        JsonObject author = new JsonObject();
        author.addProperty("global_name", "Sintez");
        author.addProperty("username", "sintez");

        assertEquals("Sintez", DiscordBot.resolveUsername(author));
    }
}
