package com.vchat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(VChatMod.MODID)
public class VChatMod {
    public static final String MODID = "vchat";

    public VChatMod() {
        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
        MinecraftForge.EVENT_BUS.register(new TabListHandler());
        MinecraftForge.EVENT_BUS.register(new GLReloadCommand());
        MinecraftForge.EVENT_BUS.register(new IgnoreCommand());
    }
}
