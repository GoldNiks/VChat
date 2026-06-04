package com.vchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VChatTabConfig {
    public String header = "\n&6&l&nVChat\n\n&7Игроки: &a%online%\n\n&7&m-----------------";
    public String footer = "&7&m-----------------\n\n&7Баланс: &e0";
    public String joinMessage = "&aДобро пожаловать на &6&l&nVChat&a!";

    public int localChatRadius = 100;
    public boolean enableGlobalChat = true;
    public boolean enableLocalChat = true;
    public String globalCommand = "g";
    public String globalChatFormat = "&e[G] &7<name>: &f<message>";
    public String localChatFormat = "&7[L] &7<name>: &f<message>";
    public boolean mentionNoOneHeard = true;
    public String noOneHeardMessage = "&7Вас никто не услышал";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static VChatTabConfig instance = null;
    private static Path configDir = Path.of("config");

    public static String header() { ensure(); return instance.header; }
    public static String footer() { ensure(); return instance.footer; }
    public static String joinMessage() { ensure(); return instance.joinMessage; }
    public static int localChatRadius() { ensure(); return instance.localChatRadius; }
    public static boolean enableGlobalChat() { ensure(); return instance.enableGlobalChat; }
    public static boolean enableLocalChat() { ensure(); return instance.enableLocalChat; }
    public static String globalCommand() { ensure(); return instance.globalCommand; }
    public static String globalChatFormat() { ensure(); return instance.globalChatFormat; }
    public static String localChatFormat() { ensure(); return instance.localChatFormat; }
    public static boolean mentionNoOneHeard() { ensure(); return instance.mentionNoOneHeard; }
    public static String noOneHeardMessage() { ensure(); return instance.noOneHeardMessage; }

    private static void ensure() {
        if (instance == null) reload(configDir);
    }

    public static void reload(Path dir) {
        configDir = dir;
        Path file = dir.resolve("vchat-tab.json");
        if (Files.exists(file)) {
            try {
                instance = GSON.fromJson(Files.newBufferedReader(file), VChatTabConfig.class);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        instance = new VChatTabConfig();
        try {
            Files.createDirectories(dir);
            Files.writeString(file, GSON.toJson(instance));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
