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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class IgnoreManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type DATA_TYPE = new TypeToken<Map<String, Set<String>>>() { }.getType();
    private static Map<String, Set<String>> ignoredPlayers = new HashMap<>();
    private static Path dataFile = Path.of("config", "vchat-ignore.json");

    private IgnoreManager() {
    }

    public static synchronized void configure(Path configDir) {
        dataFile = configDir.resolve("vchat-ignore.json");
        ignoredPlayers = new HashMap<>();
        if (!Files.exists(dataFile)) return;

        try (Reader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
            Map<String, Set<String>> loaded = GSON.fromJson(reader, DATA_TYPE);
            if (loaded != null) ignoredPlayers = loaded;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized boolean toggle(UUID owner, UUID target) {
        Set<String> ignored = ignoredPlayers.computeIfAbsent(owner.toString(), key -> new HashSet<>());
        boolean nowIgnored;
        if (ignored.remove(target.toString())) {
            nowIgnored = false;
        } else {
            ignored.add(target.toString());
            nowIgnored = true;
        }
        if (ignored.isEmpty()) ignoredPlayers.remove(owner.toString());
        save();
        return nowIgnored;
    }

    public static synchronized boolean isIgnoring(UUID recipient, UUID sender) {
        Set<String> ignored = ignoredPlayers.get(recipient.toString());
        return ignored != null && ignored.contains(sender.toString());
    }

    public static synchronized int ignoredCount(UUID owner) {
        Set<String> ignored = ignoredPlayers.get(owner.toString());
        return ignored == null ? 0 : ignored.size();
    }

    public static synchronized int clear(UUID owner) {
        Set<String> removed = ignoredPlayers.remove(owner.toString());
        int count = removed == null ? 0 : removed.size();
        if (count > 0) save();
        return count;
    }

    private static void save() {
        try {
            Files.createDirectories(dataFile.getParent());
            Path temporary = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");
            Files.writeString(temporary, GSON.toJson(ignoredPlayers, DATA_TYPE), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception ignored) {
                Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
