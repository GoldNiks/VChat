package com.vchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class VChatTabConfig {
    private static final int CURRENT_CONFIG_VERSION = 2;

    public int configVersion = CURRENT_CONFIG_VERSION;
    public TabSettings tab = new TabSettings();
    public ChatSettings chat = new ChatSettings();
    public LuckPermsSettings luckPerms = new LuckPermsSettings();

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static VChatTabConfig instance;
    private static Path configDir = Path.of("config");

    public static String header() { ensure(); return instance.tab.header; }
    public static String footer() { ensure(); return instance.tab.footer; }
    public static String joinMessage() { ensure(); return instance.tab.joinMessage; }
    public static String tabPlayerFormat() { ensure(); return instance.tab.playerFormat; }
    public static int tabUpdateIntervalTicks() { ensure(); return Math.max(1, instance.tab.updateIntervalTicks); }
    public static int localChatRadius() { ensure(); return Math.max(0, instance.chat.localRadius); }
    public static boolean enableGlobalChat() { ensure(); return instance.chat.enableGlobal; }
    public static boolean enableLocalChat() { ensure(); return instance.chat.enableLocal; }
    public static String globalCommand() { ensure(); return instance.chat.globalCommand; }
    public static String globalChatFormat() { ensure(); return instance.chat.globalFormat; }
    public static String localChatFormat() { ensure(); return instance.chat.localFormat; }
    public static boolean mentionNoOneHeard() { ensure(); return instance.chat.notifyWhenNoOneHeard; }
    public static String noOneHeardMessage() { ensure(); return instance.chat.noOneHeardMessage; }
    public static boolean playerFormattingEnabled() { ensure(); return instance.chat.playerFormatting.enabled; }
    public static boolean colorsForEveryone() { ensure(); return instance.chat.playerFormatting.colorsForEveryone; }
    public static boolean hexForEveryone() { ensure(); return instance.chat.playerFormatting.hexForEveryone; }
    public static boolean stylesForEveryone() { ensure(); return instance.chat.playerFormatting.stylesForEveryone; }
    public static boolean obfuscatedForEveryone() { ensure(); return instance.chat.playerFormatting.obfuscatedForEveryone; }
    public static boolean enableLuckPermsPrefixes() { ensure(); return instance.luckPerms.showPrefixes; }
    public static boolean enableLuckPermsSuffixes() { ensure(); return instance.luckPerms.showSuffixes; }
    public static boolean enableTabSorting() { ensure(); return instance.luckPerms.sortTabByWeight; }
    public static boolean higherWeightFirst() { ensure(); return instance.luckPerms.higherWeightFirst; }

    private static void ensure() {
        if (instance == null) reload(configDir);
    }

    public static void reload(Path dir) {
        configDir = dir;
        Path file = dir.resolve("vchat-config.json5");
        if (Files.exists(file)) {
            instance = read(file);
            return;
        }

        Path legacyFile = dir.resolve("vchat-tab.json");
        if (Files.exists(legacyFile)) {
            instance = migrateLegacy(legacyFile);
        } else {
            instance = new VChatTabConfig();
        }
        normalize();
        writeTemplate(file, instance);
    }

    private static VChatTabConfig read(Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            int fileVersion = getInt(json, "configVersion", 1);
            VChatTabConfig loaded = GSON.fromJson(json, VChatTabConfig.class);
            instance = loaded;
            normalize();
            if (fileVersion < CURRENT_CONFIG_VERSION) {
                if (json.has("luckPerms") && json.get("luckPerms").isJsonObject()) {
                    JsonObject oldLuckPerms = json.getAsJsonObject("luckPerms");
                    if (!oldLuckPerms.has("showSuffixes")) {
                        instance.luckPerms.showSuffixes = instance.luckPerms.showPrefixes;
                    }
                }
                upgradeOldDefaults(instance);
                instance.configVersion = CURRENT_CONFIG_VERSION;
                writeTemplate(file, instance);
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return new VChatTabConfig();
        }
    }

    private static VChatTabConfig migrateLegacy(Path legacyFile) {
        VChatTabConfig migrated = new VChatTabConfig();
        try (Reader reader = Files.newBufferedReader(legacyFile, StandardCharsets.UTF_8)) {
            JsonObject old = JsonParser.parseReader(reader).getAsJsonObject();
            migrated.tab.header = getString(old, "header", migrated.tab.header);
            migrated.tab.footer = getString(old, "footer", migrated.tab.footer);
            migrated.tab.joinMessage = getString(old, "joinMessage", migrated.tab.joinMessage);
            migrated.tab.updateIntervalTicks = getInt(old, "tabUpdateIntervalTicks", migrated.tab.updateIntervalTicks);

            migrated.chat.localRadius = getInt(old, "localChatRadius", migrated.chat.localRadius);
            migrated.chat.enableGlobal = getBoolean(old, "enableGlobalChat", migrated.chat.enableGlobal);
            migrated.chat.enableLocal = getBoolean(old, "enableLocalChat", migrated.chat.enableLocal);
            migrated.chat.globalCommand = getString(old, "globalCommand", migrated.chat.globalCommand);
            migrated.chat.globalFormat = getString(old, "globalChatFormat", migrated.chat.globalFormat);
            migrated.chat.localFormat = getString(old, "localChatFormat", migrated.chat.localFormat);
            migrated.chat.notifyWhenNoOneHeard = getBoolean(old, "mentionNoOneHeard", migrated.chat.notifyWhenNoOneHeard);
            migrated.chat.noOneHeardMessage = getString(old, "noOneHeardMessage", migrated.chat.noOneHeardMessage);

            migrated.luckPerms.showPrefixes = getBoolean(old, "enableLuckPermsPrefixes", migrated.luckPerms.showPrefixes);
            migrated.luckPerms.showSuffixes = migrated.luckPerms.showPrefixes;
            migrated.luckPerms.sortTabByWeight = getBoolean(old, "enableTabSorting", migrated.luckPerms.sortTabByWeight);
            migrated.luckPerms.higherWeightFirst = getBoolean(old, "higherWeightFirst", migrated.luckPerms.higherWeightFirst);
        } catch (Exception e) {
            e.printStackTrace();
        }
        upgradeOldDefaults(migrated);
        return migrated;
    }

    private static void writeTemplate(Path file, VChatTabConfig config) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, annotatedJson(config), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String annotatedJson(VChatTabConfig config) {
        return """
                {
                  // Версия структуры конфига. Не изменяйте вручную.
                  "configVersion": %d,

                  // Настройки верхней и нижней части TAB.
                  "tab": {
                    // Текст сверху. Доступны: %%online%%, %%max%%, %%player%%.
                    "header": %s,
                    // Текст снизу. Доступны те же подстановки.
                    "footer": %s,
                    // Личное сообщение игроку после входа на сервер.
                    "joinMessage": %s,
                    // Формат строки игрока в TAB.
                    // Доступны: <prefix>, <suffix>, <name>, <display_name>, <group>, <world>.
                    "playerFormat": %s,
                    // Как часто обновлять TAB и данные LuckPerms. 20 тиков = примерно 1 секунда.
                    "updateIntervalTicks": %d
                  },

                  // Настройки локального и глобального чата.
                  "chat": {
                    // Радиус локального чата в блоках.
                    "localRadius": %d,
                    // Включить глобальный чат (!сообщение и команда ниже).
                    "enableGlobal": %s,
                    // Включить обычный локальный чат.
                    "enableLocal": %s,
                    // Команда глобального чата без символа /. Например: g означает /g сообщение.
                    // После изменения имени команды требуется перезапуск сервера.
                    "globalCommand": %s,
                    // Формат глобального сообщения. Все placeholders перечислены ниже в README.
                    "globalFormat": %s,
                    // Формат локального сообщения. Обычно отличается меткой [L].
                    "localFormat": %s,
                    // Сообщать отправителю, если рядом никто не услышал локальное сообщение.
                    "notifyWhenNoOneHeard": %s,
                    // Текст этого уведомления.
                    "noOneHeardMessage": %s,

                    // Оформление, которое игроки могут писать внутри своих сообщений.
                    // Операторы с уровнем 2 всегда имеют все разрешения.
                    "playerFormatting": {
                      // Главный переключатель форматирования сообщений игроками.
                      "enabled": %s,
                      // true разрешает обычные цвета &0-&f всем игрокам.
                      // false требует LuckPerms permission: vchat.format.color
                      "colorsForEveryone": %s,
                      // true разрешает HEX (#RRGGBB, &#RRGGBB, &%%23RRGGBB) всем игрокам.
                      // false требует permission: vchat.format.hex
                      "hexForEveryone": %s,
                      // true разрешает &l, &m, &n, &o и &r всем игрокам.
                      // false требует permission: vchat.format.style
                      "stylesForEveryone": %s,
                      // Эффект &k лучше оставить только администрации.
                      // false требует permission: vchat.format.obfuscated
                      "obfuscatedForEveryone": %s
                    }
                  },

                  // Интеграция с LuckPerms. Если LuckPerms не установлен, мод продолжит работать без префиксов.
                  "luckPerms": {
                    // Показывать префикс LuckPerms перед ником игрока.
                    "showPrefixes": %s,
                    // Показывать suffix LuckPerms после ника игрока.
                    "showSuffixes": %s,
                    // Сортировать TAB по weight основной группы LuckPerms.
                    "sortTabByWeight": %s,
                    // true: больший weight выше. false: меньший weight выше.
                    "higherWeightFirst": %s
                  }
                }
                """.formatted(
                CURRENT_CONFIG_VERSION, json(config.tab.header), json(config.tab.footer),
                json(config.tab.joinMessage), json(config.tab.playerFormat),
                Math.max(1, config.tab.updateIntervalTicks), config.chat.localRadius,
                config.chat.enableGlobal, config.chat.enableLocal, json(config.chat.globalCommand),
                json(config.chat.globalFormat), json(config.chat.localFormat),
                config.chat.notifyWhenNoOneHeard, json(config.chat.noOneHeardMessage),
                config.chat.playerFormatting.enabled,
                config.chat.playerFormatting.colorsForEveryone,
                config.chat.playerFormatting.hexForEveryone,
                config.chat.playerFormatting.stylesForEveryone,
                config.chat.playerFormatting.obfuscatedForEveryone,
                config.luckPerms.showPrefixes, config.luckPerms.showSuffixes,
                config.luckPerms.sortTabByWeight,
                config.luckPerms.higherWeightFirst);
    }

    private static String json(String value) {
        return GSON.toJson(value == null ? "" : value);
    }

    private static String getString(JsonObject json, String key, String fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : fallback;
    }

    private static int getInt(JsonObject json, String key, int fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : fallback;
    }

    private static boolean getBoolean(JsonObject json, String key, boolean fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsBoolean() : fallback;
    }

    private static void normalize() {
        if (instance == null) instance = new VChatTabConfig();
        if (instance.tab == null) instance.tab = new TabSettings();
        if (instance.chat == null) instance.chat = new ChatSettings();
        if (instance.luckPerms == null) instance.luckPerms = new LuckPermsSettings();
        if (instance.chat.playerFormatting == null) {
            instance.chat.playerFormatting = new PlayerFormattingSettings();
        }

        TabSettings defaultTab = new TabSettings();
        if (instance.tab.header == null) instance.tab.header = defaultTab.header;
        if (instance.tab.footer == null) instance.tab.footer = defaultTab.footer;
        if (instance.tab.joinMessage == null) instance.tab.joinMessage = defaultTab.joinMessage;
        if (instance.tab.playerFormat == null || instance.tab.playerFormat.isBlank()) {
            instance.tab.playerFormat = defaultTab.playerFormat;
        }

        ChatSettings defaultChat = new ChatSettings();
        if (instance.chat.globalCommand == null || instance.chat.globalCommand.isBlank()) {
            instance.chat.globalCommand = defaultChat.globalCommand;
        }
        if (instance.chat.globalFormat == null) instance.chat.globalFormat = defaultChat.globalFormat;
        if (instance.chat.localFormat == null) instance.chat.localFormat = defaultChat.localFormat;
        if (instance.chat.noOneHeardMessage == null) {
            instance.chat.noOneHeardMessage = defaultChat.noOneHeardMessage;
        }
    }

    private static void upgradeOldDefaults(VChatTabConfig config) {
        if ("&e[G] &7<name>: &f<message>".equals(config.chat.globalFormat)) {
            config.chat.globalFormat = new ChatSettings().globalFormat;
        }
        if ("&7[L] &7<name>: &f<message>".equals(config.chat.localFormat)) {
            config.chat.localFormat = new ChatSettings().localFormat;
        }
    }

    public static final class TabSettings {
        public String header = "\n&6&l&nVChat\n\n&7Игроки: &a%online%\n\n&7&m-----------------";
        public String footer = "&7&m-----------------\n\n&7Баланс: &e0";
        public String joinMessage = "&aДобро пожаловать на &6&l&nVChat&a!";
        public String playerFormat = "<prefix>&f<name><suffix>";
        public int updateIntervalTicks = 20;
    }

    public static final class ChatSettings {
        public int localRadius = 100;
        public boolean enableGlobal = true;
        public boolean enableLocal = true;
        public String globalCommand = "g";
        public String globalFormat = "&e[G] <prefix>&f<name><suffix>&7: &f<message>";
        public String localFormat = "&7[L] <prefix>&f<name><suffix>&7: &f<message>";
        public boolean notifyWhenNoOneHeard = true;
        public String noOneHeardMessage = "&7Вас никто не услышал";
        public PlayerFormattingSettings playerFormatting = new PlayerFormattingSettings();
    }

    public static final class PlayerFormattingSettings {
        public boolean enabled = true;
        public boolean colorsForEveryone = false;
        public boolean hexForEveryone = false;
        public boolean stylesForEveryone = false;
        public boolean obfuscatedForEveryone = false;
    }

    public static final class LuckPermsSettings {
        public boolean showPrefixes = true;
        public boolean showSuffixes = true;
        public boolean sortTabByWeight = true;
        public boolean higherWeightFirst = true;
    }
}
