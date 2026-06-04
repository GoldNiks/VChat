package com.vchat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatEventHandler {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("VChat");

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        var source = event.getParseResults().getContext().getSource();
        if (source.getEntity() instanceof ServerPlayer player) {
            String cmd = event.getParseResults().getReader().getString();
            LOGGER.info("<{}> /{}", player.getScoreboardName(), cmd);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("g")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            CommandSourceStack src = ctx.getSource();
                            ServerPlayer player = src.getPlayerOrException();
                            String msg = StringArgumentType.getString(ctx, "message");
                            broadcastGlobal(player, msg);
                            return 1;
                        })
                )
        );
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        event.setCanceled(true);
        ServerPlayer player = event.getPlayer();
        String raw = event.getRawText();

        if (raw.startsWith("!")) {
            broadcastGlobal(player, raw.substring(1));
        } else {
            broadcastLocal(player, raw);
        }
    }

    private Component name(ServerPlayer player) {
        return player.getDisplayName().copy();
    }

    private void broadcastGlobal(ServerPlayer sender, String message) {
        Component text = Component.literal("[G] ").withStyle(ChatFormatting.YELLOW)
                .append(name(sender))
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(HexUtil.fromLegacy(message));

        for (ServerPlayer p : sender.getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(text);
        }
        LOGGER.info(text.getString());
    }

    private void broadcastLocal(ServerPlayer sender, String message) {
        Component text = Component.literal("[L] ").withStyle(ChatFormatting.GRAY)
                .append(name(sender))
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(HexUtil.fromLegacy(message));

        boolean heard = false;
        for (ServerPlayer p : sender.getServer().getPlayerList().getPlayers()) {
            if (p == sender) continue;
            if (p.distanceTo(sender) <= 100.0) {
                p.sendSystemMessage(text);
                heard = true;
            }
        }

        if (!heard) {
            sender.sendSystemMessage(Component.literal("\u0412\u0430\u0441 \u043D\u0438\u043A\u0442\u043E \u043D\u0435 \u0443\u0441\u043B\u044B\u0448\u0430\u043B"));
        }
        sender.sendSystemMessage(text);
        LOGGER.info(text.getString());
    }
}
