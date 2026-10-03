package tconstruct.tools.gui;

import static tconstruct.tools.gui.StationButtons.HOOK_V;
import static tconstruct.tools.gui.StationButtons.HOOK_W;
import static tconstruct.tools.gui.StationButtons.PANEL_HOOK_H;
import static tconstruct.tools.gui.StationButtons.PANEL_HOOK_INSET;
import static tconstruct.tools.gui.StationButtons.PANEL_HOOK_L;
import static tconstruct.tools.gui.StationButtons.PANEL_HOOK_R;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** A side panel's frame, scroll bar and caption, after TiC2's GuiInfoPanel. */
@SideOnly(Side.CLIENT)
final class InfoPanel {

    /** A skin in panel.png: the corner size and the size of its middle. */
    private static final int PANEL_CORNER = 4, PANEL_RES_W = 118, PANEL_RES_H = 75;
    private static final int FAR_U = PANEL_CORNER + PANEL_RES_W, FAR_V = PANEL_CORNER + PANEL_RES_H;
    /** The wood and metal skins' column in panel.png; the block picks the row. */
    private static final int PANEL_SKIN_U = PANEL_RES_W + 2 * PANEL_CORNER;
    static final int PANEL_TEXT_COLOR = 0xF0F0F0;
    static final int CAPTION_Y = 8;
    private static final int BAR_IN = 9, BAR_DOWN = 2, BAR_UP = 9;
    /** Text beside the bar wraps this much narrower, ending 4 px short of it. */
    static final int BAR_ROOM = 10;
    private static final int CAPTION_MIN_X = 5;

    private static final GuiElementScalable TOP_LEFT = piece(0, 0, PANEL_CORNER, PANEL_CORNER);
    private static final GuiElementScalable TOP_RIGHT = piece(FAR_U, 0, PANEL_CORNER, PANEL_CORNER);
    private static final GuiElementScalable BOT_LEFT = piece(0, FAR_V, PANEL_CORNER, PANEL_CORNER);
    private static final GuiElementScalable BOT_RIGHT = piece(FAR_U, FAR_V, PANEL_CORNER, PANEL_CORNER);
    private static final GuiElementScalable TOP = piece(PANEL_CORNER, 0, PANEL_RES_W, PANEL_CORNER);
    private static final GuiElementScalable BOT = piece(PANEL_CORNER, FAR_V, PANEL_RES_W, PANEL_CORNER);
    private static final GuiElementScalable LEFT = piece(0, PANEL_CORNER, PANEL_CORNER, PANEL_RES_H);
    private static final GuiElementScalable RIGHT = piece(FAR_U, PANEL_CORNER, PANEL_CORNER, PANEL_RES_H);
    /** Every panel's middle is the plain skin's, on wood and metal alike. */
    private static final GuiElementScalable BACKGROUND = piece(PANEL_CORNER, PANEL_CORNER, PANEL_RES_W, PANEL_RES_H);

    private final GuiBorderWidget border;
    private final GuiSliderWidget bar;
    /** The first text row, from the panel's top; the bar hangs under it. */
    private final int textTop;
    private final int cornerTaken;
    /** The panel's box, in the GUI's coordinates. */
    private int x, y, w, h;
    /** The caption drawn this frame when "..." cut it, and where it stands; null when it was whole. */
    private String cutCaption;
    private int captionLeft, captionWidth;

    InfoPanel(StationTheme theme, int textTop, int cornerTaken) {
        border = frame(theme);
        bar = bar(theme.sliderU);
        this.textTop = textTop;
        this.cornerTaken = cornerTaken;
        bar.hide();
    }

    private static GuiBorderWidget frame(StationTheme theme) {
        int xd = PANEL_SKIN_U, yd = theme.panelV;
        GuiBorderWidget border = new GuiBorderWidget();
        border.borderTop = TOP.shift(xd, yd);
        border.borderBottom = BOT.shift(xd, yd);
        border.borderLeft = LEFT.shift(xd, yd);
        border.borderRight = RIGHT.shift(xd, yd);
        border.cornerTopLeft = TOP_LEFT.shift(xd, yd);
        border.cornerTopRight = TOP_RIGHT.shift(xd, yd);
        border.cornerBottomLeft = BOT_LEFT.shift(xd, yd);
        border.cornerBottomRight = BOT_RIGHT.shift(xd, yd);
        return border;
    }

    /** The scroll bar whose sprites start at column u of panel.png. */
    static GuiSliderWidget bar(int u) {
        GuiElementDuex lit = piece(u + 3, 83, 3, 5);
        return new GuiSliderWidget(
                piece(u, 83, 3, 5),
                lit,
                lit,
                piece(u + 3, 88, 3, 4),
                piece(u + 3, 92, 3, 4),
                piece(u, 88, 3, 8));
    }

