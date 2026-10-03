package tconstruct.tools.gui;

import static tconstruct.tools.gui.InfoPanel.PANEL_TEXT_COLOR;
import static tconstruct.tools.gui.StationButtons.COLUMN_W;
import static tconstruct.tools.gui.StationDraw.BEAM_CAP;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.tools.gui.StatRows.Row;
import tconstruct.util.config.PHConstruct;

/** The two side panels: how tall each is this frame, and what each shows. */
@SideOnly(Side.CLIENT)
final class StationPanels {

    /** The panels start 2 px past the 176 px central panel and hang 4 px of link under the 7 px beam. */
    static final int PANEL_X = COLUMN_W + 178, PANEL_TEXT_X = PANEL_X + 8, UPPER_Y = 11, PANEL_GAP = 4;
    static final int TEXT_INSET = 25, TEXT_PAD = 6;
    private static final int UPPER_MIN_H = 94, LOWER_MIN_H = 87;
    /** The central panel's height; toolstation.png is drawn for it. */
    static final int Y_SIZE = UPPER_Y + UPPER_MIN_H + PANEL_GAP + LOWER_MIN_H;
    private static final int STATS_TOP = InfoPanel.CAPTION_Y + 20, STATS_PAD = 13;
    /** A text row's ink, its shadow included, takes all but the last of the font's 9 pixel rows. */
    private static final int GLYPH_H = 8;
    /** As much space under the last row's ink as above the first row's. */
    private static final int MODIFIERS_TOP = 13, MODIFIERS_PAD = GLYPH_H + MODIFIERS_TOP;
    /** The widest panel that fits a 1366x768 screen at GUI scale 3. */
    static final int PANEL_WIDTH_MAX = 156;
    static final int PANEL_WIDTH_MIN = 126;

    /** TiC2's idle hammer. */
    private static final String[] IDLE_HAMMER = { "  .", "/( _________", "|  >:=========`", ")(", "\"\"" };

    private final ToolStationGui screen;
    final ToolDescription description;
    final InfoPanel upper, lower;

    int panelWidth = PANEL_WIDTH_MAX;
    int upperH = UPPER_MIN_H, lowerY = UPPER_Y + UPPER_MIN_H + PANEL_GAP, lowerH = LOWER_MIN_H, panelBottom = Y_SIZE;
    private ItemStack shown;
    private boolean warning, scrolls;
    private List<Row> statsRows = Collections.emptyList(), modifierRows = Collections.emptyList();

    StationPanels(ToolStationGui screen) {
        this.screen = screen;
        description = new ToolDescription(screen, this);
        upper = new InfoPanel(screen.theme(), TEXT_INSET, ScrollSwitch.CORNER);
        lower = new InfoPanel(screen.theme(), MODIFIERS_TOP, 0);
    }

    /** Text wraps from 8 px inside the panel's left edge to 3 px short of its right. */
    int textWidth() {
        return panelWidth - 11;
    }

    int right() {
        return PANEL_X + panelWidth;
    }

    int bottom() {
        return panelBottom;
    }

    /** The column, the central panel, and the side panels to their beam caps' ends, in GUI coordinates. */
    boolean covers(Rectangle area) {
        int sideX = PANEL_X - BEAM_CAP;
        return area.intersects(0, 0, sideX, Y_SIZE)
                || area.intersects(sideX, 0, panelWidth + 2 * BEAM_CAP, panelBottom);
    }

    ItemStack shown() {
        return shown;
    }

    /** The lower panel takes the slack, so the column ends with the central panel. */
    void layoutPanels(FontRenderer fontRendererObj, ItemStack tool) {
        shown = tool;
        warning = tool == null && screen.components.showsMaterialWarning();
        int width = textWidth();
        int[] upperNeed = measureUpper(fontRendererObj, width);
        int[] lowerNeed = tool != null ? measureModifiers(width) : null;
        // the warning keeps the description's height, so the panel does not jump
        upperH = Math.max(UPPER_MIN_H, warning ? description.need(fontRendererObj, width)[0] : upperNeed[0]);
        lowerY = UPPER_Y + upperH + PANEL_GAP;
        lowerH = Math.max(lowerWant(fontRendererObj, lowerNeed), Y_SIZE - lowerY);
        scrolls = scrollBar(lowerY + lowerH > screen.height - screen.guiTop - 2);
        if (scrolls) {
            upperH = UPPER_MIN_H;
            lowerY = UPPER_Y + upperH + PANEL_GAP;
            lowerH = Y_SIZE - lowerY;
        }
        // NEI hides its items against whichever of the panels and the central GUI reaches lower
        panelBottom = Math.max(lowerY + lowerH, Y_SIZE);
        upper.place(PANEL_X, UPPER_Y, panelWidth, upperH);
        lower.place(PANEL_X, lowerY, panelWidth, lowerH);
        if (!scrolls) return;
        // rows wrap narrower beside a bar, so a panel that shows one is measured again
        int narrow = width - InfoPanel.BAR_ROOM;
        if (upper.outgrows(upperNeed[0])) upper.showBar(measureUpper(fontRendererObj, narrow));
        if (lowerNeed != null && lower.outgrows(lowerNeed[0])) lower.showBar(measureModifiers(narrow));
    }

