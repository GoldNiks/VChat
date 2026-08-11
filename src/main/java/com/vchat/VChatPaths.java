package com.vchat;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/** Owns only VChat's directory inside the shared VMods namespace. */
public final class VChatPaths {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("VChat");
    private static final String MIGRATION_MARKER = ".legacy-config-migrated";
    private static final List<String> LEGACY_FILES = List.of(
            "vchat-config.json5",
            "vchat-config.json5.last-good",
            "vchat-ignore.json",
            "vchat-firstjoin.json",
            "vchat-tab.json"
    );
    private static volatile Path preparedDirectory;

    private VChatPaths() {
    }

    public static Path configDirectory() {
        return FMLPaths.GAMEDIR.get().resolve("VMods").resolve("VChat");
    }

    public static Path legacyForgeConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static Path prepareConfigDirectory() {
        return prepareConfigDirectory(configDirectory(), legacyForgeConfigDirectory());
    }

    static Path prepareConfigDirectory(Path target, Path legacyConfigDirectory) {
        try {
            // Never replace or clear VMods: create only VChat's own directory.
            Files.createDirectories(target);
            Path marker = target.resolve(MIGRATION_MARKER);
            if (!Files.exists(marker)) {
                for (String fileName : LEGACY_FILES) {
                    migrateIfAbsent(legacyConfigDirectory.resolve(fileName), target.resolve(fileName));
                }
                Files.writeString(marker, "Legacy config migration completed. Safe to keep this file.\n");
            }
            preparedDirectory = target.toAbsolutePath().normalize();
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("Could not prepare VChat directory: " + target, e);
        }
    }

    public static boolean isManagedDirectory(Path directory) {
        Path prepared = preparedDirectory;
        return directory != null && prepared != null
                && directory.toAbsolutePath().normalize().equals(prepared);
    }

    private static void migrateIfAbsent(Path source, Path target) throws IOException {
        if (!Files.isRegularFile(source) || Files.exists(target)) return;
        Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
        LOGGER.info("Copied legacy VChat file to {} (old file kept as backup)", target);
    }
}
