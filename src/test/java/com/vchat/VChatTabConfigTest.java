package com.vchat;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VChatTabConfigTest {
    @Test
    void migratesDiscordSettingsFromMcChatLinkToml() throws Exception {
        Path directory = Files.createTempDirectory("vchat-mcchatlink-test-");
        Path toml = directory.resolve("mcchatlink-server.toml");
        Files.writeString(toml, """
                [discord]
                \tchatWebhookUrl = "https://discord.com/api/webhooks/abc"
                \tstatusWebhookUrl = "https://discord.com/api/webhooks/def"
                \tserverName = "ValorCraft (TFG)"
                \trelayServerStatus = true
                \twebhookUsername = "ValorCraft"
                \twebhookAvatarUrl = "https://example.com/avatar.png"
                \trelayChatToDiscord = true
                \tgameToDiscordFormat = "**{player}**: {message}"

                [bot]
                \tbotEnabled = true
                \tbotToken = "test-token"
                \tbotChannelId = 1510057193376976906
                \trelayDiscordToGame = true
                \tdiscordToGameFormat = "&8[Discord] &7{username}&8: &f{message}"
                """, StandardCharsets.UTF_8);

        assertTrue(VChatTabConfig.reload(directory));
        assertEquals("https://discord.com/api/webhooks/abc", VChatTabConfig.discordChatWebhookUrl());
        assertEquals("https://discord.com/api/webhooks/def", VChatTabConfig.discordStatusWebhookUrl());
        assertEquals("ValorCraft (TFG)", VChatTabConfig.discordServerName());
        assertEquals("test-token", VChatTabConfig.discordBotToken());
        assertEquals(1510057193376976906L, VChatTabConfig.discordBotChannelId());
        assertTrue(VChatTabConfig.discordRelayChatToDiscord());
        assertTrue(VChatTabConfig.discordBotEnabled());
        assertTrue(VChatTabConfig.discordRelayDiscordToGame());
        assertEquals("&8[Discord] &7{username}&8: &f{message}", VChatTabConfig.discordToGameFormat());
    }

    @Test
    void rejectsBrokenReloadAndKeepsLastWorkingConfig() throws Exception {
        Path directory = Files.createTempDirectory("vchat-config-test-");
        assertTrue(VChatTabConfig.reload(directory));
        assertEquals(500, VChatTabConfig.cooldownMillis());

        Path config = directory.resolve("vchat-config.json5");
        String generated = Files.readString(config);
        assertTrue(generated.contains("\"configVersion\": 18"));
        assertTrue(generated.contains("\"playerUsernameFormat\": \"🎮 {player} | Minecraft\""));
        assertTrue(generated.contains("\"useValorCraftSkinsAvatar\": true"));
        assertTrue(generated.contains("\"detectionMode\": \"started\""));
        assertTrue(generated.contains("\"quests\": []"));
        assertTrue(generated.contains("ВАШ_ID_ИЗ_SNBT"));
        assertTrue(generated.contains("questsmetallurgy"));
        assertTrue(generated.contains("\"saveIntervalMillis\": 1000"));
        assertTrue(generated.contains("\"chapters\""));
        assertTrue(generated.contains("\"discord\""));
        assertTrue(generated.contains("\"announcements\""));
        assertTrue(Files.exists(directory.resolve("vchat-config.json5.last-good")));

        String versionSeven = generated
                .replace("\"configVersion\": 18", "\"configVersion\": 7")
                .replace("\"cooldownMillis\": 500", "\"cooldownMillis\": 1000");
        Files.writeString(config, versionSeven);
        assertTrue(VChatTabConfig.reload(directory));
        assertEquals(500, VChatTabConfig.cooldownMillis());

        Files.writeString(config, "{ broken json");
        assertFalse(VChatTabConfig.reload(directory));
        assertEquals(500, VChatTabConfig.cooldownMillis());
    }

    @Test
    void versionUpgradeDoesNotOverwriteDiscordWithOldMcChatLinkConfig() throws Exception {
        Path directory = Files.createTempDirectory("vchat-discord-upgrade-test-");
        assertTrue(VChatTabConfig.reload(directory));
        Path config = directory.resolve("vchat-config.json5");
        String generated = Files.readString(config)
                .replace("\"configVersion\": 18", "\"configVersion\": 14")
                .replace("\"chatWebhookUrl\": \"\"",
                        "\"chatWebhookUrl\": \"https://discord.com/api/webhooks/current\"");
        Files.writeString(config, generated);
        Files.writeString(directory.resolve("mcchatlink-server.toml"), """
                [discord]
                chatWebhookUrl = "https://discord.com/api/webhooks/old"
                relayChatToDiscord = false
                """);

        assertTrue(VChatTabConfig.reload(directory));
        assertEquals("https://discord.com/api/webhooks/current", VChatTabConfig.discordChatWebhookUrl());
        assertTrue(VChatTabConfig.discordRelayChatToDiscord());
        assertTrue(VChatTabConfig.discordUsePlayerIdentity());
        assertTrue(VChatTabConfig.discordUseValorCraftSkinsAvatar());
        assertEquals("🎮 {player} | Minecraft", VChatTabConfig.discordPlayerUsernameFormat());
        assertEquals("{message}", VChatTabConfig.discordGameToDiscordFormat());
    }
}
