package com.vchat;

import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Optional FTB Quests integration without a hard runtime dependency.
 * Resolves a player's current development stage from configured quest chapters.
 */
public final class FTBQuestsStageBridge {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("VChat");
    private static final String QUEST_FILE_CLASS = "dev.ftb.mods.ftbquests.quest.ServerQuestFile";
    private static final String CHAPTER_CLASS = "dev.ftb.mods.ftbquests.quest.Chapter";
    private static final String QUEST_OBJECT_CLASS = "dev.ftb.mods.ftbquests.quest.QuestObject";
    private static final String TEAM_DATA_CLASS = "dev.ftb.mods.ftbquests.quest.TeamData";
    private static boolean classMissing;
    private static boolean reflectionFailureLogged;
    private static Field instanceField;
    private static Method teamDataGet;
    private static Method getAllChapters;
    private static Method getFilename;
    private static Method isCompletedRaw;
    private static Method isStarted;
    private static Object cachedFile;
    private static Map<String, Object> chaptersByFilename = Map.of();

    private FTBQuestsStageBridge() {
    }

    /**
     * Returns the configured tag of the player's current stage, or "" when
     * FTB Quests is unavailable, the system is disabled or no stage chapter
     * matches the configured detection mode yet.
     */
    public static String stageText(ServerPlayer player) {
        // The system only ever writes into the suffix and <stage>; when it is
        // disabled, skip all reflection work entirely.
        if (!VChatTabConfig.stagesEnabled()) return "";
        try {
            Object file = questFile();
            Object teamData = teamData(file, player);
            if (file == null || teamData == null) return "";
            List<VChatTabConfig.StageChapter> configured = VChatTabConfig.stageChapters();
            boolean startedMode = VChatTabConfig.stageDetectionMode().equals("started");
            String selected = selectStageTag(configured, chapterName -> {
                try {
                    Object chapter = chapter(file, chapterName);
                    return chapter != null
                            && Boolean.TRUE.equals((startedMode ? isStarted : isCompletedRaw)
                            .invoke(chapter, teamData));
                } catch (ReflectiveOperationException error) {
                    logFailure(error);
                    return false;
                }
            });
            if (selected.isEmpty() && startedMode && !configured.isEmpty()
                    && chapter(file, configured.get(0).chapter) != null) {
                return configured.get(0).tag;
            }
            return selected;
        } catch (ReflectiveOperationException error) {
            logFailure(error);
            return "";
        }
    }

    /**
     * Returns the tag of the most advanced completed chapter: the chapters are
     * ordered from early to late, so the list is scanned from the end.
     */
    static String selectStageTag(List<VChatTabConfig.StageChapter> chapters,
                                  java.util.function.Predicate<String> chapterCompleted) {
        for (int i = chapters.size() - 1; i >= 0; i--) {
            VChatTabConfig.StageChapter stage = chapters.get(i);
            if (stage.chapter != null && chapterCompleted.test(stage.chapter)) {
                return stage.tag;
            }
        }
        return "";
    }

    private static Object questFile() throws ReflectiveOperationException {
        if (classMissing) return null;
        if (instanceField == null) {
            try {
                instanceField = Class.forName(QUEST_FILE_CLASS).getField("INSTANCE");
            } catch (ClassNotFoundException e) {
                classMissing = true;
                return null;
            }
        }
        return instanceField.get(null);
    }

    private static Object teamData(Object file, ServerPlayer player) throws ReflectiveOperationException {
        if (file == null) return null;
        if (teamDataGet == null) {
            // FTB Quests 2001.x (1.20.1): TeamData.get(Player) — note that it
            // takes the base Player class, not ServerPlayer.
            teamDataGet = Class.forName(TEAM_DATA_CLASS).getMethod("get",
                    Class.forName("net.minecraft.world.entity.player.Player"));
        }
        return teamDataGet.invoke(null, player);
    }

    private static Object chapter(Object file, String filename) throws ReflectiveOperationException {
        if (file == null || filename == null || filename.isBlank()) return null;
        ensureChapters(file);
        if (chaptersByFilename.containsKey(filename)) return chaptersByFilename.get(filename);
        return null;
    }

    private static void ensureChapters(Object file) throws ReflectiveOperationException {
        if (cachedFile == file) return;
        if (getAllChapters == null) getAllChapters = file.getClass().getMethod("getAllChapters");
        if (getFilename == null) getFilename = Class.forName(CHAPTER_CLASS).getMethod("getFilename");
        if (isCompletedRaw == null) {
            isCompletedRaw = Class.forName(QUEST_OBJECT_CLASS)
                    .getMethod("isCompletedRaw", Class.forName(TEAM_DATA_CLASS));
        }
        if (isStarted == null) {
            isStarted = Class.forName(TEAM_DATA_CLASS)
                    .getMethod("isStarted", Class.forName(QUEST_OBJECT_CLASS));
        }
        Collection<?> chapters = (Collection<?>) getAllChapters.invoke(file);
        Map<String, Object> rebuilt = new HashMap<>();
        if (chapters != null) {
            for (Object chapter : chapters) {
                String chapterName = (String) getFilename.invoke(chapter);
                if (chapterName != null && !chapterName.isBlank()) {
                    rebuilt.put(chapterName, chapter);
                }
            }
        }
        chaptersByFilename = rebuilt;
        cachedFile = file;
    }

    private static void logFailure(ReflectiveOperationException error) {
        if (!reflectionFailureLogged) {
            reflectionFailureLogged = true;
            LOGGER.warn("FTB Quests stage integration failed; stage tags are temporarily unavailable", error);
        }
    }
}
