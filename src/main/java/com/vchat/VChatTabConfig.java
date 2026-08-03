package com.vchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

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

    public boolean enableLuckPermsPrefixes = true;
    public boolean enableTabSorting = true;
    public Map<String, Integer> tabGroupOrder = new LinkedHashMap<>();
    public String tabOrderMetaKey = "tab-order";
    public boolean useLuckPermsWeightFallback = true;
    public boolean higherWeightFirst = true;
    public int defaultTabOrder = 9999;
    public int tabUpdateIntervalTicks = 20;

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
    public static boolean enableLuckPermsPrefixes() { ensure(); return instance.enableLuckPermsPrefixes; }
    public static boolean enableTabSorting() { ensure(); return instance.enableTabSorting; }
    public static String tabOrderMetaKey() { ensure(); return instance.tabOrderMetaKey; }
    public static boolean useLuckPermsWeightFallback() { ensure(); return instance.useLuckPermsWeightFallback; }
    public static boolean higherWeightFirst() { ensure(); return instance.higherWeightFirst; }
    public static int defaultTabOrder() { ensure(); return clampTabOrder(instance.defaultTabOrder); }
    public static int tabUpdateIntervalTicks() { ensure(); return Math.max(1, instance.tabUpdateIntervalTicks); }

    public static Integer groupTabOrder(String groupName) {
        ensure();
        if (groupName == null || instance.tabGroupOrder == null) return null;
        for (Map.Entry<String, Integer> entry : instance.tabGroupOrder.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(groupName)) {
                return entry.getValue() == null ? null : clampTabOrder(entry.getValue());
            }
        }
        return null;
    }

    public static int clampTabOrder(int order) {
        return Math.max(0, Math.min(9999, order));
    }

    private static void ensure() {
        if (instance == null) reload(configDir);
    }

    public static void reload(Path dir) {
        configDir = dir;
        Path file = dir.resolve("vchat-tab.json");
        if (Files.exists(file)) {
            try {
                JsonObject json;
                try (Reader reader = Files.newBufferedReader(file)) {
                    json = JsonParser.parseReader(reader).getAsJsonObject();
                }
                boolean changed = addMissingDefaults(json);
                instance = GSON.fromJson(json, VChatTabConfig.class);
                normalize();
                if (changed) {
                    Files.writeString(file, GSON.toJson(json));
                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
                instance = new VChatTabConfig();
                return;
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

    private static boolean addMissingDefaults(JsonObject json) {
        JsonObject defaults = GSON.toJsonTree(new VChatTabConfig()).getAsJsonObject();
        boolean changed = false;
        for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
            if (!json.has(entry.getKey())) {
                json.add(entry.getKey(), entry.getValue().deepCopy());
                changed = true;
            }
        }
        return changed;
    }

    private static void normalize() {
        if (instance == null) instance = new VChatTabConfig();
        if (instance.tabGroupOrder == null) instance.tabGroupOrder = new LinkedHashMap<>();
        if (instance.tabOrderMetaKey == null) instance.tabOrderMetaKey = "";
    }
}
