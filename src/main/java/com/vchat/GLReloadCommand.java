package com.vchat;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
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
                            var dir = ctx.getSource().getServer().getServerDirectory().toPath().resolve("config");
                            VChatTabConfig.reload(dir);
                            var server = ctx.getSource().getServer();
                            TabListHandler.refreshAll(server, true);
                            ctx.getSource().sendSuccess(() -> Component.literal("§aVChat config reloaded"), true);
                            return 1;
                        })
                )
                .then(Commands.literal("status")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal("§7Header: §f" + VChatTabConfig.header()), false);
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
        );
    }

    private static String emptyAsDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
