package com.vchat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MessageFormatterTest {

    private static final Map<String, String> VALUES = Map.ofEntries(
            Map.entry("prefix", "&7[VIP] "),
            Map.entry("suffix", "&r"),
            Map.entry("name", "Steve"),
            Map.entry("display_name", "Steve"),
            Map.entry("message", "hello"),
            Map.entry("group", "default"),
            Map.entry("world", "minecraft:overworld"),
            Map.entry("channel", "global"),
            Map.entry("stage", "Глава 1"),
            Map.entry("balance", "100"),
            Map.entry("tps", "19.5")
    );

    private static String plain(Map<String, String> values, String pattern) {
        return MessageFormatter.formatValues(pattern, values, null).getString();
    }

    @Test
    void replacesPlaceholders() {
        assertEquals("Hello Steve!", plain(VALUES, "Hello <name>!"));
        assertEquals("100", plain(VALUES, "<balance>"));
        assertEquals("Глава 1", plain(VALUES, "<stage>"));
    }

    @Test
    void unknownPlaceholderLeftAsIs() {
        assertEquals("Hi <unknown>", plain(VALUES, "Hi <unknown>"));
    }

    @Test
    void emptyPatternRendersEmpty() {
        assertEquals("", plain(VALUES, ""));
        assertEquals("", plain(VALUES, null));
    }

    @Test
    void emptyValuePlaceholderIsSkipped() {
        assertEquals("ab", plain(Map.of("name", ""), "a<name>b"));
    }

    @Test
    void plainReplacementInheritsRunningStyle() {
        Component rendered = MessageFormatter.formatValues("&a<name>!", VALUES, null);
        List<Component> siblings = rendered.getSiblings();
        assertEquals("Steve", siblings.get(0).getString());
        assertEquals(0x55FF55, siblings.get(0).getStyle().getColor().getValue());
        assertEquals("Steve!", rendered.getString());
    }

    @Test
    void formattedReplacementParsedAndStyleContinues() {
        Component rendered = MessageFormatter.formatValues("<prefix>! <name>",
                Map.of("prefix", "&aX&r", "name", "Steve"), null);
        assertEquals("X! Steve", rendered.getString());
        List<Component> siblings = rendered.getSiblings();
        assertEquals("X", siblings.get(0).getString());
        assertEquals(0x55FF55, siblings.get(0).getStyle().getColor().getValue());
        assertEquals(Style.EMPTY, siblings.get(1).getStyle());
    }

    @Test
    void colorCodesBeforePlaceholderApplyToIt() {
        Component rendered = MessageFormatter.formatValues("&#FF0000<name>", VALUES, null);
        List<Component> siblings = rendered.getSiblings();
        assertEquals("Steve", siblings.get(0).getString());
        assertEquals(0xFF0000, siblings.get(0).getStyle().getColor().getValue());
    }

    @Test
    void hoverAppliedToNamePiecesOnly() {
        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Team"));
        Component rendered = MessageFormatter.formatValues("&a<prefix>&7<name> <message>",
                Map.of("prefix", "&aX", "name", "Steve", "message", "hi"), hover);
        List<Component> siblings = rendered.getSiblings();
        assertEquals("Steve", siblings.get(1).getString());
        assertNotNull(siblings.get(1).getStyle().getHoverEvent(), "name piece must carry hover");
        assertEquals("hi", siblings.get(3).getString());
        assertNull(siblings.get(3).getStyle().getHoverEvent(), "message piece must not carry hover");
    }

    @Test
    void noHoverEventWhenNull() {
        Component rendered = MessageFormatter.formatValues("<name>", VALUES, null);
        assertEquals("Steve", rendered.getSiblings().get(0).getString());
        assertNull(rendered.getSiblings().get(0).getStyle().getHoverEvent());
    }

    @Test
    void multipleSamePlaceholders() {
        assertEquals("aStevebStevec", plain(VALUES, "a<name>b<name>c"));
    }
}
