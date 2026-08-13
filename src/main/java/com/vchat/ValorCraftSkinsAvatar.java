package com.vchat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Resolves a Discord-ready face URL from the same API used by ValorCraftSkins. */
final class ValorCraftSkinsAvatar {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("VChat");
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private static final long SUCCESS_TTL_MS = Duration.ofMinutes(30).toMillis();
    private static final long FAILURE_TTL_MS = Duration.ofMinutes(5).toMillis();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final Map<String, CachedAvatar> CACHE = new ConcurrentHashMap<>();

    private ValorCraftSkinsAvatar() {
    }

    static String resolve(String playerName, String fallback) {
        String safeFallback = fallback == null ? "" : fallback;
        if (!VChatTabConfig.discordUseValorCraftSkinsAvatar()
                || playerName == null || !USERNAME.matcher(playerName).matches()) {
            return safeFallback;
        }

        String key = playerName.toLowerCase(Locale.ROOT);
        long now = System.currentTimeMillis();
        CachedAvatar cached = CACHE.get(key);
        if (cached != null && now < cached.expiresAt()) {
            return cached.url().isEmpty() ? safeFallback : cached.url();
        }

        try {
            String apiUrl = replaceEncoded(VChatTabConfig.discordValorCraftSkinsApiUrl(),
                    "{player}", playerName);
            URI apiUri = safeHttpUri(apiUrl);
            HttpRequest request = HttpRequest.newBuilder(apiUri)
                    .header("Accept", "application/json")
                    .header("User-Agent", "VChat/1.6.12")
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                cacheFailure(key, now);
                return safeFallback;
            }

            String skinUrl = extractSkinUrl(response.body());
            if (skinUrl.isEmpty()) {
                cacheFailure(key, now);
                return safeFallback;
            }
            safeHttpUri(skinUrl);
            String headUrl = buildHeadUrl(VChatTabConfig.discordSkinHeadUrlTemplate(), skinUrl, playerName);
            safeHttpUri(headUrl);
            CACHE.put(key, new CachedAvatar(headUrl, now + SUCCESS_TTL_MS));
            return headUrl;
        } catch (Exception e) {
            cacheFailure(key, now);
            LOGGER.debug("ValorCraftSkins avatar unavailable for {}: {}", playerName, e.getMessage());
            return safeFallback;
        }
    }

    static String extractSkinUrl(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("skin") || !root.get("skin").isJsonObject()) return "";
            JsonObject skin = root.getAsJsonObject("skin");
            if (!skin.has("url") || skin.get("url").isJsonNull()) return "";
            String url = skin.get("url").getAsString();
            return url == null ? "" : url.trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    static String buildHeadUrl(String template, String skinUrl, String playerName) {
        return replaceEncoded(replaceEncoded(template, "{skinUrl}", skinUrl),
                "{player}", playerName);
    }

    private static String replaceEncoded(String value, String placeholder, String replacement) {
        String encoded = URLEncoder.encode(replacement, StandardCharsets.UTF_8).replace("+", "%20");
        return value.replace(placeholder, encoded);
    }

    private static URI safeHttpUri(String value) {
        URI uri = URI.create(value);
        String scheme = uri.getScheme();
        if (uri.getHost() == null || !("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))) {
            throw new IllegalArgumentException("Only HTTP(S) URLs are allowed");
        }
        return uri;
    }

    private static void cacheFailure(String key, long now) {
        CACHE.put(key, new CachedAvatar("", now + FAILURE_TTL_MS));
    }

    private record CachedAvatar(String url, long expiresAt) {
    }
}
