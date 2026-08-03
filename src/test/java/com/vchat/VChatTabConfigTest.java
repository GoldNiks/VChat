package com.vchat;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VChatTabConfigTest {
    @Test
    void rejectsBrokenReloadAndKeepsLastWorkingConfig() throws Exception {
        Path directory = Files.createTempDirectory("vchat-config-test-");
        assertTrue(VChatTabConfig.reload(directory));
        assertEquals(500, VChatTabConfig.cooldownMillis());

        Path config = directory.resolve("vchat-config.json5");
        String generated = Files.readString(config);
        assertTrue(generated.contains("\"configVersion\": 8"));
        assertTrue(generated.contains("\"saveIntervalMillis\": 1000"));
        assertTrue(Files.exists(directory.resolve("vchat-config.json5.last-good")));

        String versionSeven = generated
                .replace("\"configVersion\": 8", "\"configVersion\": 7")
                .replace("\"cooldownMillis\": 500", "\"cooldownMillis\": 1000");
        Files.writeString(config, versionSeven);
        assertTrue(VChatTabConfig.reload(directory));
        assertEquals(500, VChatTabConfig.cooldownMillis());

        Files.writeString(config, "{ broken json");
        assertFalse(VChatTabConfig.reload(directory));
        assertEquals(500, VChatTabConfig.cooldownMillis());
    }
}