    /** Always with the sheet size: the four-argument constructor reads a default the six-argument one overwrites. */
    private static GuiElementScalable piece(int u, int v, int w, int h) {
        return new GuiElementScalable(u, v, w, h, 256, 256);
    }

    void place(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        bar.hide();
        cutCaption = null;
    }

    boolean outgrows(int want) {
        return want > h;
    }

    /** need[k] is the height rows k on take; the bar may hide just enough leading rows to bring the last into view. */
    void showBar(int[] need) {
        bar.show();
        int hidden = 0;
        while (hidden < need.length - 1 && need[hidden] > h) hidden++;
        bar.setSliderParameters(0, hidden, 1);
    }

    /** The first row shown; 0 while the bar is hidden. */
    int firstRow() {
        return bar.getValue();
    }

    int wrapWidth(int width) {
        return bar.isHidden() ? width : width - BAR_ROOM;
    }

    /** The mouse is in GUI coordinates here; a notch up shows the row above. */
    void scroll(int mouseX, int mouseY, int wheel) {
        if (bar.isHidden() || mouseX < x || mouseX >= x + w || mouseY < y || mouseY >= y + h) return;
        if (wheel > 0) bar.decrement();
        else bar.increment();
    }

    /**
     * The heading, centered and underlined; it moves left of anything in the corner, but not past CAPTION_MIN_X, and is
     * cut short with "..." when it still does not fit.
     */
    void caption(FontRenderer fontRendererObj, String caption) {
        int right = cornerTaken > 0 ? w - cornerTaken - 2 : w - CAPTION_MIN_X;
        String underlined = StatRows.ellipsize(
                fontRendererObj,
                EnumChatFormatting.UNDERLINE + EnumChatFormatting.getTextWithoutFormattingCodes(caption),
                right - CAPTION_MIN_X);
        int width = fontRendererObj.getStringWidth(underlined);
        int left = x + w / 2 - width / 2;
        // the underline's shadow ends on column left + width
        if (cornerTaken > 0) left = Math.min(left, Math.max(x + CAPTION_MIN_X, x + w - cornerTaken - 2 - width));
        fontRendererObj.drawStringWithShadow(underlined, left, y + CAPTION_Y, PANEL_TEXT_COLOR);
        boolean cut = !underlined
                .equals(EnumChatFormatting.UNDERLINE + EnumChatFormatting.getTextWithoutFormattingCodes(caption));
        cutCaption = cut ? caption : null;
        captionLeft = left;
        captionWidth = width;
    }

    /** The whole caption when "..." cut it and the mouse, in GUI coordinates, is on it; else null. */
    String cutCaptionAt(int mouseX, int mouseY) {
        if (cutCaption == null) return null;
        boolean on = mouseX >= captionLeft && mouseX < captionLeft + captionWidth
                && mouseY >= y + CAPTION_Y - 1
                && mouseY < y + CAPTION_Y + 9;
        return on ? cutCaption : null;
    }

    static void drawPanelHooks(Gui gui, int x, int y, int w, int u) {
        gui.drawTexturedModalRect(x + PANEL_HOOK_INSET, y - PANEL_HOOK_H, u, HOOK_V, PANEL_HOOK_L, PANEL_HOOK_H);
        gui.drawTexturedModalRect(
                x + w - PANEL_HOOK_INSET - PANEL_HOOK_R,
                y - PANEL_HOOK_H,
                u + HOOK_W - PANEL_HOOK_R,
                HOOK_V,
                PANEL_HOOK_R,
                PANEL_HOOK_H);
    }

    /** Expects panel.png bound and the color set; the mouse is in screen coordinates. */
    void draw(int guiLeft, int guiTop, int mouseX, int mouseY) {
        int left = guiLeft + x, top = guiTop + y;
        border.setPosition(left, top);
        border.setSize(w, h);
        border.draw();
        BACKGROUND.drawScaled(left + PANEL_CORNER, top + PANEL_CORNER, w - 2 * PANEL_CORNER, h - 2 * PANEL_CORNER);
        bar.setPosition(left + w - BAR_IN, top + textTop + BAR_DOWN);
        bar.setSize(h - BAR_UP - textTop - BAR_DOWN);
        bar.update(mouseX, mouseY, false);
        // the metal bar's rounded ends are half transparent
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        bar.draw();
        GL11.glDisable(GL11.GL_BLEND);
    }
}
