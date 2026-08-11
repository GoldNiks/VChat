package com.vchat;

import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Optional FTB Quests integration without a hard runtime dependency.
 * Resolves a player's current development stage: the most advanced quest
 * chapter (by config order) that is fully completed.
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
    private static Object cachedFile;
    private static Map<String, Object> chaptersByFilename = Map.of();

    private FTBQuestsStageBridge() {
    }

    /**
     * Returns the configured tag of the player's current stage, or "" when
     * FTB Quests is unavailable, the system is disabled or no stage chapter
     * is completed yet.
     */
    public static String stageText(ServerPlayer player) {
        // The system only ever writes into the suffix and <stage>; when it is
        // disabled, skip all reflection work entirely.
        if (!VChatTabConfig.stagesEnabled()) return "";
        try {
            Object file = questFile();
            Object teamData = teamData(file, player);
            if (file == null || teamData == null) return "";
            for (VChatTabConfig.StageChapter stage : VChatTabConfig.stageChapters()) {
                Object chapter = chapter(file, stage.chapter);
                if (chapter != null && Boolean.TRUE.equals(isCompletedRaw.invoke(chapter, teamData))) {
                    return stage.tag;
                }
            }
            return "";
        } catch (ReflectiveOperationException error) {
            logFailure(error);
            return "";
        }
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
            teamDataGet = Class.forName(TEAM_DATA_CLASS).getMethod("get", ServerPlayer.class);
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