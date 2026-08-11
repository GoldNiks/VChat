package com.vchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class FirstJoinManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type DATA_TYPE = new TypeToken<Set<String>>() { }.getType();
    private static Set<String> knownPlayers = new HashSet<>();
    private static Path dataFile = Path.of("config", "VMods", "VChat", "vchat-firstjoin.json");
    private static boolean dirty;
    private static long dirtySinceNanos;

    private FirstJoinManager() {
    }

    public static synchronized void configure(Path configDir) {
        flushNow();
        dataFile = configDir.resolve("vchat-firstjoin.json");
        knownPlayers = new HashSet<>();
        dirty = false;
        if (!Files.exists(dataFile)) return;

        try (Reader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
            Set<String> loaded = GSON.fromJson(reader, DATA_TYPE);
            if (loaded != null) knownPlayers = loaded;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized boolean isFirstJoin(UUID player) {
        return !knownPlayers.contains(player.toString());
    }

    public static synchronized void markJoined(UUID player) {
        if (knownPlayers.add(player.toString())) markDirty();
    }

    public static synchronized void flushIfDue() {
        if (!dirty) return;
        long intervalNanos = VChatTabConfig.ignoreSaveIntervalMillis() * 1_000_000L;
        if (System.nanoTime() - dirtySinceNanos >= intervalNanos) save();
    }

    public static synchronized void flushNow() {
        if (dirty) save();
    }

    private static void markDirty() {
        if (!dirty) dirtySinceNanos = System.nanoTime();
        dirty = true;
    }

    private static void save() {
        try {
            Files.createDirectories(dataFile.getParent());
            Path temporary = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");
            Files.writeString(temporary, GSON.toJson(knownPlayers, DATA_TYPE), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception ignored) {
                Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING);
            }
            dirty = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
