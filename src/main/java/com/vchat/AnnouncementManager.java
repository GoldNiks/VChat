package com.vchat;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Automatic chat announcements: shows one configured phrase every
 * {@code intervalSeconds}, cycling through the list in random order without
 * repeating the same message twice in a row. Supports legacy colors and
 * clickable links written as {@code [text](https://url)}.
 */
public class AnnouncementManager {
    private static final String LINK_MARKER_PREFIX = "\uE000vchat_link_";
    private static final String LINK_MARKER_SUFFIX = "\uE001";
    private static final Random RANDOM = new Random();

    private static int ticksUntilNext = 0;
    private static int lastIndex = -1;

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        if (server == null) return;
        if (!VChatTabConfig.announcementsEnabled()) {
            ticksUntilNext = 0;
            return;
        }
        List<String> messages = VChatTabConfig.announcementsMessages();
        if (messages.isEmpty()) return;

        if (ticksUntilNext > 0) {
            ticksUntilNext--;
            return;
        }
        int index = pick(messages.size());
        if (index >= 0) {
            lastIndex = index;
            broadcast(server, messages.get(index));
        }
        ticksUntilNext = VChatTabConfig.announcementsIntervalSeconds() * 20;
    }

    private static int pick(int size) {
        if (size <= 0) return -1;
        if (size == 1) return 0;
        int index = RANDOM.nextInt(size);
        while (index == lastIndex) {
            index = RANDOM.nextInt(size);
        }
        return index;
    }

    /** Sends a system announcement to every online player. */
    public static void broadcast(MinecraftServer server, String rawMessage) {
        server.getPlayerList().broadcastSystemMessage(render(rawMessage), false);
    }

    /** Re-arms the timer, e.g. after a config reload. */
    public static void reset() {
        ticksUntilNext = 0;
        lastIndex = -1;
    }

    /**
     * Renders a raw announcement: legacy colors via {@link HexUtil} plus
     * clickable {@code [text](url)} links. Links become underlined,
     * click-to-open and show the URL on hover.
     */
    public static Component render(String raw) {
        if (raw == null) return Component.literal("");
        List<String[]> links = new ArrayList<>();
        StringBuilder base = new StringBuilder();
        int cursor = 0;
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\[([^\\]]+)\\]\\(([^)]+)\\)").matcher(raw);
        while (matcher.find()) {
            base.append(raw, cursor, matcher.start());
            int linkIndex = links.size();
            base.append(LINK_MARKER_PREFIX).append(linkIndex).append(LINK_MARKER_SUFFIX);
            links.add(new String[]{matcher.group(1), matcher.group(2)});
            cursor = matcher.end();
        }
        base.append(raw, cursor, raw.length());

        Component styled = HexUtil.fromLegacy(base.toString());
        MutableComponent result = Component.empty();
        for (Component sibling : styled.getSiblings()) {
            appendWithLinks(result, sibling, links);
        }
        return result;
    }

    private static void appendWithLinks(MutableComponent target, Component sibling, List<String[]> links) {
        String text = sibling.getString();
        int from = 0;
        int markerAt;
        while ((markerAt = text.indexOf(LINK_MARKER_PREFIX, from)) >= 0) {
            int markerEnd = text.indexOf(LINK_MARKER_SUFFIX, markerAt);
            if (markerEnd < 0) break;
            if (markerAt > from) {
                target.append(Component.literal(text.substring(from, markerAt))
                        .withStyle(sibling.getStyle()));
            }
            String linkId = text.substring(markerAt + LINK_MARKER_PREFIX.length(), markerEnd);
            try {
                int index = Integer.parseInt(linkId);
                String[] link = links.get(index);
                Component linkText = HexUtil.fromLegacy(link[0]);
                HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.literal(link[1]));
                for (Component part : linkText.getSiblings()) {
                    target.append(part.copy().withStyle(style -> style
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link[1]))
                            .withHoverEvent(hover)));
                }
            } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
            }
            from = markerEnd + LINK_MARKER_SUFFIX.length();
        }
        if (from < text.length()) {
            target.append(Component.literal(text.substring(from)).withStyle(sibling.getStyle()));
        }
    }
}
