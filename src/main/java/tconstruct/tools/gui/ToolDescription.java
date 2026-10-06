package tconstruct.tools.gui;

import static tconstruct.tools.gui.InfoPanel.PANEL_TEXT_COLOR;
import static tconstruct.tools.gui.StationPanels.PANEL_TEXT_X;
import static tconstruct.tools.gui.StationPanels.TEXT_INSET;
import static tconstruct.tools.gui.StationPanels.TEXT_PAD;
import static tconstruct.tools.gui.StationPanels.UPPER_Y;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** The open tab's description: wrapped and classified once per width, headings and stat labels in gold. */
@SideOnly(Side.CLIENT)
final class ToolDescription {

    private final ToolStationGui screen;
    private final StationPanels panels;

    private final Map<Integer, List<DescRow>> rowsByWidth = new HashMap<>();
    long glowTime;
    private String wrappedBody;

    private static final int HEADING_COLOR = 0xFFAA00;
    /** One turn of the hues: its period in ms, and its length in characters along a row. */
    private static final long FLOW_PERIOD = 2000L;
    private static final float FLOW_LENGTH = 12F;
    private static final float FADE_MS = 250F;
    private static final int HEADING_GAP = 4;
    /** Caps one frame's fade step, so the first frame after a pause does not finish the fade at once. */
    private static final long FADE_STEP_MAX_MS = 100L;
    private static final int STAT_GAP = 2;
    private static final int SECTION_ROW = 7;

    ToolDescription(ToolStationGui screen, StationPanels panels) {
        this.screen = screen;
        this.panels = panels;
    }

    private static final class DescRow {

        final String text;
        final boolean heading;
        /** "Durability:" of a row that rates a stat, else null. */
        final String label;
        final StatRating rating;
        final int advance;
        int ratingX;
        /** 0 to 1: how far into the flow the gold has faded. */
        float glow;

        DescRow(String text, boolean heading, String label, StatRating rating, int advance) {
            this.text = text;
            this.heading = heading;
            this.label = label;
            this.rating = rating;
            this.advance = advance;
        }
    }

    /** Wrapped once per text and width, not per frame; a row keeps its fade, so each width keeps its own rows. */
    private List<DescRow> rows(FontRenderer fontRendererObj, int width) {
        if (!screen.body.equals(wrappedBody)) {
            rowsByWidth.clear();
            wrappedBody = screen.body;
        }
        List<DescRow> rows = rowsByWidth.get(width);
        if (rows == null) {
            rows = describe(fontRendererObj, screen.body, width);
            rowsByWidth.put(width, rows);
        }
        return rows;
    }

    /** need[k] is the panel height for the description from row k on. */
    int[] need(FontRenderer fontRendererObj, int width) {
        List<DescRow> rows = rows(fontRendererObj, width);
        int[] need = new int[Math.max(1, rows.size())];
        int below = TEXT_PAD;
        for (int k = rows.size() - 1; k >= 0; k--) {
            below += rows.get(k).advance;
            need[k] = TEXT_INSET + below;
        }
        if (rows.isEmpty()) need[0] = TEXT_INSET + TEXT_PAD;
        return need;
    }

    private List<DescRow> describe(FontRenderer fontRendererObj, String body, int width) {
        // only the English descriptions are sectioned
        boolean styled = englishLocale();
        // a heading is a whole unwrapped line ending in a colon, not a piece the wrap cut off
        Set<String> headings = new HashSet<>();
        if (styled) for (String line : body.split("\n")) if (line.trim().endsWith(":")) headings.add(line);
        List<DescRow> rows = new ArrayList<>();
        for (Object line : fontRendererObj.listFormattedStringToWidth(body, width)) {
            String text = (String) line;
            int colon = text.indexOf(':');
            StatRating rating = styled && colon > 0 && !text.startsWith("-") ? StatRating.of(text.substring(colon + 1))
                    : null;
            String label = rating != null ? text.substring(0, colon + 1) : null;
            boolean heading = headings.contains(text);
            int gap = heading ? HEADING_GAP : label != null ? STAT_GAP : 0;
            int advance = styled && text.isEmpty() ? SECTION_ROW : fontRendererObj.FONT_HEIGHT + gap;
            rows.add(new DescRow(text, heading, label, rating, advance));
        }
        ratingColumns(fontRendererObj, rows);
        return rows;
    }

