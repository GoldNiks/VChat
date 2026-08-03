package com.vchat;

import net.minecraft.commands.Commands;
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
                            ctx.getSource().sendSuccess(() -> Component.literal("§7TAB refresh ticks: §f"
                                    + VChatTabConfig.tabUpdateIntervalTicks()), false);
                            return 1;
                        })
                )
        );
    }
}
