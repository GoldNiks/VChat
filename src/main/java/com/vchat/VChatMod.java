package com.vchat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(VChatMod.MODID)
public class VChatMod {
    public static final String MODID = "vchat";

    public VChatMod() {
        VChatTabConfig.initialize(FMLPaths.CONFIGDIR.get());
        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
        MinecraftForge.EVENT_BUS.register(new TabListHandler());
        MinecraftForge.EVENT_BUS.register(new DeathMessageHandler());
        MinecraftForge.EVENT_BUS.register(new GLReloadCommand());
        MinecraftForge.EVENT_BUS.register(new IgnoreCommand());
        MinecraftForge.EVENT_BUS.register(new DiscordBridge());
        MinecraftForge.EVENT_BUS.register(new AnnouncementManager());
    }
}