    private void ratingColumns(FontRenderer fontRendererObj, List<DescRow> rows) {
        int start = 0;
        while (start < rows.size()) {
            int end = start, widest = 0;
            while (end < rows.size() && rows.get(end).label != null) {
                widest = Math.max(widest, fontRendererObj.getStringWidth(rows.get(end).label));
                end++;
            }
            for (int i = start; i < end; i++) rows.get(i).ratingX = widest;
            // the row that ended the block rates nothing
            start = end + 1;
        }
    }

    /** The English locales, Pirate Speak included. */
    private static boolean englishLocale() {
        String code = FMLCommonHandler.instance().getCurrentLanguage();
        return code.equals("en_US") || code.equals("en_GB") || code.equals("en_PT");
    }

    void drawDescription(FontRenderer fontRendererObj, int mouseX, int mouseY) {
        panels.upper.caption(fontRendererObj, screen.title);
        List<DescRow> rows = rows(fontRendererObj, panels.upper.wrapWidth(panels.textWidth()));
        long now = Minecraft.getSystemTime();
        float step = Math.min(now - glowTime, FADE_STEP_MAX_MS) / FADE_MS;
        glowTime = now;
        int y = UPPER_Y + TEXT_INSET, bottom = UPPER_Y + panels.upperH - TEXT_PAD;
        for (int i = panels.upper.firstRow(); i < rows.size() && y + fontRendererObj.FONT_HEIGHT <= bottom; i++) {
            DescRow row = rows.get(i);
            String label = row.heading ? row.text : row.label;
            if (label == null) {
                fontRendererObj.drawStringWithShadow(row.text, PANEL_TEXT_X, y, PANEL_TEXT_COLOR);
                y += row.advance;
                continue;
            }
            // the mouse is in screen coordinates, the row in the GUI's
            boolean hovered = mouseX >= screen.guiLeft + PANEL_TEXT_X
                    && mouseX < screen.guiLeft + PANEL_TEXT_X + fontRendererObj.getStringWidth(label)
                    && mouseY >= screen.guiTop + y
                    && mouseY < screen.guiTop + y + fontRendererObj.FONT_HEIGHT;
            row.glow = Math.max(0F, Math.min(1F, row.glow + (hovered ? step : -step)));
            drawHeading(fontRendererObj, label, PANEL_TEXT_X, y, row.glow);
            if (!row.heading) {
                String rating = row.text.substring(label.length());
                fontRendererObj.drawStringWithShadow(rating, PANEL_TEXT_X + row.ratingX, y, row.rating.color);
            }
            y += row.advance;
        }
    }

    private void drawHeading(FontRenderer fontRendererObj, String row, int x, int y, float glow) {
        if (glow <= 0F) {
            fontRendererObj.drawStringWithShadow(row, x, y, HEADING_COLOR);
            return;
        }
        float phase = (Minecraft.getSystemTime() % FLOW_PERIOD) / (float) FLOW_PERIOD;
        // the unicode font advances by half pixels that per-character drawing loses, so the whole row gets one color
        if (fontRendererObj.getUnicodeFlag()) {
            fontRendererObj.drawStringWithShadow(row, x, y, blend(HEADING_COLOR, flowColor(0, phase), glow));
            return;
        }
        for (int i = 0; i < row.length(); i++) {
            String ch = row.substring(i, i + 1);
            fontRendererObj.drawStringWithShadow(ch, x, y, blend(HEADING_COLOR, flowColor(i, phase), glow));
            x += fontRendererObj.getStringWidth(ch);
        }
    }

    private static int flowColor(int i, float phase) {
        return Color.HSBtoRGB((i / FLOW_LENGTH - phase + 1F) % 1F, 0.8F, 1F);
    }

    private static int blend(int from, int to, float t) {
        int rgb = 0;
        for (int shift = 16; shift >= 0; shift -= 8) {
            int a = from >> shift & 0xFF, b = to >> shift & 0xFF;
            rgb |= Math.round(a + (b - a) * t) << shift;
        }
        return rgb;
    }
}
