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

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static VChatTabConfig instance = null;
    private static Path configDir = Path.of("config");

    public static String header() { ensure(); return instance.header; }
    public static String footer() { ensure(); return instance.footer; }
    public static String joinMessage() { ensure(); return instance.joinMessage; }

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
