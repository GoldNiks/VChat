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
import java.util.ArrayList;
import java.util.List;

public class VChatTabConfig {
    private static final int CURRENT_CONFIG_VERSION = 3;

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
    public static String globalDisabledMessage() { ensure(); return instance.chat.globalDisabledMessage; }
    public static String localDisabledMessage() { ensure(); return instance.chat.localDisabledMessage; }
    public static boolean playerFormattingEnabled() { ensure(); return instance.chat.playerFormatting.enabled; }
    public static boolean colorsForEveryone() { ensure(); return instance.chat.playerFormatting.colorsForEveryone; }
    public static boolean hexForEveryone() { ensure(); return instance.chat.playerFormatting.hexForEveryone; }
    public static boolean stylesForEveryone() { ensure(); return instance.chat.playerFormatting.stylesForEveryone; }
    public static boolean obfuscatedForEveryone() { ensure(); return instance.chat.playerFormatting.obfuscatedForEveryone; }
    public static boolean antiSpamEnabled() { ensure(); return instance.chat.antiSpam.enabled; }
    public static int maxMessageLength() { ensure(); return Math.max(1, instance.chat.antiSpam.maxMessageLength); }
    public static long cooldownMillis() { ensure(); return Math.max(0, instance.chat.antiSpam.cooldownMillis); }
    public static boolean blockRepeatedMessages() { ensure(); return instance.chat.antiSpam.blockRepeatedMessages; }
    public static long repeatWindowMillis() { ensure(); return Math.max(0, instance.chat.antiSpam.repeatWindowSeconds) * 1000L; }
    public static String tooLongMessage() { ensure(); return instance.chat.antiSpam.tooLongMessage; }
    public static String tooFastMessage() { ensure(); return instance.chat.antiSpam.tooFastMessage; }
    public static String repeatedMessage() { ensure(); return instance.chat.antiSpam.repeatedMessage; }
    public static boolean mentionsEnabled() { ensure(); return instance.chat.mentions.enabled; }
    public static String mentionFormat() { ensure(); return instance.chat.mentions.highlightFormat; }
    public static boolean mentionSoundEnabled() { ensure(); return instance.chat.mentions.playSound; }
    public static String mentionSound() { ensure(); return instance.chat.mentions.sound; }
    public static float mentionVolume() { ensure(); return Math.max(0.0F, instance.chat.mentions.volume); }
    public static float mentionPitch() { ensure(); return Math.max(0.01F, instance.chat.mentions.pitch); }
    public static boolean ignoreEnabled() { ensure(); return instance.chat.ignore.enabled; }
    public static String ignoreAddedMessage() { ensure(); return instance.chat.ignore.addedMessage; }
    public static String ignoreRemovedMessage() { ensure(); return instance.chat.ignore.removedMessage; }
    public static String ignoreDisabledMessage() { ensure(); return instance.chat.ignore.disabledMessage; }
    public static String cannotIgnoreSelfMessage() { ensure(); return instance.chat.ignore.cannotIgnoreSelfMessage; }
    public static String ignoreUsageMessage() { ensure(); return instance.chat.ignore.usageMessage; }
    public static String ignoreClearedMessage() { ensure(); return instance.chat.ignore.clearedMessage; }
    public static boolean logChatMessages() { ensure(); return instance.chat.logging.logChatMessages; }
    public static boolean logCommands() { ensure(); return instance.chat.logging.logCommands; }
    public static boolean includeCommandArguments() { ensure(); return instance.chat.logging.includeCommandArguments; }
    public static List<String> redactedCommands() { ensure(); return List.copyOf(instance.chat.logging.redactedCommands); }
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
            IgnoreManager.configure(dir);
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
        IgnoreManager.configure(dir);
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
                    // Сообщение, если игрок пишет в выключенный глобальный чат.
                    "globalDisabledMessage": %s,
                    // Сообщение, если выключен локальный чат.
                    "localDisabledMessage": %s,

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
                    },

                    // Защита от спама. Permission обхода: vchat.antispam.bypass
                    "antiSpam": {
                      "enabled": %s,
                      // Максимальная длина сообщения в символах Unicode.
                      "maxMessageLength": %d,
                      // Минимальная задержка между сообщениями в миллисекундах.
                      "cooldownMillis": %d,
                      // Запрещать повтор одного и того же сообщения.
                      "blockRepeatedMessages": %s,
                      // В течение скольких секунд сообщение считается повтором.
                      "repeatWindowSeconds": %d,
                      // В сообщениях доступны <max> и <seconds>.
                      "tooLongMessage": %s,
                      "tooFastMessage": %s,
                      "repeatedMessage": %s
                    },

                    // Упоминания игроков через @Ник.
                    "mentions": {
                      "enabled": %s,
                      // <name> заменяется точным ником упомянутого игрока.
                      "highlightFormat": %s,
                      "playSound": %s,
                      // Идентификатор звука Minecraft.
                      "sound": %s,
                      "volume": %s,
                      "pitch": %s
                    },

                    // Постоянный персональный список игнорирования: /ignore <игрок>.
                    "ignore": {
                      "enabled": %s,
                      // В сообщениях доступен <name>.
                      "addedMessage": %s,
                      "removedMessage": %s,
                      "disabledMessage": %s,
                      "cannotIgnoreSelfMessage": %s,
                      // В сообщении доступен <count>.
                      "usageMessage": %s,
                      "clearedMessage": %s
                    },

                    // Безопасное серверное логирование.
                    "logging": {
                      "logChatMessages": %s,
                      "logCommands": %s,
                      // false записывает только название команды без аргументов.
                      "includeCommandArguments": %s,
                      // Аргументы этих команд скрываются всегда.
                      "redactedCommands": %s
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
                json(config.chat.globalDisabledMessage), json(config.chat.localDisabledMessage),
                config.chat.playerFormatting.enabled,
                config.chat.playerFormatting.colorsForEveryone,
                config.chat.playerFormatting.hexForEveryone,
                config.chat.playerFormatting.stylesForEveryone,
                config.chat.playerFormatting.obfuscatedForEveryone,
                config.chat.antiSpam.enabled, Math.max(1, config.chat.antiSpam.maxMessageLength),
                Math.max(0, config.chat.antiSpam.cooldownMillis),
                config.chat.antiSpam.blockRepeatedMessages,
                Math.max(0, config.chat.antiSpam.repeatWindowSeconds),
                json(config.chat.antiSpam.tooLongMessage), json(config.chat.antiSpam.tooFastMessage),
                json(config.chat.antiSpam.repeatedMessage),
                config.chat.mentions.enabled, json(config.chat.mentions.highlightFormat),
                config.chat.mentions.playSound, json(config.chat.mentions.sound),
                config.chat.mentions.volume, config.chat.mentions.pitch,
                config.chat.ignore.enabled, json(config.chat.ignore.addedMessage),
                json(config.chat.ignore.removedMessage), json(config.chat.ignore.disabledMessage),
                json(config.chat.ignore.cannotIgnoreSelfMessage),
                json(config.chat.ignore.usageMessage),
                json(config.chat.ignore.clearedMessage),
                config.chat.logging.logChatMessages, config.chat.logging.logCommands,
                config.chat.logging.includeCommandArguments,
                GSON.toJson(config.chat.logging.redactedCommands),
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
        if (instance.chat.antiSpam == null) instance.chat.antiSpam = new AntiSpamSettings();
        if (instance.chat.mentions == null) instance.chat.mentions = new MentionSettings();
        if (instance.chat.ignore == null) instance.chat.ignore = new IgnoreSettings();
        if (instance.chat.logging == null) instance.chat.logging = new LoggingSettings();
        if (instance.chat.logging.redactedCommands == null) {
            instance.chat.logging.redactedCommands = new ArrayList<>(new LoggingSettings().redactedCommands);
        }
        instance.chat.logging.redactedCommands.removeIf(command -> command == null || command.isBlank());

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
        if (instance.chat.globalDisabledMessage == null) {
            instance.chat.globalDisabledMessage = defaultChat.globalDisabledMessage;
        }
        if (instance.chat.localDisabledMessage == null) {
            instance.chat.localDisabledMessage = defaultChat.localDisabledMessage;
        }

        AntiSpamSettings defaultAntiSpam = new AntiSpamSettings();
        if (instance.chat.antiSpam.tooLongMessage == null) {
            instance.chat.antiSpam.tooLongMessage = defaultAntiSpam.tooLongMessage;
        }
        if (instance.chat.antiSpam.tooFastMessage == null) {
            instance.chat.antiSpam.tooFastMessage = defaultAntiSpam.tooFastMessage;
        }
        if (instance.chat.antiSpam.repeatedMessage == null) {
            instance.chat.antiSpam.repeatedMessage = defaultAntiSpam.repeatedMessage;
        }

        MentionSettings defaultMentions = new MentionSettings();
        if (instance.chat.mentions.highlightFormat == null) {
            instance.chat.mentions.highlightFormat = defaultMentions.highlightFormat;
        }
        if (instance.chat.mentions.sound == null || instance.chat.mentions.sound.isBlank()) {
            instance.chat.mentions.sound = defaultMentions.sound;
        }

        IgnoreSettings defaultIgnore = new IgnoreSettings();
        if (instance.chat.ignore.addedMessage == null) {
            instance.chat.ignore.addedMessage = defaultIgnore.addedMessage;
        }
        if (instance.chat.ignore.removedMessage == null) {
            instance.chat.ignore.removedMessage = defaultIgnore.removedMessage;
        }
        if (instance.chat.ignore.disabledMessage == null) {
            instance.chat.ignore.disabledMessage = defaultIgnore.disabledMessage;
        }
        if (instance.chat.ignore.cannotIgnoreSelfMessage == null) {
            instance.chat.ignore.cannotIgnoreSelfMessage = defaultIgnore.cannotIgnoreSelfMessage;
        }
        if (instance.chat.ignore.usageMessage == null) {
            instance.chat.ignore.usageMessage = defaultIgnore.usageMessage;
        }
        if (instance.chat.ignore.clearedMessage == null) {
            instance.chat.ignore.clearedMessage = defaultIgnore.clearedMessage;
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
        public String globalDisabledMessage = "&cГлобальный чат сейчас отключён";
        public String localDisabledMessage = "&cЛокальный чат сейчас отключён";
        public PlayerFormattingSettings playerFormatting = new PlayerFormattingSettings();
        public AntiSpamSettings antiSpam = new AntiSpamSettings();
        public MentionSettings mentions = new MentionSettings();
        public IgnoreSettings ignore = new IgnoreSettings();
        public LoggingSettings logging = new LoggingSettings();
    }

    public static final class PlayerFormattingSettings {
        public boolean enabled = true;
        public boolean colorsForEveryone = false;
        public boolean hexForEveryone = false;
        public boolean stylesForEveryone = false;
        public boolean obfuscatedForEveryone = false;
    }

    public static final class AntiSpamSettings {
        public boolean enabled = true;
        public int maxMessageLength = 256;
        public int cooldownMillis = 1000;
        public boolean blockRepeatedMessages = true;
        public int repeatWindowSeconds = 15;
        public String tooLongMessage = "&cСообщение слишком длинное. Максимум: <max> символов";
        public String tooFastMessage = "&cНе так быстро. Подождите ещё <seconds> сек.";
        public String repeatedMessage = "&cНе повторяйте одно и то же сообщение";
    }

    public static final class MentionSettings {
        public boolean enabled = true;
        public String highlightFormat = "&e&l@<name>&r&f";
        public boolean playSound = true;
        public String sound = "minecraft:entity.experience_orb.pickup";
        public float volume = 0.8F;
        public float pitch = 1.2F;
    }

    public static final class IgnoreSettings {
        public boolean enabled = true;
        public String addedMessage = "&7Вы больше не видите сообщения игрока &f<name>";
        public String removedMessage = "&7Вы снова видите сообщения игрока &f<name>";
        public String disabledMessage = "&cСистема игнорирования отключена";
        public String cannotIgnoreSelfMessage = "&cНельзя игнорировать самого себя";
        public String usageMessage = "&7Использование: &f/ignore <игрок>&7 или &f/ignore clear&7. В списке: &f<count>";
        public String clearedMessage = "&7Список игнорирования очищен. Удалено игроков: &f<count>";
    }

    public static final class LoggingSettings {
        public boolean logChatMessages = true;
        public boolean logCommands = true;
        public boolean includeCommandArguments = false;
        public List<String> redactedCommands = new ArrayList<>(List.of(
                "login", "l", "register", "reg", "changepassword", "cp", "password", "2fa"
        ));
    }

    public static final class LuckPermsSettings {
        public boolean showPrefixes = true;
        public boolean showSuffixes = true;
        public boolean sortTabByWeight = true;
        public boolean higherWeightFirst = true;
    }
}
