package com.vchat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Locale;

public class ChatEventHandler {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("VChat");

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        if (!VChatTabConfig.logCommands()) return;
        var source = event.getParseResults().getContext().getSource();
        if (source.getEntity() instanceof ServerPlayer player) {
            String commandLine = event.getParseResults().getReader().getString().strip();
            String label = commandLine.split("\\s+", 2)[0].replaceFirst("^/", "")
                    .toLowerCase(Locale.ROOT);
            String bareLabel = label.substring(label.lastIndexOf(':') + 1);
            boolean hasArguments = commandLine.indexOf(' ') >= 0;
            boolean sensitive = VChatTabConfig.redactedCommands().stream()
                    .filter(command -> command != null)
                    .map(command -> command.strip().toLowerCase(Locale.ROOT))
                    .anyMatch(command -> command.equals(label) || command.equals(bareLabel));
            String logged = VChatTabConfig.includeCommandArguments() && !sensitive
                    ? commandLine
                    : label + (hasArguments ? " <arguments hidden>" : "");
            LOGGER.info("<{}> /{}", player.getScoreboardName(), logged);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        AntiSpamManager.clear(event.getEntity().getUUID());
        IgnoreManager.clearCooldown(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            IgnoreManager.flushIfDue();
            FirstJoinManager.flushIfDue();
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        IgnoreManager.flushNow();
        FirstJoinManager.flushNow();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(VChatTabConfig.globalCommand())
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
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

    private void broadcastGlobal(ServerPlayer sender, String message) {
        if (!VChatTabConfig.enableGlobalChat()) {
            sender.sendSystemMessage(HexUtil.fromLegacy(VChatTabConfig.globalDisabledMessage()));
            return;
        }
        if (!AntiSpamManager.allow(sender, message)) return;

        String safeMessage = ChatFormattingPolicy.filter(sender, message);
        MentionProcessor.Result mentions = MentionProcessor.process(sender.getServer(), safeMessage);
        Component text = MessageFormatter.chat(VChatTabConfig.globalChatFormat(), sender,
                mentions.message(), "global");

        for (ServerPlayer p : sender.getServer().getPlayerList().getPlayers()) {
            if (p != sender && IgnoreManager.isIgnoring(p.getUUID(), sender.getUUID())) continue;
            p.sendSystemMessage(text);
            if (p != sender && mentions.mentionedPlayers().contains(p.getUUID())) {
                MentionProcessor.notify(p);
            }
        }
        if (VChatTabConfig.logChatMessages()) LOGGER.info(text.getString());
        DiscordBridge.relayGlobalChat(sender, mentions.message());
    }

    private void broadcastLocal(ServerPlayer sender, String message) {
        if (!VChatTabConfig.enableLocalChat()) {
            sender.sendSystemMessage(HexUtil.fromLegacy(VChatTabConfig.localDisabledMessage()));
            return;
        }
        if (!AntiSpamManager.allow(sender, message)) return;

        String safeMessage = ChatFormattingPolicy.filter(sender, message);
        MentionProcessor.Result mentions = MentionProcessor.process(sender.getServer(), safeMessage);
        Component text = MessageFormatter.chat(VChatTabConfig.localChatFormat(), sender,
                mentions.message(), "local");
        int radius = VChatTabConfig.localChatRadius();
        boolean heard = false;

        for (ServerPlayer p : sender.getServer().getPlayerList().getPlayers()) {
            if (p == sender) continue;
            if (p.serverLevel() != sender.serverLevel()) continue;
            if (IgnoreManager.isIgnoring(p.getUUID(), sender.getUUID())) continue;
            if (p.distanceTo(sender) <= radius) {
                p.sendSystemMessage(text);
                heard = true;
                if (mentions.mentionedPlayers().contains(p.getUUID())) {
                    MentionProcessor.notify(p);
                }
            }
        }

        if (!heard && VChatTabConfig.mentionNoOneHeard()) {
            sender.sendSystemMessage(HexUtil.fromLegacy(VChatTabConfig.noOneHeardMessage()));
        }
        sender.sendSystemMessage(text);
        if (VChatTabConfig.logChatMessages()) LOGGER.info(text.getString());
    }
}
