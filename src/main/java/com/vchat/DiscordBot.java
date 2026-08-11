package com.vchat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Discord bot (gateway v10 over java.net.http WebSocket) that relays messages
 * from a Discord channel into the game. No external dependencies.
 */
public final class DiscordBot {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("VChat");
    private static final String GATEWAY_URL = "wss://gateway.discord.gg/?v=10&encoding=json";
    private static final int IDENTIFY_DELAY_SECONDS = 6;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final int INTENTS = 33281; // GUILDS | GUILD_MESSAGES | MESSAGE_CONTENT

    private final String token;
    private final String channelIdStr;
    private final MinecraftServer server;
    private final ScheduledExecutorService scheduler;
    private HttpClient httpClient;
    private volatile WebSocket ws;
    private ScheduledFuture<?> heartbeatTask;
    private volatile int lastSeq;
    private volatile int reconnectAttempts;

    public DiscordBot(String token, long channelId, MinecraftServer server) {
        this.token = token;
        this.channelIdStr = String.valueOf(channelId);
        this.server = server;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "VChat-Discord-Bot");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        scheduler.execute(this::connect);
    }

    public void stop() {
        reconnectAttempts = MAX_RECONNECT_ATTEMPTS;
        stopHeartbeat();
        WebSocket socket = ws;
        if (socket != null) {
            socket.sendClose(1000, "Server shutting down");
            ws = null;
        }
        scheduler.shutdownNow();
    }

    private void connect() {
        try {
            if (httpClient == null) httpClient = HttpClient.newHttpClient();
            httpClient.newWebSocketBuilder()
                    .buildAsync(URI.create(GATEWAY_URL), new BotListener())
                    .thenAccept(socket -> {
                        ws = socket;
                        reconnectAttempts = 0;
                    })
                    .exceptionally(error -> {
                        LOGGER.error("Discord bot: failed to connect", error);
                        scheduleReconnect();
                        return null;
                    });
        } catch (Exception e) {
            LOGGER.error("Discord bot: failed to connect", e);
            scheduleReconnect();
        }
    }

    private void send(JsonObject payload) {
        WebSocket socket = ws;
        if (socket == null) return;
        socket.sendText(payload.toString(), true);
    }

    private void identify() {
        JsonObject properties = new JsonObject();
        properties.addProperty("os", "windows");
        properties.addProperty("browser", "vchat");
        properties.addProperty("device", "vchat");

        JsonObject data = new JsonObject();
        data.addProperty("token", token);
        data.addProperty("intents", INTENTS);
        data.add("properties", properties);

        JsonObject payload = new JsonObject();
        payload.addProperty("op", 2);
        payload.add("d", data);
        send(payload);
    }

    private void sendHeartbeat() {
        JsonObject payload = new JsonObject();
        payload.addProperty("op", 1);
        if (lastSeq > 0) {
            payload.addProperty("d", lastSeq);
        } else {
            payload.add("d", null);
        }
        send(payload);
    }

    private void startHeartbeat(int intervalMillis) {
        stopHeartbeat();
        heartbeatTask = scheduler.scheduleAtFixedRate(this::sendHeartbeat,
                intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    private void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
            heartbeatTask = null;
        }
    }

    private void scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            LOGGER.warn("Discord bot: max reconnect attempts ({}) reached, stopping",
                    MAX_RECONNECT_ATTEMPTS);
            return;
        }
        reconnectAttempts++;
        int delaySeconds = (int) (Math.pow(2, reconnectAttempts) * 5);
        LOGGER.info("Discord bot: reconnecting in {}s (attempt {}/{})",
                delaySeconds, reconnectAttempts, MAX_RECONNECT_ATTEMPTS);
        scheduler.schedule(this::connect, delaySeconds, TimeUnit.SECONDS);
    }

    private void handleDispatch(JsonObject payload) {
        String type = payload.get("t").getAsString();
        if ("READY".equals(type)) {
            String username = payload.getAsJsonObject("d")
                    .getAsJsonObject("user")
                    .get("username").getAsString();
            LOGGER.info("Discord bot: authenticated as '{}'", username);
        } else if ("MESSAGE_CREATE".equals(type)) {
            handleMessageCreate(payload.getAsJsonObject("d"));
        }
    }

    private void handleMessageCreate(JsonObject message) {
        if (!VChatTabConfig.discordRelayDiscordToGame()) return;

        if (!channelIdStr.equals(message.get("channel_id").getAsString())) return;

        JsonElement authorBot = message.getAsJsonObject("author").get("bot");
        if (authorBot != null && authorBot.getAsBoolean()) return;

        String content = message.get("content").getAsString();
        if (content.isEmpty()) return;

        JsonElement globalName = message.getAsJsonObject("author").get("global_name");
        String username;
        if (globalName != null && !globalName.getAsString().isEmpty()) {
            username = globalName.getAsString();
        } else {
            username = message.getAsJsonObject("author").get("username").getAsString();
        }

        String formatted = VChatTabConfig.discordToGameFormat()
                .replace("{username}", username)
                .replace("{message}", content);
        Component component = HexUtil.fromLegacy(formatted);
        server.execute(() -> server.getPlayerList().broadcastSystemMessage(component, false));
    }

    private void handleHello(JsonObject payload) {
        int interval = payload.get("heartbeat_interval").getAsInt();
        startHeartbeat(interval);
        scheduler.schedule(this::identify, IDENTIFY_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private final class BotListener implements WebSocket.Listener {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                String text = buffer.toString();
                buffer.setLength(0);
                try {
                    JsonObject payload = JsonParser.parseString(text).getAsJsonObject();
                    if (payload.has("s") && !payload.get("s").isJsonNull()) {
                        lastSeq = payload.get("s").getAsInt();
                    }
                    int op = payload.get("op").getAsInt();
                    switch (op) {
                        case 0 -> handleDispatch(payload);
                        case 7 -> scheduler.execute(() -> {
                            stopHeartbeat();
                            WebSocket socket = ws;
                            ws = null;
                            if (socket != null) socket.sendClose(1000, "Reconnecting");
                            connect();
                        });
                        case 9 -> scheduler.schedule(DiscordBot.this::connect,
                                IDENTIFY_DELAY_SECONDS, TimeUnit.SECONDS);
                        case 10 -> handleHello(payload.getAsJsonObject("d"));
                        default -> {
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Discord bot: error parsing payload", e);
                }
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            if (ws != webSocket) return;
            stopHeartbeat();
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            if (ws != webSocket) return null;
            ws = null;
            stopHeartbeat();
            if (statusCode == 1000 && reconnectAttempts == 0) {
                LOGGER.warn("Discord bot: disconnected (1000) - check that MESSAGE CONTENT INTENT is enabled "
                        + "in the Discord Developer Portal");
            }
            scheduleReconnect();
            return null;
        }
    }
}