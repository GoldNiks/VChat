package com.vchat;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VChatPathsTest {
    @Test
    void migratesOnlyVChatFilesWithoutTouchingOtherModsOrExistingFiles() throws Exception {
        Path root = Files.createTempDirectory("vchat-paths-test-");
        Path legacy = root.resolve("config");
        Path misplaced = root.resolve("VMods").resolve("VChat");
        Path target = legacy.resolve("VMods").resolve("VChat");
        Path otherModFile = legacy.resolve("VMods").resolve("VTab").resolve("settings.json5");
        Files.createDirectories(legacy);
        Files.createDirectories(target);
        Files.createDirectories(misplaced);
        Files.createDirectories(otherModFile.getParent());

        Files.writeString(legacy.resolve("vchat-config.json5"), "old-config");
        Files.writeString(legacy.resolve("vchat-ignore.json"), "old-ignore");
        Files.writeString(misplaced.resolve("vchat-ignore.json"), "latest-ignore");
        Files.writeString(legacy.resolve("unrelated-mod.toml"), "leave-me");
        Files.writeString(target.resolve("vchat-config.json5"), "new-config");
        Files.writeString(otherModFile, "other-mod");

        assertEquals(target, VChatPaths.prepareConfigDirectory(target, List.of(misplaced, legacy)));
        assertEquals("new-config", Files.readString(target.resolve("vchat-config.json5")));
        assertEquals("latest-ignore", Files.readString(target.resolve("vchat-ignore.json")));
        assertEquals("other-mod", Files.readString(otherModFile));
        assertTrue(Files.exists(legacy.resolve("vchat-config.json5")));
        assertFalse(Files.exists(target.resolve("unrelated-mod.toml")));

        Files.delete(target.resolve("vchat-ignore.json"));
        VChatPaths.prepareConfigDirectory(target, List.of(misplaced, legacy));
        assertFalse(Files.exists(target.resolve("vchat-ignore.json")),
                "completed migration must not resurrect deliberately removed files");
    }
}
