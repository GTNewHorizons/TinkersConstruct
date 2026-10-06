package tconstruct.tools.gui;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.tools.gui.StatRows.Row;
import tconstruct.util.config.PHConstruct;

/**
 * The Tinker Table's side panel: description.png's frame stretched to the tool's readout, as wide and as tall as the
 * screen allows, with the station panels' scroll bar when the readout still does not fit.
 */
@SideOnly(Side.CLIENT)
final class CraftingStationReadout {

    static final int MIN_W = 126, MIN_H = 172;
    private static final int MAX_W = StationPanels.PANEL_WIDTH_MAX;
    private static final ResourceLocation TEXTURE = new ResourceLocation("tinker", "textures/gui/description.png");
    /** description.png's frame: three px of edge, rounded inside four. */
    private static final int CORNER = 4, FAR_U = MIN_W - CORNER, FAR_V = MIN_H - CORNER;
    private static final int TEXT_X = 10, TEXT_RIGHT = 6, TITLE_Y = 8, ROWS_Y = TITLE_Y + 20;
    /** A row's glyphs end 8 px under its y, and 8 px of panel stay under them. */
    private static final int PAD = 16;
    private static final int BAR_IN = 9, BAR_TOP = 27, BAR_UP = 9;

    private static final GuiElementScalable MIDDLE = piece(CORNER, CORNER, FAR_U - CORNER, FAR_V - CORNER);

    private final GuiBorderWidget border = frame();
    private final GuiSliderWidget bar = InfoPanel.bar(StationTheme.METAL.sliderU);
    private ItemStack shown;
    /** The title when "..." cut it this frame, and its box in screen coordinates; null when it was whole. */
    private String cutTitle;
    private Rectangle titleBox = new Rectangle();
    private List<Row> rows = Collections.emptyList();
    /** The panel's box, in screen coordinates. */
    private int x, y, w = MIN_W, h = MIN_H;

    CraftingStationReadout() {
        bar.hide();
    }

    private static GuiBorderWidget frame() {
        GuiBorderWidget border = new GuiBorderWidget();
        border.cornerTopLeft = piece(0, 0, CORNER, CORNER);
        border.cornerTopRight = piece(FAR_U, 0, CORNER, CORNER);
        border.cornerBottomLeft = piece(0, FAR_V, CORNER, CORNER);
        border.cornerBottomRight = piece(FAR_U, FAR_V, CORNER, CORNER);
        border.borderTop = piece(CORNER, 0, FAR_U - CORNER, CORNER);
        border.borderBottom = piece(CORNER, FAR_V, FAR_U - CORNER, CORNER);
        border.borderLeft = piece(0, CORNER, CORNER, FAR_V - CORNER);
        border.borderRight = piece(FAR_U, CORNER, CORNER, FAR_V - CORNER);
        return border;
    }

    /** Always with the sheet size: the four-argument constructor reads a default the six-argument one overwrites. */
    private static GuiElementScalable piece(int u, int v, int w, int h) {
        return new GuiElementScalable(u, v, w, h, 256, 256);
    }

    /**
     * Sizes the panel at (x, y) for the tool it reads out, or for the plain text when tool is null; it may grow to
     * bottom.
     */
    void layout(int x, int y, int screenWidth, int bottom, ItemStack tool) {
        this.x = x;
        this.y = y;
        w = Math.max(MIN_W, Math.min(MAX_W, screenWidth - x - 2));
        h = MIN_H;
        bar.hide();
        cutTitle = null;
        shown = tool;
        if (tool == null) return;
        int width = textWidth();
        rows = ToolStationGuiHelper.readoutRows(tool, width);
        int need = StatRows.need(rows, ROWS_Y, PAD)[0];
        int saved = PHConstruct.stationPanelScrollBar;
        int cap = saved == PHConstruct.SCROLL_BAR_OFF ? Integer.MAX_VALUE
                : saved == PHConstruct.SCROLL_BAR_ON ? MIN_H : Math.max(MIN_H, bottom - y);
        h = Math.max(MIN_H, Math.min(need, cap));
        if (need <= cap) return;
        // rows wrap narrower beside the bar; it may hide just enough leading rows to bring the last into view
        rows = ToolStationGuiHelper.readoutRows(tool, width - InfoPanel.BAR_ROOM);
        int[] needs = StatRows.need(rows, ROWS_Y, PAD);
        int hidden = 0;
        while (hidden < needs.length - 1 && needs[hidden] > h) hidden++;
        bar.show();
        bar.setSliderParameters(0, hidden, 1);
    }

    int width() {
        return w;
    }

    int textX() {
        return x + TEXT_X;
    }

    int textWidth() {
        return w - TEXT_X - TEXT_RIGHT;
    }

    Rectangle bounds() {
        return new Rectangle(x, y, w, h);
    }

    /** The whole title when "..." cut it and the mouse, in screen coordinates, is on it; else null. */
    String cutTitleAt(int mouseX, int mouseY) {
        return cutTitle != null && titleBox.contains(mouseX, mouseY) ? cutTitle : null;
    }

    /** The mouse is in screen coordinates; a notch up shows the row above. */
    void scroll(int mouseX, int mouseY, int wheel) {
        if (bar.isHidden() || !bounds().contains(mouseX, mouseY)) return;
        if (wheel > 0) bar.decrement();
        else bar.increment();
    }

    void draw(Minecraft mc, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        border.setPosition(x, y);
        border.setSize(w, h);
        border.draw();
        MIDDLE.drawScaled(x + CORNER, y + CORNER, w - 2 * CORNER, h - 2 * CORNER);
        if (bar.isHidden()) return;
        mc.getTextureManager().bindTexture(StationDraw.panelTexture);
        bar.setPosition(x + w - BAR_IN, y + BAR_TOP);
        bar.setSize(h - BAR_UP - BAR_TOP);
        bar.update(mouseX, mouseY, false);
        // the metal bar's rounded ends are half transparent
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        bar.draw();
        GL11.glDisable(GL11.GL_BLEND);
    }

    /** The tool's title and rows, translated by the GUI's corner as the foreground layer is. */
    void drawText(FontRenderer fontRendererObj, int guiLeft, int guiTop) {
        if (shown == null) return;
        int left = textX() - guiLeft, top = y - guiTop;
        String whole = ToolStationGuiHelper.titleOf(shown);
        String title = StatRows.ellipsize(fontRendererObj, "\u00A7n" + whole, textWidth());
        ToolStationGuiHelper
                .drawCenteredString(fontRendererObj, title, left + textWidth() / 2, top + TITLE_Y, StatRows.WHITE);
        int width = fontRendererObj.getStringWidth(title);
        cutTitle = title.equals("\u00A7n" + whole) ? null : whole;
        titleBox = new Rectangle(textX() + textWidth() / 2 - width / 2, y + TITLE_Y - 1, width, 10);
        StatRows.drawRows(rows, bar.getValue(), left, top + ROWS_Y, top + h - PAD, false);
    }
}
