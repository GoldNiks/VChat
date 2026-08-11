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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class VChatTabConfig {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("VChat");
    private static final int CURRENT_CONFIG_VERSION = 15;

    public int configVersion = CURRENT_CONFIG_VERSION;
    public TabSettings tab = new TabSettings();
    public ChatSettings chat = new ChatSettings();
    public LuckPermsSettings luckPerms = new LuckPermsSettings();
    public FTBTeamsSettings ftbTeams = new FTBTeamsSettings();
    public DeathMessageSettings deathMessages = new DeathMessageSettings();
    public StagesSettings stages = new StagesSettings();
    public DiscordSettings discord = new DiscordSettings();
    public AnnouncementsSettings announcements = new AnnouncementsSettings();

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static VChatTabConfig instance;
    private static Path configDir = Path.of("config", "VMods", "VChat");

    public static String header() { ensure(); return instance.tab.header; }
    public static String footer() { ensure(); return instance.tab.footer; }
    public static String joinMessage() { ensure(); return instance.tab.joinMessage; }
    public static String firstJoinMessage() { ensure(); return instance.tab.firstJoinMessage; }
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
    public static String emptyMessage() { ensure(); return instance.chat.antiSpam.emptyMessage; }
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
    public static long ignoreCommandCooldownMillis() { ensure(); return instance.chat.ignore.commandCooldownMillis; }
    public static long ignoreSaveIntervalMillis() { ensure(); return instance.chat.ignore.saveIntervalMillis; }
    public static String ignoreCooldownMessage() { ensure(); return instance.chat.ignore.cooldownMessage; }
    public static boolean logChatMessages() { ensure(); return instance.chat.logging.logChatMessages; }
    public static boolean logCommands() { ensure(); return instance.chat.logging.logCommands; }
    public static boolean includeCommandArguments() { ensure(); return instance.chat.logging.includeCommandArguments; }
    public static List<String> redactedCommands() { ensure(); return List.copyOf(instance.chat.logging.redactedCommands); }
    public static boolean enableLuckPermsPrefixes() { ensure(); return instance.luckPerms.showPrefixes; }
    public static boolean enableLuckPermsSuffixes() { ensure(); return instance.luckPerms.showSuffixes; }
    public static boolean enableTabSorting() { ensure(); return instance.luckPerms.sortTabByWeight; }
    public static boolean higherWeightFirst() { ensure(); return instance.luckPerms.higherWeightFirst; }
    public static boolean ftbTeamsHoverEnabled() { ensure(); return instance.ftbTeams.showTeamOnNameHover; }
    public static boolean ftbTeamsShowTeamName() { ensure(); return instance.ftbTeams.showTeamName; }
    public static boolean ftbTeamsShowPlayerRank() { ensure(); return instance.ftbTeams.showPlayerRank; }
    public static boolean ftbTeamsShowMemberCount() { ensure(); return instance.ftbTeams.showMemberCount; }
    public static boolean ftbTeamsHideWithoutTeam() { ensure(); return instance.ftbTeams.hideHoverWithoutTeam; }
    public static String ftbTeamsTeamLabel() { ensure(); return instance.ftbTeams.teamLabel; }
    public static String ftbTeamsRankLabel() { ensure(); return instance.ftbTeams.rankLabel; }
    public static String ftbTeamsMembersLabel() { ensure(); return instance.ftbTeams.membersLabel; }
    public static String ftbTeamsNoTeamText() { ensure(); return instance.ftbTeams.noTeamText; }
    public static boolean stagesEnabled() { ensure(); return instance.stages.enabled; }
    public static boolean stagesAppendToSuffix() { ensure(); return instance.stages.appendToSuffix; }
    public static String stageDetectionMode() { ensure(); return instance.stages.detectionMode; }
    public static String stageSeparator() { ensure(); return instance.stages.separator; }
    public static List<StageQuest> stageQuests() { ensure(); return List.copyOf(instance.stages.quests); }
    public static List<StageChapter> stageChapters() { ensure(); return List.copyOf(instance.stages.chapters); }
    public static String stageSource() { ensure(); return instance.stages.quests.isEmpty() ? "chapters" : "quests"; }
    public static boolean discordEnabled() { ensure(); return instance.discord.enabled; }
    public static boolean discordRelayChatToDiscord() { ensure(); return instance.discord.enabled && instance.discord.relayChatToDiscord; }
    public static boolean discordRelayServerStatus() { ensure(); return instance.discord.enabled && instance.discord.relayServerStatus; }
    public static boolean discordBotEnabled() { ensure(); return instance.discord.enabled && instance.discord.botEnabled; }
    public static boolean discordRelayDiscordToGame() { ensure(); return instance.discord.enabled && instance.discord.relayDiscordToGame; }
    public static String discordChatWebhookUrl() { ensure(); return instance.discord.chatWebhookUrl; }
    public static String discordStatusWebhookUrl() { ensure(); return instance.discord.statusWebhookUrl; }
    public static String discordServerName() { ensure(); return instance.discord.serverName; }
    public static String discordWebhookUsername() { ensure(); return instance.discord.webhookUsername; }
    public static String discordWebhookAvatarUrl() { ensure(); return instance.discord.webhookAvatarUrl; }
    public static String discordGameToDiscordFormat() { ensure(); return instance.discord.gameToDiscordFormat; }
    public static String discordJoinFormat() { ensure(); return instance.discord.joinFormat; }
    public static String discordLeaveFormat() { ensure(); return instance.discord.leaveFormat; }
    public static String discordServerStartedFormat() { ensure(); return instance.discord.serverStartedFormat; }
    public static String discordServerStoppedFormat() { ensure(); return instance.discord.serverStoppedFormat; }
    public static String discordBotToken() { ensure(); return instance.discord.botToken; }
    public static long discordBotChannelId() { ensure(); return Math.max(0, instance.discord.botChannelId); }
    public static String discordToGameFormat() { ensure(); return instance.discord.discordToGameFormat; }
    public static boolean announcementsEnabled() { ensure(); return instance.announcements.enabled; }
    public static int announcementsIntervalSeconds() {
        ensure();
        return Math.max(60, Math.min(36000, instance.announcements.intervalSeconds));
    }
    public static List<String> announcementsMessages() {
        ensure();
        return instance.announcements.messages == null ? List.of() : List.copyOf(instance.announcements.messages);
    }
    public static boolean hidePlayerHeadsInDeathMessages() {
        ensure();
        return instance.deathMessages.enabled && instance.deathMessages.hidePlayerHeads;
    }

    private static void ensure() {
        if (instance == null) {
            reload(configDir);
            if (instance == null) {
                instance = new VChatTabConfig();
                normalize();
                IgnoreManager.configure(configDir);
                FirstJoinManager.configure(configDir);
            }
        }
    }

    public static void initialize(Path dir) {
        reload(dir);
        if (instance == null) {
            throw new IllegalStateException("VChat config is invalid and no last-working backup is available");
        }
    }

    public static boolean reload(Path dir) {
        configDir = dir;
        Path file = dir.resolve("vchat-config.json5");
        if (Files.exists(file)) {
            VChatTabConfig previous = instance;
            VChatTabConfig loaded = read(file);
            boolean recoveredFromBackup = false;
            if (loaded == null && previous == null) {
                Path backup = backupPath(file);
                if (Files.exists(backup)) {
                    loaded = read(backup);
                    recoveredFromBackup = loaded != null;
                    if (recoveredFromBackup) {
                        LOGGER.warn("Loaded last working VChat config backup: {}", backup);
                    }
                }
            }
            if (loaded == null) return false;
            instance = loaded;
            if (!recoveredFromBackup) backupConfig(file);
            IgnoreManager.configure(dir);
            FirstJoinManager.configure(dir);
            return !recoveredFromBackup;
        }

        Path legacyFile = dir.resolve("vchat-tab.json");
        if (Files.exists(legacyFile)) {
            instance = migrateLegacy(legacyFile);
        } else {
            instance = new VChatTabConfig();
        }
        normalize();
        migrateMcChatLinkToml();
        if (!writeTemplate(file, instance)) return false;
        backupConfig(file);
        IgnoreManager.configure(dir);
        FirstJoinManager.configure(dir);
        return true;
    }

    private static VChatTabConfig read(Path file) {
        VChatTabConfig previous = instance;
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            int fileVersion = getInt(json, "configVersion", 1);
            boolean hasDiscordSection = json.has("discord") && json.get("discord").isJsonObject();
            VChatTabConfig loaded = GSON.fromJson(json, VChatTabConfig.class);
            instance = loaded;
            normalize();
            validate();
            if (fileVersion < CURRENT_CONFIG_VERSION) {
                if (json.has("luckPerms") && json.get("luckPerms").isJsonObject()) {
                    JsonObject oldLuckPerms = json.getAsJsonObject("luckPerms");
                    if (!oldLuckPerms.has("showSuffixes")) {
                        instance.luckPerms.showSuffixes = instance.luckPerms.showPrefixes;
                    }
                }
                if (fileVersion < 8 && instance.chat.antiSpam.cooldownMillis == 1000) {
                    instance.chat.antiSpam.cooldownMillis = 500;
                }
                upgradeOldDefaults(instance);
                // Import MC Chat Link only for genuinely old configs which did
                // not have VChat's own Discord section. Never overwrite values
                // explicitly configured by the administrator.
                if (!hasDiscordSection) migrateMcChatLinkToml();
                instance.configVersion = CURRENT_CONFIG_VERSION;
                if (!writeTemplate(file, instance)) {
                    instance = previous;
                    return null;
                }
            }
            return instance;
        } catch (Exception e) {
            instance = previous;
            LOGGER.error("VChat config was not reloaded; keeping the last working settings: {}", file, e);
            return null;
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

    private static boolean writeTemplate(Path file, VChatTabConfig config) {
        try {
            Files.createDirectories(file.getParent());
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, annotatedJson(config), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Could not write VChat config: {}", file, e);
            return false;
        }
    }

    private static Path backupPath(Path file) {
        return file.resolveSibling(file.getFileName() + ".last-good");
    }

    private static void backupConfig(Path file) {
        try {
            Files.copy(file, backupPath(file), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("Could not update VChat last-working config backup: {}", file, e);
        }
    }

    private static String annotatedJson(VChatTabConfig config) {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append(indentBlock(versionJson(config)));
        sb.append(sectionBlock(tabJson(config))).append(",\n");
        sb.append(sectionBlock(chatJson(config))).append(",\n");
        sb.append(sectionBlock(luckPermsJson(config))).append(",\n");
        sb.append(sectionBlock(ftbTeamsJson(config))).append(",\n");
        sb.append(sectionBlock(deathMessagesJson(config))).append(",\n");
        sb.append(sectionBlock(stagesJson(config))).append(",\n");
        sb.append(sectionBlock(discordJson(config))).append(",\n");
        sb.append(sectionBlock(announcementsJson(config)));
        sb.append("}\n");
        return sb.toString();
    }

    private static String indentBlock(String body) {
        return "  " + body.replace("\n", "\n  ").replace("\n  \n", "\n\n").stripTrailing() + "\n";
    }

    private static String sectionBlock(String body) {
        return "\n" + indentBlock(body);
    }

    private static String versionJson(VChatTabConfig config) {
        return """
                // Версия структуры конфига. Не изменяйте вручную.
                "configVersion": %d,
                """.formatted(CURRENT_CONFIG_VERSION);
    }

    private static String tabJson(VChatTabConfig config) {
        return """
                // Настройки верхней и нижней части TAB.
                "tab": {
                  // Текст сверху. Новая строка: \\n. Пустая строка: \\n\\n.
                  // Жёсткого лимита строк нет, но для небольших экранов рекомендуется 2-4 строки.
                  // Доступны: %%online%%, %%max%%, %%player%%, %%balance%%, %%tps%%.
                  // Цвета: &0-&f, стили: &l &m &n &o &r, HEX: &#RRGGBB.
                  "header": %s,
                  // Текст снизу. Переносы, цвета и подстановки работают так же.
                  // Рекомендуется 1-3 строки, чтобы TAB не выходил за границы экрана.
                  "footer": %s,
                  // Личное сообщение игроку после входа на сервер.
                  "joinMessage": %s,
                  // Сообщение всем игрокам при самом первом входе нового игрока на сервер.
                  // Доступен placeholder <name>. UUID всех уже заходивших игроков
                  // хранятся в config/vchat-firstjoin.json.
                  "firstJoinMessage": %s,
                  // Формат строки игрока в TAB.
                  // Доступны: <prefix>, <suffix>, <name>, <display_name>, <group>, <world>, <stage>, <balance>, <tps>.
                  "playerFormat": %s,
                  // Как часто обновлять TAB и данные LuckPerms. 20 тиков = примерно 1 секунда.
                  "updateIntervalTicks": %d
                }
                """.formatted(json(config.tab.header), json(config.tab.footer),
                json(config.tab.joinMessage), json(config.tab.firstJoinMessage),
                json(config.tab.playerFormat),
                Math.max(1, config.tab.updateIntervalTicks));
    }

    private static String chatJson(VChatTabConfig config) {
        return """
                // Настройки локального и глобального чата.
                "chat": {
                  // Радиус локального чата в блоках.
                  "localRadius": %d,
                  // Включить глобальный чат (!сообщение и команда ниже).
                  "enableGlobal": %s,
                  // Включить обычный локальный чат.
                  "enableLocal": %s,
                  // Команда глобального чата без символа /. Например: g означает /g сообщение.
                  // Разрешены строчные a-z, цифры, _, - и :; максимум 32 символа.
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
                    "repeatedMessage": %s,
                    // Ответ на ! без текста или сообщение только из пробелов.
                    "emptyMessage": %s
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
                    "clearedMessage": %s,
                    // Защита команды /ignore от частых переключений.
                    "commandCooldownMillis": %d,
                    // Изменения объединяются и записываются на диск не чаще этого интервала.
                    "saveIntervalMillis": %d,
                    "cooldownMessage": %s
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
                }
                """.formatted(config.chat.localRadius, config.chat.enableGlobal,
                config.chat.enableLocal, json(config.chat.globalCommand),
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
                json(config.chat.antiSpam.repeatedMessage), json(config.chat.antiSpam.emptyMessage),
                config.chat.mentions.enabled, json(config.chat.mentions.highlightFormat),
                config.chat.mentions.playSound, json(config.chat.mentions.sound),
                config.chat.mentions.volume, config.chat.mentions.pitch,
                config.chat.ignore.enabled, json(config.chat.ignore.addedMessage),
                json(config.chat.ignore.removedMessage), json(config.chat.ignore.disabledMessage),
                json(config.chat.ignore.cannotIgnoreSelfMessage), json(config.chat.ignore.usageMessage),
                json(config.chat.ignore.clearedMessage),
                Math.max(0, config.chat.ignore.commandCooldownMillis),
                Math.max(50, config.chat.ignore.saveIntervalMillis),
                json(config.chat.ignore.cooldownMessage),
                config.chat.logging.logChatMessages, config.chat.logging.logCommands,
                config.chat.logging.includeCommandArguments,
                GSON.toJson(config.chat.logging.redactedCommands));
    }

    private static String luckPermsJson(VChatTabConfig config) {
        return """
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
                """.formatted(config.luckPerms.showPrefixes, config.luckPerms.showSuffixes,
                config.luckPerms.sortTabByWeight, config.luckPerms.higherWeightFirst);
    }

    private static String ftbTeamsJson(VChatTabConfig config) {
        return """
                // Необязательная интеграция с FTB Teams.
                // Если FTB Teams не установлен, VChat продолжит работать без hover-подсказки.
                "ftbTeams": {
                  // Показывать сведения о команде при наведении курсора на ник в чате.
                  "showTeamOnNameHover": %s,
                  // Отдельные строки подсказки. Название и роль сохраняют оформление FTB Teams.
                  "showTeamName": %s,
                  "showPlayerRank": %s,
                  "showMemberCount": %s,
                  // true: у игроков без общей/party-команды подсказки не будет.
                  // false: будет показан текст noTeamText.
                  "hideHoverWithoutTeam": %s,
                  // Цвета и стили подписей поддерживают &-коды и HEX.
                  "teamLabel": %s,
                  "rankLabel": %s,
                  "membersLabel": %s,
                  "noTeamText": %s
                }
                """.formatted(config.ftbTeams.showTeamOnNameHover,
                config.ftbTeams.showTeamName, config.ftbTeams.showPlayerRank,
                config.ftbTeams.showMemberCount, config.ftbTeams.hideHoverWithoutTeam,
                json(config.ftbTeams.teamLabel), json(config.ftbTeams.rankLabel),
                json(config.ftbTeams.membersLabel), json(config.ftbTeams.noTeamText));
    }

    private static String deathMessagesJson(VChatTabConfig config) {
        return """
                // Обработка ванильных сообщений о смерти игроков.
                "deathMessages": {
                  // Главный переключатель обработки death-компонентов через VChat.
                  "enabled": %s,
                  // Убирает головы Chat Heads только у сообщений смерти.
                  // Текст, перевод, причина смерти и hover предмета сохраняются.
                  "hidePlayerHeads": %s
                }
                """.formatted(config.deathMessages.enabled, config.deathMessages.hidePlayerHeads);
    }

    private static String stagesJson(VChatTabConfig config) {
        return """
                // Текущий этап развития игрока по главам квестов FTB Quests.
                "stages": {
                  // Главный переключатель показа этапа. Без FTB Quests на сервере
                  // этап просто не определяется, мод работает как раньше.
                  "enabled": %s,
                  // Как определять текущий этап:
                  // "started" — последняя начатая глава (рекомендуется; новый игрок получает первый этап);
                  // "completed" — только последняя глава, выполненная на 100%%.
                  "detectionMode": %s,
                  // true: тег этапа автоматически дописывается в конец <suffix>
                  // в TAB и чате. false: этап можно вывести вручную через <stage>.
                  "appendToSuffix": %s,
                  // Разделитель между суффиксом LuckPerms и тегом этапа.
                  "separator": %s,
                  // Рекомендуемый точный способ: суффикс выдаётся после выполнения конкретного квеста.
                  // ID берётся из строки id: "..." самого квеста в файле главы .snbt.
                  // Не путайте его с первым id файла — это ID главы. Пример элемента:
                  // { "questId": "ВАШ_ID_ИЗ_SNBT", "tag": "&7Stone Age" }
                  // Порядок: от раннего этапа к позднему; побеждает последний выполненный квест.
                  // Когда список quests заполнен, старый список chapters ниже не используется.
                  "quests": %s,
                  // Резервный старый способ определения по целой главе FTB Quests.
                  // Список глав сверху вниз: от ранних к поздним. Игроку показывается
                  // последняя подходящая глава согласно detectionMode.
                  // "chapter" - имя файла главы без расширения .snbt,
                  // "tag" - вывод, поддерживает &-цвета и HEX.
                  "chapters": %s
                }
                """.formatted(config.stages.enabled, json(config.stages.detectionMode), config.stages.appendToSuffix,
                json(config.stages.separator), GSON.toJson(config.stages.quests),
                GSON.toJson(config.stages.chapters));
    }

    private static String discordJson(VChatTabConfig config) {
        return """
                // Интеграция с Discord: webhook-и и бот для моста чата.
                // Заменяет отдельный мод MC Chat Link.
                "discord": {
                  // Главный переключатель всей интеграции с Discord.
                  "enabled": %s,
                  // Релеить глобальный чат и вход/выход игроков в Discord.
                  "relayChatToDiscord": %s,
                  // Webhook, куда идут глобальный чат и вход/выход.
                  "chatWebhookUrl": %s,
                  // Webhook для статуса сервера (запуск/остановка).
                  "statusWebhookUrl": %s,
                  // Релеить статус сервера.
                  "relayServerStatus": %s,
                  // Имя сервера для placeholder {server}.
                  "serverName": %s,
                  // Имя и аватар, под которыми публикуют webhook-и.
                  "webhookUsername": %s,
                  "webhookAvatarUrl": %s,
                  // Формат сообщения чата в Discord. Placeholders: {player}, {message}, {server}.
                  "gameToDiscordFormat": %s,
                  // Форматы уведомлений. Placeholder: {player} / {server}.
                  "joinFormat": %s,
                  "leaveFormat": %s,
                  "serverStartedFormat": %s,
                  "serverStoppedFormat": %s,

                  // Бот: Discord -> игра. Требует токен бота и MESSAGE CONTENT INTENT.
                  "botEnabled": %s,
                  "botToken": %s,
                  "botChannelId": %d,
                  // Показывать сообщения из Discord в игре.
                  "relayDiscordToGame": %s,
                  // Формат сообщения из Discord в игре. Placeholders: {username}, {message}.
                  "discordToGameFormat": %s
                }
                """.formatted(config.discord.enabled, config.discord.relayChatToDiscord,
                json(config.discord.chatWebhookUrl), json(config.discord.statusWebhookUrl),
                config.discord.relayServerStatus, json(config.discord.serverName),
                json(config.discord.webhookUsername), json(config.discord.webhookAvatarUrl),
                json(config.discord.gameToDiscordFormat),
                json(config.discord.joinFormat), json(config.discord.leaveFormat),
                json(config.discord.serverStartedFormat), json(config.discord.serverStoppedFormat),
                config.discord.botEnabled, json(config.discord.botToken),
                Math.max(0, config.discord.botChannelId), config.discord.relayDiscordToGame,
                json(config.discord.discordToGameFormat));
    }

    private static String announcementsJson(VChatTabConfig config) {
        return """
                // Автоматические объявления в чат.
                "announcements": {
                  // Главный переключатель автоматических объявлений.
                  "enabled": %s,
                  // Как часто показывать объявление, в секундах. Диапазон 60-36000.
                  "intervalSeconds": %d,
                  // Список фраз, показываются в случайном порядке без повторов подряд.
                  // Поддерживают &-цвета и кликабельные ссылки [текст](https://url).
                  "messages": %s
                }
                """.formatted(config.announcements.enabled,
                Math.max(60, Math.min(36000, config.announcements.intervalSeconds)),
                GSON.toJson(config.announcements.messages));
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
        if (instance.ftbTeams == null) instance.ftbTeams = new FTBTeamsSettings();
        if (instance.deathMessages == null) instance.deathMessages = new DeathMessageSettings();
        if (instance.stages == null) instance.stages = new StagesSettings();
        if (instance.stages.detectionMode == null
                || (!instance.stages.detectionMode.equalsIgnoreCase("started")
                && !instance.stages.detectionMode.equalsIgnoreCase("completed"))) {
            instance.stages.detectionMode = "started";
        } else {
            instance.stages.detectionMode = instance.stages.detectionMode.toLowerCase(java.util.Locale.ROOT);
        }
        if (instance.stages.quests == null) instance.stages.quests = new ArrayList<>();
        instance.stages.quests.removeIf(entry -> entry == null
                || entry.questId == null || entry.questId.isBlank()
                || entry.tag == null || entry.tag.isBlank());
        if (instance.stages.chapters == null) instance.stages.chapters = new ArrayList<>();
        instance.stages.chapters.removeIf(entry -> entry == null
                || entry.chapter == null || entry.chapter.isBlank()
                || entry.tag == null || entry.tag.isBlank());
        if (instance.stages.separator == null) instance.stages.separator = " ";
        if (instance.discord == null) instance.discord = new DiscordSettings();
        DiscordSettings defaultDiscord = new DiscordSettings();
        if (instance.discord.chatWebhookUrl == null) instance.discord.chatWebhookUrl = "";
        if (instance.discord.statusWebhookUrl == null) instance.discord.statusWebhookUrl = "";
        if (instance.discord.serverName == null || instance.discord.serverName.isBlank()) {
            instance.discord.serverName = defaultDiscord.serverName;
        }
        if (instance.discord.webhookUsername == null || instance.discord.webhookUsername.isBlank()) {
            instance.discord.webhookUsername = defaultDiscord.webhookUsername;
        }
        if (instance.discord.webhookAvatarUrl == null) instance.discord.webhookAvatarUrl = "";
        if (instance.discord.gameToDiscordFormat == null) {
            instance.discord.gameToDiscordFormat = defaultDiscord.gameToDiscordFormat;
        }
        if (instance.discord.joinFormat == null) instance.discord.joinFormat = defaultDiscord.joinFormat;
        if (instance.discord.leaveFormat == null) instance.discord.leaveFormat = defaultDiscord.leaveFormat;
        if (instance.discord.serverStartedFormat == null) {
            instance.discord.serverStartedFormat = defaultDiscord.serverStartedFormat;
        }
        if (instance.discord.serverStoppedFormat == null) {
            instance.discord.serverStoppedFormat = defaultDiscord.serverStoppedFormat;
        }
        if (instance.discord.botToken == null) instance.discord.botToken = "";
        instance.discord.botChannelId = Math.max(0, instance.discord.botChannelId);
        if (instance.discord.discordToGameFormat == null) {
            instance.discord.discordToGameFormat = defaultDiscord.discordToGameFormat;
        }
        if (instance.announcements == null) instance.announcements = new AnnouncementsSettings();
        if (instance.announcements.messages == null) instance.announcements.messages = new ArrayList<>();
        instance.announcements.messages.removeIf(message -> message == null || message.isBlank());
        instance.announcements.intervalSeconds = Math.max(60, Math.min(36000, instance.announcements.intervalSeconds));
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
        instance.tab.updateIntervalTicks = Math.max(1, instance.tab.updateIntervalTicks);
        instance.chat.localRadius = Math.max(0, instance.chat.localRadius);
        instance.chat.antiSpam.maxMessageLength = Math.max(1, instance.chat.antiSpam.maxMessageLength);
        instance.chat.antiSpam.cooldownMillis = Math.max(0, instance.chat.antiSpam.cooldownMillis);
        instance.chat.antiSpam.repeatWindowSeconds = Math.max(0, instance.chat.antiSpam.repeatWindowSeconds);
        instance.chat.ignore.commandCooldownMillis = Math.max(0, instance.chat.ignore.commandCooldownMillis);
        instance.chat.ignore.saveIntervalMillis = Math.max(50, instance.chat.ignore.saveIntervalMillis);
        if (!Float.isFinite(instance.chat.mentions.volume)) instance.chat.mentions.volume = 0.8F;
        if (!Float.isFinite(instance.chat.mentions.pitch)) instance.chat.mentions.pitch = 1.2F;
        instance.chat.mentions.volume = Math.max(0.0F, Math.min(4.0F, instance.chat.mentions.volume));
        instance.chat.mentions.pitch = Math.max(0.01F, Math.min(2.0F, instance.chat.mentions.pitch));

        TabSettings defaultTab = new TabSettings();
        if (instance.tab.header == null) instance.tab.header = defaultTab.header;
        if (instance.tab.footer == null) instance.tab.footer = defaultTab.footer;
        if (instance.tab.joinMessage == null) instance.tab.joinMessage = defaultTab.joinMessage;
        if (instance.tab.firstJoinMessage == null) instance.tab.firstJoinMessage = defaultTab.firstJoinMessage;
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
        if (instance.chat.antiSpam.emptyMessage == null) {
            instance.chat.antiSpam.emptyMessage = defaultAntiSpam.emptyMessage;
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
        if (instance.chat.ignore.cooldownMessage == null) {
            instance.chat.ignore.cooldownMessage = defaultIgnore.cooldownMessage;
        }

        FTBTeamsSettings defaultFtbTeams = new FTBTeamsSettings();
        if (instance.ftbTeams.teamLabel == null) instance.ftbTeams.teamLabel = defaultFtbTeams.teamLabel;
        if (instance.ftbTeams.rankLabel == null) instance.ftbTeams.rankLabel = defaultFtbTeams.rankLabel;
        if (instance.ftbTeams.membersLabel == null) instance.ftbTeams.membersLabel = defaultFtbTeams.membersLabel;
        if (instance.ftbTeams.noTeamText == null) instance.ftbTeams.noTeamText = defaultFtbTeams.noTeamText;
    }

    private static void validate() {
        if (!instance.chat.globalCommand.matches("[a-z0-9_:-]{1,32}")) {
            throw new IllegalArgumentException("chat.globalCommand must contain only a-z, 0-9, _, : or -");
        }
        if (instance.tab.header.length() > 32767 || instance.tab.footer.length() > 32767
                || instance.chat.globalFormat.length() > 32767
                || instance.chat.localFormat.length() > 32767) {
            throw new IllegalArgumentException("A VChat format string is too long");
        }
        for (StageQuest stage : instance.stages.quests) {
            if (!stage.questId.matches("(?i)(?:0x)?[0-9a-f]{1,16}")) {
                throw new IllegalArgumentException("stages.quests questId must be a hexadecimal FTB Quest ID: "
                        + stage.questId);
            }
        }
    }

    private static void upgradeOldDefaults(VChatTabConfig config) {
        TabSettings newDefaults = new TabSettings();
        String oldHeader = "\n&6&l&nVChat\n\n&7Игроки: &a%online%\n\n&7&m-----------------";
        String oldFooter = "&7&m-----------------\n\n&7Баланс: &e0";
        String oldJoinMessage = "&aДобро пожаловать на &6&l&nVChat&a!";
        String oldValorCraftJoinMessage = "&aДобро пожаловать на &6&lValorCraft&a!";
        if (oldHeader.equals(config.tab.header)) {
            config.tab.header = newDefaults.header;
        }
        if (oldFooter.equals(config.tab.footer)) {
            config.tab.footer = newDefaults.footer;
        }
        if (oldJoinMessage.equals(config.tab.joinMessage)
                || oldValorCraftJoinMessage.equals(config.tab.joinMessage)
                || (config.tab.joinMessage != null && config.tab.joinMessage.contains("VChat"))) {
            config.tab.joinMessage = newDefaults.joinMessage;
        }
        if ("&e[G] &7<name>: &f<message>".equals(config.chat.globalFormat)) {
            config.chat.globalFormat = new ChatSettings().globalFormat;
        }
        if ("&7[L] &7<name>: &f<message>".equals(config.chat.localFormat)) {
            config.chat.localFormat = new ChatSettings().localFormat;
        }
    }

    /**
     * One-time import of the old MC Chat Link config (mcchatlink-server.toml)
     * into the new "discord" section, run only when that section was missing.
     */
    private static void migrateMcChatLinkToml() {
        Path toml = configDir.resolve("mcchatlink-server.toml");
        if (!Files.exists(toml) && VChatPaths.isManagedDirectory(configDir)) {
            // Read the other mod's old config in place; never copy it into or
            // modify VMods/VChat.
            toml = VChatPaths.legacyForgeConfigDirectory().resolve("mcchatlink-server.toml");
        }
        if (!Files.exists(toml)) return;

        String section = "";
        try {
            var lines = Files.readAllLines(toml, StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    section = trimmed.substring(1, trimmed.length() - 1);
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) continue;
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                value = value.replaceAll("^\"|\"$", "");
                applyMcChatLinkValue(section, key, value);
            }
            LOGGER.info("Imported Discord settings from the old MC Chat Link config: {}", toml);
        } catch (Exception e) {
            LOGGER.warn("Could not import MC Chat Link Discord settings from {}", toml, e);
        }
    }

    private static void applyMcChatLinkValue(String section, String key, String value) {
        if (instance.discord == null) instance.discord = new DiscordSettings();
        if ("discord".equals(section)) {
            switch (key) {
                case "chatWebhookUrl" -> instance.discord.chatWebhookUrl = value;
                case "statusWebhookUrl" -> instance.discord.statusWebhookUrl = value;
                case "serverName" -> {
                    if (!value.isBlank()) instance.discord.serverName = value;
                }
                case "relayServerStatus" -> instance.discord.relayServerStatus = parseBool(value,
                        instance.discord.relayServerStatus);
                case "webhookUsername" -> {
                    if (!value.isBlank()) instance.discord.webhookUsername = value;
                }
                case "webhookAvatarUrl" -> instance.discord.webhookAvatarUrl = value;
                case "relayChatToDiscord" -> instance.discord.relayChatToDiscord = parseBool(value,
                        instance.discord.relayChatToDiscord);
                case "gameToDiscordFormat" -> {
                    if (!value.isBlank()) instance.discord.gameToDiscordFormat = value;
                }
                default -> {
                }
            }
        } else if ("bot".equals(section)) {
            switch (key) {
                case "botEnabled" -> instance.discord.botEnabled = parseBool(value,
                        instance.discord.botEnabled);
                case "botToken" -> {
                    if (!value.isBlank()) instance.discord.botToken = value;
                }
                case "botChannelId" -> {
                    try {
                        instance.discord.botChannelId = Long.parseLong(value);
                    } catch (NumberFormatException ignored) {
                    }
                }
                case "relayDiscordToGame" -> instance.discord.relayDiscordToGame = parseBool(value,
                        instance.discord.relayDiscordToGame);
                case "discordToGameFormat" -> {
                    if (!value.isBlank()) instance.discord.discordToGameFormat = value;
                }
                default -> {
                }
            }
        }
    }

    private static boolean parseBool(String value, boolean fallback) {
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        return fallback;
    }

    public static final class TabSettings {
        public String header = "\n&6&lValorCraft\n&7Игроки: &f%online%&8/&7%max%\n";
        public String footer = "\n&8valorcraft.ru\n";
        public String joinMessage = "Добро пожаловать на ValorCraft!";
        public String firstJoinMessage = "Игрок &f<name> &7присоединился к серверу впервые. Приветствуем!";
        public String playerFormat = "<prefix>&f<name><suffix>&r";
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
        public int cooldownMillis = 500;
        public boolean blockRepeatedMessages = true;
        public int repeatWindowSeconds = 15;
        public String tooLongMessage = "&cСообщение слишком длинное. Максимум: <max> символов";
        public String tooFastMessage = "&cНе так быстро. Подождите ещё <seconds> сек.";
        public String repeatedMessage = "&cНе повторяйте одно и то же сообщение";
        public String emptyMessage = "&cСообщение не может быть пустым";
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
        public int commandCooldownMillis = 1000;
        public int saveIntervalMillis = 1000;
        public String cooldownMessage = "&cНе так быстро. Подождите перед повторным изменением списка";
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

    public static final class FTBTeamsSettings {
        public boolean showTeamOnNameHover = true;
        public boolean showTeamName = true;
        public boolean showPlayerRank = true;
        public boolean showMemberCount = true;
        public boolean hideHoverWithoutTeam = true;
        public String teamLabel = "&7Команда: &f";
        public String rankLabel = "&7Роль: &f";
        public String membersLabel = "&7Участников: &f";
        public String noTeamText = "&7Игрок не состоит в команде";
    }

    public static final class DeathMessageSettings {
        public boolean enabled = true;
        public boolean hidePlayerHeads = true;
    }

    public static final class StagesSettings {
        // Главный переключатель показа этапа развития игрока.
        public boolean enabled = true;
        // "started" = текущая начатая глава; "completed" = только полностью завершённая.
        public String detectionMode = "started";
        // Автоматически добавлять тег этапа в конец <suffix> в TAB и чате.
        public boolean appendToSuffix = true;
        // Разделитель между суффиксом LuckPerms и тегом этапа.
        public String separator = " ";
        // Точные квесты-триггеры. Если список не пуст, chapters не используется.
        public List<StageQuest> quests = new ArrayList<>();
        // Старый резервный способ: определение этапа по главе целиком.
        public List<StageChapter> chapters = new ArrayList<>(List.of(
                new StageChapter("questsstoneage", "&7Stone Age"),
                new StageChapter("questsmetallurgy", "&6Metallurgy"),
                new StageChapter("questssteam_age", "&7Steam"),
                new StageChapter("lv__low_voltage", "&aLV"),
                new StageChapter("mv__medium_voltage", "&bMV"),
                new StageChapter("hv__high_voltage", "&eHV"),
                new StageChapter("ev__extreme_voltage", "&dEV"),
                new StageChapter("iv__insane_voltage", "&5IV"),
                new StageChapter("luv__ludicrous_voltage", "&dLuV"),
                new StageChapter("zpm__zero_point_module", "&fZPM"),
                new StageChapter("uv__ultimate_voltage", "&cUV")
        ));
    }

    public static final class StageQuest {
        // 16-значный HEX ID конкретного квеста из файла главы FTB Quests.
        public String questId;
        // Суффикс после выполнения этого квеста. Поддерживает &-цвета и HEX.
        public String tag;

        public StageQuest() {
        }

        public StageQuest(String questId, String tag) {
            this.questId = questId;
            this.tag = tag;
        }
    }

    public static final class StageChapter {
        // Имя файла главы FTB Quests без расширения .snbt.
        public String chapter;
        // Текст, который будет показан игроку. Поддерживает &-цвета и HEX.
        public String tag;

        public StageChapter() {
        }

        public StageChapter(String chapter, String tag) {
            this.chapter = chapter;
            this.tag = tag;
        }
    }

    public static final class DiscordSettings {
        // Главный переключатель всей интеграции с Discord.
        public boolean enabled = true;
        // Релеить глобальный чат и вход/выход игроков в Discord.
        public boolean relayChatToDiscord = true;
        // Webhook, куда идут глобальный чат и сообщения о входе/выходе.
        public String chatWebhookUrl = "";
        // Webhook для статуса сервера (запуск/остановка).
        public String statusWebhookUrl = "";
        // Релеить статус сервера.
        public boolean relayServerStatus = true;
        // Имя сервера для placeholder {server}.
        public String serverName = "ValorCraft";
        // Имя и аватар, под которыми публикуют webhook-ы.
        public String webhookUsername = "ValorCraft";
        public String webhookAvatarUrl = "";
        // Формат сообщения чата в Discord. Placeholders: {player}, {message}, {server}.
        public String gameToDiscordFormat = "**{player}**: {message}";
        // Форматы уведомлений. Placeholder: {player} / {server}.
        public String joinFormat = "**{player}** вошёл на сервер";
        public String leaveFormat = "**{player}** вышел с сервера";
        public String serverStartedFormat = "🟢 Сервер запущен | {server}";
        public String serverStoppedFormat = "🔴 Сервер остановлен | {server}";

        // Бот: Discord -> игра. Требует токен бота и MESSAGE CONTENT INTENT.
        public boolean botEnabled = false;
        public String botToken = "";
        public long botChannelId = 0;
        // Показывать сообщения из Discord в игре.
        public boolean relayDiscordToGame = true;
        // Формат сообщения из Discord в игре. Placeholders: {username}, {message}.
        public String discordToGameFormat = "&8[Discord] &7{username}&8: &f{message}";
    }

    public static final class AnnouncementsSettings {
        // Главный переключатель автоматических объявлений.
        public boolean enabled = true;
        // Как часто показывать объявление, в секундах. Диапазон 60-36000.
        public int intervalSeconds = 600;
        // Список фраз, показываются в случайном порядке без повторов подряд.
        // Поддерживают &-цвета и кликабельные ссылки [текст](https://url).
        public List<String> messages = new ArrayList<>(List.of(
                "&eСайт сервера: &f[valorcraft.ru](https://valorcraft.ru) &7| &f[Правила](https://valorcraft.ru/rules)",
                "&eDiscord: &f[discord.gg](https://discord.gg/mzCtnkJA7S)",
                "&eНужна помощь? &fЗадай вопрос администрации через &a/ask"));
    }
}
