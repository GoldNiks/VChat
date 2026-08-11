package com.vchat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Minecraft <-> Discord relay: chat webhooks, server status, join/leave
 * notifications and the Discord-to-game bot. Replaces the separate
 * MC Chat Link mod.
 */
public class DiscordBridge {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("VChat");
    private static MinecraftServer server;
    private static DiscordBot bot;

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        server = event.getServer();
        if (VChatTabConfig.discordRelayServerStatus()) {
            DiscordWebhook.send(VChatTabConfig.discordStatusWebhookUrl(),
                    VChatTabConfig.discordServerStartedFormat()
                            .replace("{server}", VChatTabConfig.discordServerName()),
                    VChatTabConfig.discordWebhookUsername(),
                    VChatTabConfig.discordWebhookAvatarUrl());
        }
        startBot();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        stopBot();
        if (VChatTabConfig.discordRelayServerStatus()) {
            DiscordWebhook.sendSync(VChatTabConfig.discordStatusWebhookUrl(),
                    VChatTabConfig.discordServerStoppedFormat()
                            .replace("{server}", VChatTabConfig.discordServerName()),
                    VChatTabConfig.discordWebhookUsername(),
                    VChatTabConfig.discordWebhookAvatarUrl());
        }
        server = null;
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            relayPlayerEvent(VChatTabConfig.discordJoinFormat(), player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            relayPlayerEvent(VChatTabConfig.discordLeaveFormat(), player);
        }
    }

    /** Relays a successfully sent global chat message to Discord. */
    public static void relayGlobalChat(ServerPlayer player, String message) {
        if (!VChatTabConfig.discordRelayChatToDiscord()) return;
        String plainMessage = FormattingSanitizer.stripFormatting(message).stripLeading();
        String text = VChatTabConfig.discordGameToDiscordFormat()
                .replace("{player}", player.getGameProfile().getName())
                .replace("{message}", plainMessage)
                .replace("{server}", VChatTabConfig.discordServerName());
        DiscordWebhook.send(VChatTabConfig.discordChatWebhookUrl(), text,
                VChatTabConfig.discordWebhookUsername(),
                VChatTabConfig.discordWebhookAvatarUrl());
    }

    /** Restarts the bot so config changes take effect. Call after /vchat reload. */
    public static void reload() {
        stopBot();
        startBot();
    }

    private static void relayPlayerEvent(String format, ServerPlayer player) {
        if (!VChatTabConfig.discordRelayChatToDiscord()) return;
        String text = format.replace("{player}", player.getGameProfile().getName());
        DiscordWebhook.send(VChatTabConfig.discordChatWebhookUrl(), text,
                VChatTabConfig.discordWebhookUsername(),
                VChatTabConfig.discordWebhookAvatarUrl());
    }

    private static void startBot() {
        if (server == null) return;
        if (!VChatTabConfig.discordBotEnabled()) return;
        String token = VChatTabConfig.discordBotToken();
        long channelId = VChatTabConfig.discordBotChannelId();
        if (token == null || token.isBlank() || channelId <= 0) {
            LOGGER.warn("Discord bot: enabled but token/channel not configured");
            return;
        }
        stopBot();
        bot = new DiscordBot(token, channelId, server);
        bot.start();
    }

    private static void stopBot() {
        if (bot != null) {
            bot.stop();
            bot = null;
        }
    }
}