    private int[] measureUpper(FontRenderer fontRendererObj, int width) {
        if (shown != null) {
            statsRows = ToolStationGuiHelper.statsRows(shown, width);
            return StatRows.need(statsRows, STATS_TOP, STATS_PAD);
        }
        if (warning) return screen.components.warningNeed(fontRendererObj, width);
        return description.need(fontRendererObj, width);
    }

    private int[] measureModifiers(int width) {
        modifierRows = ToolStationGuiHelper.modifierRows(shown, width);
        return StatRows.need(modifierRows, MODIFIERS_TOP, MODIFIERS_PAD);
    }

    private int lowerWant(FontRenderer fontRendererObj, int[] modifiersNeed) {
        if (modifiersNeed != null) return Math.max(LOWER_MIN_H, modifiersNeed[0]);
        return screen.components.componentNames != null ? componentsNeed(fontRendererObj) : LOWER_MIN_H;
    }

    private static boolean scrollBar(boolean grownPastScreen) {
        int saved = PHConstruct.stationPanelScrollBar;
        return saved == PHConstruct.SCROLL_BAR_ON || saved != PHConstruct.SCROLL_BAR_OFF && grownPastScreen;
    }

    /** The whole caption under the mouse, in GUI coordinates, when "..." cut it; else null. */
    String cutCaptionAt(int mouseX, int mouseY) {
        String caption = upper.cutCaptionAt(mouseX, mouseY);
        return caption != null ? caption : lower.cutCaptionAt(mouseX, mouseY);
    }

    boolean scrolls() {
        return scrolls;
    }

    /** Mouse coordinates are the GUI's, not the screen's. */
    void scroll(int mouseX, int mouseY, int wheel) {
        upper.scroll(mouseX, mouseY, wheel);
        lower.scroll(mouseX, mouseY, wheel);
    }

    /** The list's last row, then as much space under it as the caption has above it. */
    private int componentsNeed(FontRenderer fontRendererObj) {
        int glyphsEnd = TEXT_INSET + (screen.components.componentNames.length - 1) * fontRendererObj.FONT_HEIGHT
                + GLYPH_H;
        return glyphsEnd + InfoPanel.CAPTION_Y;
    }

    void drawPanelText(FontRenderer fontRendererObj, int mouseX, int mouseY) {
        if (shown != null) {
            upper.caption(fontRendererObj, ToolStationGuiHelper.titleOf(shown));
            int statsTop = UPPER_Y + STATS_TOP, statsLast = UPPER_Y + upperH - STATS_PAD;
            StatRows.drawRows(statsRows, upper.firstRow(), PANEL_TEXT_X, statsTop, statsLast);
            int modifiersTop = lowerY + MODIFIERS_TOP, modifiersLast = lowerY + lowerH - MODIFIERS_PAD;
            StatRows.drawRows(modifierRows, lower.firstRow(), PANEL_TEXT_X, modifiersTop, modifiersLast);
        } else {
            drawToolInformation(fontRendererObj, mouseX, mouseY);
        }
    }

    private void drawToolInformation(FontRenderer fontRendererObj, int mouseX, int mouseY) {
        if (warning) screen.components.drawWarning(fontRendererObj);
        else description.drawDescription(fontRendererObj, mouseX, mouseY);
        if (screen.components.componentNames != null) screen.components.drawComponents(fontRendererObj, mouseX, mouseY);
        else drawIdleHammer(fontRendererObj);
    }

    /** All lines share one left edge; centering each on its own would pull the drawing apart. */
    private void drawIdleHammer(FontRenderer fontRendererObj) {
        int w = 0;
        for (String line : IDLE_HAMMER) w = Math.max(w, fontRendererObj.getStringWidth(line));
        int x = PANEL_X + (panelWidth - w) / 2;
        int y = lowerY + (lowerH - IDLE_HAMMER.length * fontRendererObj.FONT_HEIGHT) / 2;
        String gray = EnumChatFormatting.DARK_GRAY.toString();
        for (int i = 0; i < IDLE_HAMMER.length; i++) {
            fontRendererObj.drawStringWithShadow(
                    gray + IDLE_HAMMER[i],
                    x,
                    y + i * fontRendererObj.FONT_HEIGHT,
                    PANEL_TEXT_COLOR);
        }
    }
}
