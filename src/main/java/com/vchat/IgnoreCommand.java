package com.vchat;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IgnoreCommand {
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ignore")
                .executes(ctx -> {
                    var owner = ctx.getSource().getPlayerOrException();
                    if (!VChatTabConfig.ignoreEnabled()) {
                        owner.sendSystemMessage(HexUtil.fromLegacy(VChatTabConfig.ignoreDisabledMessage()));
                        return 0;
                    }
                    String message = VChatTabConfig.ignoreUsageMessage().replace("<count>",
                            String.valueOf(IgnoreManager.ignoredCount(owner.getUUID())));
                    owner.sendSystemMessage(HexUtil.fromLegacy(message));
                    return 1;
                })
                .then(Commands.literal("clear")
                        .executes(ctx -> {
                            var owner = ctx.getSource().getPlayerOrException();
                            if (!VChatTabConfig.ignoreEnabled()) {
                                owner.sendSystemMessage(HexUtil.fromLegacy(
                                        VChatTabConfig.ignoreDisabledMessage()));
                                return 0;
                            }
                            int count = IgnoreManager.clear(owner.getUUID());
                            owner.sendSystemMessage(HexUtil.fromLegacy(
                                    VChatTabConfig.ignoreClearedMessage()
                                            .replace("<count>", String.valueOf(count))));
                            return 1;
                        }))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            var source = ctx.getSource();
                            var owner = source.getPlayerOrException();
                            if (!VChatTabConfig.ignoreEnabled()) {
                                owner.sendSystemMessage(HexUtil.fromLegacy(VChatTabConfig.ignoreDisabledMessage()));
                                return 0;
                            }

                            var target = EntityArgument.getPlayer(ctx, "player");
                            if (owner.getUUID().equals(target.getUUID())) {
                                owner.sendSystemMessage(HexUtil.fromLegacy(
                                        VChatTabConfig.cannotIgnoreSelfMessage()));
                                return 0;
                            }

                            boolean ignored = IgnoreManager.toggle(owner.getUUID(), target.getUUID());
                            String template = ignored ? VChatTabConfig.ignoreAddedMessage()
                                    : VChatTabConfig.ignoreRemovedMessage();
                            owner.sendSystemMessage(HexUtil.fromLegacy(
                                    template.replace("<name>", target.getScoreboardName())));
                            return 1;
                        })));
    }
}
