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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Resolves a Discord-ready face URL from the same API used by ValorCraftSkins. */
final class ValorCraftSkinsAvatar {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("VChat");
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private static final long SUCCESS_TTL_MS = Duration.ofMinutes(30).toMillis();
    private static final long FAILURE_TTL_MS = Duration.ofMinutes(5).toMillis();
    private static final int MAX_CACHE_ENTRIES = 512;
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final Map<String, CachedAvatar> CACHE = new LinkedHashMap<>(128, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CachedAvatar> eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
    };

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
        CachedAvatar cached = cacheGet(key);
        if (cached != null && now < cached.expiresAt()) {
            return cached.url().isEmpty() ? safeFallback : cached.url();
        }

        try {
            String apiUrl = replaceEncoded(VChatTabConfig.discordValorCraftSkinsApiUrl(),
                    "{player}", playerName);
            URI apiUri = safeHttpUri(apiUrl);
            HttpRequest request = HttpRequest.newBuilder(apiUri)
                    .header("Accept", "application/json")
                    .header("User-Agent", "VChat/1.6.13")
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                cacheFailure(key, now);
                return safeFallback;
            }

            String headUrl = extractHeadUrl(response.body());
            if (headUrl.isEmpty()) {
                cacheFailure(key, now);
                return safeFallback;
            }
            safeHttpUri(headUrl);
            cachePut(key, new CachedAvatar(headUrl, now + SUCCESS_TTL_MS));
            return headUrl;
        } catch (Exception e) {
            cacheFailure(key, now);
            LOGGER.debug("ValorCraftSkins avatar unavailable for {}: {}", playerName, e.getMessage());
            return safeFallback;
        }
    }

    static String extractHeadUrl(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("head") || !root.get("head").isJsonObject()) return "";
            JsonObject head = root.getAsJsonObject("head");
            if (!head.has("url") || head.get("url").isJsonNull()) return "";
            String url = head.get("url").getAsString();
            return url == null ? "" : url.trim();
        } catch (Exception ignored) {
            return "";
        }
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
        cachePut(key, new CachedAvatar("", now + FAILURE_TTL_MS));
    }

    private static synchronized CachedAvatar cacheGet(String key) {
        return CACHE.get(key);
    }

    private static synchronized void cachePut(String key, CachedAvatar value) {
        CACHE.put(key, value);
    }

    private record CachedAvatar(String url, long expiresAt) {
    }
}
