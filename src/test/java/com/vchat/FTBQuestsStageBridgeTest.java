package com.vchat;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FTBQuestsStageBridgeTest {

    private static VChatTabConfig.StageChapter chapter(String name, String tag) {
        return new VChatTabConfig.StageChapter(name, tag);
    }

    private static final List<VChatTabConfig.StageChapter> EARLY_TO_LATE = List.of(
            chapter("questsstoneage", "&7Stone Age"),
            chapter("questsmetallurgy", "&6Metallurgy"),
            chapter("questssteam_age", "&7Steam"),
            chapter("lv__low_voltage", "&aLV"),
            chapter("mv__medium_voltage", "&bMV"),
            chapter("hv__high_voltage", "&eHV"),
            chapter("ev__extreme_voltage", "&dEV"),
            chapter("iv__insane_voltage", "&5IV"),
            chapter("luv__ludicrous_voltage", "&dLuV"),
            chapter("zpm__zero_point_module", "&fZPM"),
            chapter("uv__ultimate_voltage", "&cUV")
    );

    @Test
    void mostAdvancedCompletedChapterWins() {
        assertEquals("&bMV", FTBQuestsStageBridge.selectStageTag(EARLY_TO_LATE,
                name -> name.equals("questsstoneage") || name.equals("lv__low_voltage")
                        || name.equals("mv__medium_voltage")));
    }

    @Test
    void nothingCompletedReturnsEmpty() {
        assertEquals("", FTBQuestsStageBridge.selectStageTag(EARLY_TO_LATE, name -> false));
    }

    @Test
    void onlyEarliestCompletedReturnsIt() {
        assertEquals("&7Stone Age", FTBQuestsStageBridge.selectStageTag(EARLY_TO_LATE,
                name -> name.equals("questsstoneage")));
    }

    @Test
    void allCompletedReturnsLast() {
        assertEquals("&cUV", FTBQuestsStageBridge.selectStageTag(EARLY_TO_LATE, name -> true));
    }

    @Test
    void unknownChapterNamesAreIgnored() {
        assertEquals("", FTBQuestsStageBridge.selectStageTag(EARLY_TO_LATE,
                name -> name.equals("not_a_chapter")));
    }

    @Test
    void emptyListReturnsEmpty() {
        assertEquals("", FTBQuestsStageBridge.selectStageTag(List.of(), name -> true));
    }
}
