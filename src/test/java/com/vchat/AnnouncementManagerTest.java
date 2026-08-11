package com.vchat;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AnnouncementManagerTest {

    private static List<Component> flatten(Component root) {
        List<Component> flat = new ArrayList<>();
        collect(root, flat);
        return flat;
    }

    private static void collect(Component node, List<Component> out) {
        for (Component sibling : node.getSiblings()) {
            out.add(sibling);
            collect(sibling, out);
        }
    }

    @Test
    void linkBecomesClickableAndUnderlined() {
        Component rendered = AnnouncementManager.render("&e[Карта](https://map.example) готова");
        List<Component> parts = flatten(rendered);
        Component link = null;
        for (Component part : parts) {
            if (part.getString().equals("Карта")) {
                link = part;
                break;
            }
        }
        assertNotNull(link, "link text should be rendered");
        assertNotNull(link.getStyle().getClickEvent());
        assertEquals(ClickEvent.Action.OPEN_URL, link.getStyle().getClickEvent().getAction());
        assertEquals("https://map.example", link.getStyle().getClickEvent().getValue());
        assertEquals(Boolean.TRUE, link.getStyle().isUnderlined());
        assertNotNull(link.getStyle().getHoverEvent());
        String hover = link.getStyle().getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT).getString();
        assertEquals("https://map.example", hover);
    }

    @Test
    void plainTextWithoutLinksStaysPlain() {
        Component rendered = AnnouncementManager.render("&7Простое сообщение");
        List<Component> parts = flatten(rendered);
        assertEquals("Простое сообщение", rendered.getString());
        for (Component part : parts) {
            assertNull(part.getStyle().getClickEvent());
        }
    }

    @Test
    void multipleLinksAndSurroundingText() {
        Component rendered = AnnouncementManager.render(
                "&eСайт: &f[один](https://a.ru) &7| &f[два](https://b.ru)");
        assertEquals("Сайт: один | два", rendered.getString());
        List<String> urls = new ArrayList<>();
        for (Component part : flatten(rendered)) {
            ClickEvent click = part.getStyle().getClickEvent();
            if (click != null) urls.add(click.getValue());
        }
        assertEquals(List.of("https://a.ru", "https://b.ru"), urls);
    }

    @Test
    void nullRendersEmpty() {
        assertEquals("", AnnouncementManager.render(null).getString());
    }
}
