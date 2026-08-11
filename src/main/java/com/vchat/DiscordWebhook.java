package com.vchat;

import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sends messages to a Discord webhook without blocking the server thread.
 */
public final class DiscordWebhook {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("VChat");
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    private static final ExecutorService POOL = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable, "VChat-Discord-Webhook");
        thread.setDaemon(true);
        return thread;
    });

    private DiscordWebhook() {
    }

    public static void send(String url, String content, String username, String avatarUrl) {
        if (url == null || url.isBlank()) return;
        CompletableFuture.runAsync(() -> doSend(url, content, username, avatarUrl), POOL);
    }

    public static void sendSync(String url, String content, String username, String avatarUrl) {
        if (url == null || url.isBlank()) return;
        doSend(url, content, username, avatarUrl);
    }

    private static void doSend(String url, String content, String username, String avatarUrl) {
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("content", content == null ? "" : content);
            if (username != null && !username.isEmpty()) payload.addProperty("username", username);
            if (avatarUrl != null && !avatarUrl.isEmpty()) payload.addProperty("avatar_url", avatarUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOGGER.warn("Discord webhook error: {} {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to send Discord webhook", e);
        }
    }
}