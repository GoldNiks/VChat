package com.vchat;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GLReloadCommand {
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("vchat")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            var dir = VChatPaths.prepareConfigDirectory();
                            boolean reloaded = VChatTabConfig.reload(dir);
                            if (!reloaded) {
                                ctx.getSource().sendFailure(Component.literal(
                                        "§cVChat config contains an error. Previous settings are still active; see server log."));
                                return 0;
                            }
                            var server = ctx.getSource().getServer();
                            TabListHandler.refreshAll(server, true);
                            DiscordBridge.reload();
                            AnnouncementManager.reset();
                            ctx.getSource().sendSuccess(() -> Component.literal("§aVChat config reloaded"), true);
                            return 1;
                        })
                )
                .then(Commands.literal("announce")
                        .then(Commands.argument("message", MessageArgument.message())
                                .executes(ctx -> {
                                    String text = MessageArgument.getMessage(ctx, "message").getString();
                                    AnnouncementManager.broadcast(ctx.getSource().getServer(), text);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "§aAnnouncement sent"), true);
                                    return 1;
                                }))
                )
                .then(Commands.literal("status")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Header: §f" + VChatTabConfig.header()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Config directory: §f"
                                    + VChatPaths.configDirectory().toAbsolutePath()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Footer: §f" + VChatTabConfig.footer()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7LuckPerms prefixes: §f"
                                    + VChatTabConfig.enableLuckPermsPrefixes()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7TAB sorting: §f"
                                    + VChatTabConfig.enableTabSorting()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7TAB sorting source: §fLuckPerms weight"), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7TAB player format: §f"
                                    + VChatTabConfig.tabPlayerFormat()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7TAB refresh ticks: §f"
                                    + VChatTabConfig.tabUpdateIntervalTicks()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Player formatting: §f"
                                    + VChatTabConfig.playerFormattingEnabled()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Anti-spam: §f"
                                    + VChatTabConfig.antiSpamEnabled() + " §7(max "
                                    + VChatTabConfig.maxMessageLength() + ", cooldown "
                                    + VChatTabConfig.cooldownMillis() + " ms)"), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Mentions / ignore: §f"
                                    + VChatTabConfig.mentionsEnabled() + " / "
                                    + VChatTabConfig.ignoreEnabled()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7FTB Teams name hover: §f"
                                    + VChatTabConfig.ftbTeamsHoverEnabled()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Death message heads hidden: §f"
                                    + VChatTabConfig.hidePlayerHeadsInDeathMessages()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Announcements: §f"
                                    + VChatTabConfig.announcementsEnabled() + " §7(every "
                                    + VChatTabConfig.announcementsIntervalSeconds() + " s, "
                                    + VChatTabConfig.announcementsMessages().size() + " messages)"), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Discord enabled / chat relay: §f"
                                    + VChatTabConfig.discordEnabled() + " / "
                                    + VChatTabConfig.discordRelayChatToDiscord()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Discord chat webhook configured: §f"
                                    + !VChatTabConfig.discordChatWebhookUrl().isBlank()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Discord bot (Discord -> game): §f"
                                    + DiscordBridge.botStatus()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Discord webhook avatar configured: §f"
                                    + !VChatTabConfig.discordWebhookAvatarUrl().isBlank()), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Command arguments in log: §f"
                                    + VChatTabConfig.includeCommandArguments()), false);
                            return 1;
                        })
                )
                .then(Commands.literal("debug")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    var target = EntityArgument.getPlayer(ctx, "player");
                                    var data = LuckPermsBridge.read(target);
                                    var formatting = ChatFormattingPolicy.capabilities(target);

                                    ctx.getSource().sendSuccess(() -> Component.literal("§6VChat debug: §f"
                                            + target.getScoreboardName()), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("§7Primary group: §f"
                                            + emptyAsDash(data.primaryGroup())), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("§7Weight: §f"
                                            + (data.groupWeight() == null ? "—" : data.groupWeight())), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("§7Prefix: §f")
                                            .append(HexUtil.fromLegacy(emptyAsDash(data.prefix()))), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("§7Suffix: §f")
                                            .append(HexUtil.fromLegacy(emptyAsDash(data.suffix()))), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("§7FTB Quests stage: §f")
                                            .append(HexUtil.fromLegacy(emptyAsDash(
                                                    FTBQuestsStageBridge.stageText(target))))
                                            .append(Component.literal(" §8(mode: "
                                                    + VChatTabConfig.stageDetectionMode() + ", source: "
                                                    + VChatTabConfig.stageSource() + ")")), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("§7TAB row: §f")
                                            .append(MessageFormatter.player(VChatTabConfig.tabPlayerFormat(),
                                                    target, data)), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("§7World: §f"
                                            + target.serverLevel().dimension().location()), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("§7Formatting: §fcolor="
                                            + formatting.colors() + ", hex=" + formatting.hex()
                                            + ", style=" + formatting.styles()
                                            + ", obfuscated=" + formatting.obfuscated()), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("§7Ignored players: §f"
                                            + IgnoreManager.ignoredCount(target.getUUID())), false);
                                    return 1;
                                }))
                )
                .then(Commands.literal("discord-test")
                        .executes(ctx -> {
                            if (!VChatTabConfig.discordEnabled()) {
                                ctx.getSource().sendFailure(Component.literal("§cDiscord integration is disabled"));
                                return 0;
                            }
                            if (VChatTabConfig.discordChatWebhookUrl().isBlank()) {
                                ctx.getSource().sendFailure(Component.literal("§cDiscord chat webhook URL is empty"));
                                return 0;
                            }
                            DiscordWebhook.send(VChatTabConfig.discordChatWebhookUrl(),
                                    "✅ VChat webhook test | " + VChatTabConfig.discordServerName(),
                                    VChatTabConfig.discordWebhookUsername(),
                                    VChatTabConfig.discordWebhookAvatarUrl());
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§aDiscord webhook test queued; check Discord and server log"), false);
                            return 1;
                        }))
        );
    }

    private static String emptyAsDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
