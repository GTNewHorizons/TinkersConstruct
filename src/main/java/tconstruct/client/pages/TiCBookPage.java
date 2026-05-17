package tconstruct.client.pages;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import mantle.books.BookData;
import mantle.client.pages.BookPage;
import tconstruct.library.util.ItemStackWithPosition;
import tconstruct.library.util.TiCGuiButton;
import tconstruct.library.util.WidgetsHasTooltips;

public abstract class TiCBookPage extends BookPage {

    public static final int PAGECONTENTHEIGHT = 165;

    // outer gap is 16 inner gap is 5, 185 + 16 + 5 = 206
    public static final int PAGECONTENTWIDTH = 185;

    public static final int BACKGROUNDCOLOR = 0xFFFAEE;

    List<TiCGuiButton> pageButtonList;
    public List<WidgetsHasTooltips> widgetsList = new ArrayList<>();

    public void updateButtonPositionAndRender(int startX, int startY, float scale, int mouseX, int mouseY,
            List<GuiButton> parentButtonList) {

        // just for debug, draw a rect on page range to see where is the border
        // Gui.drawRect(startX, startY, startX + PAGECONTENTWIDTH, startY + PAGECONTENTHEIGHT, 0xAAAAAAAA);

        updateButtonPosition(startX, startY, scale, mouseX, mouseY, parentButtonList);
        render(startX, startY, scale, mouseX, mouseY, parentButtonList);
    }

    public void updateButtonPosition(int startX, int startY, float scale, int mouseX, int mouseY,
            List<GuiButton> parentButtonList) {}

    public void render(int startX, int startY, float scale, int mouseX, int mouseY, List<GuiButton> parentButtonList) {}

    @Override
    public void renderContentLayer(int localwidth, int localheight, boolean isTranslatable) {
        this.updateButtonPositionAndRender(localheight, localheight, 1.0f, 0, 0, null);
    }

    public void actionPerformed(GuiButton button, BookData b) {}

    void drawStrAt(String str, int X, int Y, float scale) {
        drawStrAt(str, X, Y, scale, 0x000000);
    }

    void drawStrAt(String str, int X, int Y, float scale, int color) {
        manual.fonts.drawString(str, (int) (X / scale), (int) (Y / scale), color);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    void drawStrCenterAt(String str, int X, int Y, float scale, int color) {
        manual.fonts.drawString(
                str,
                (int) (X / scale - manual.fonts.getStringWidth(str) / 2),
                (int) ((Y - manual.fonts.FONT_HEIGHT / 2) / scale),
                color);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    void renderItemStackIntoPage(ItemStack stack, int x, int y) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        manual.renderitem.renderItemAndEffectIntoGUI(manual.fonts, manual.getMC().renderEngine, stack, x, y);
        if (stack.stackSize > 1) manual.renderitem.renderItemOverlayIntoGUI(
                manual.fonts,
                manual.getMC().renderEngine,
                stack,
                x,
                y,
                String.valueOf(stack.stackSize));
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    void beforeRenderItem() {
        beforeRenderItem(2.0F);
    }

    void beforeRenderItem(float scale) {
        beforeRenderItem(scale, 100);
    }

    void beforeRenderItem(float scale, int z) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glScalef(scale, scale, 2.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        manual.renderitem.zLevel = z;
    }

    void afterRenderItem() {
        afterRenderItem(2.0F);
    }

    void afterRenderItem(float scale) {
        manual.renderitem.zLevel = 0;
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    void renderItemStackIntoPage(ItemStack stack, int x, int y, float scale) {
        renderItemStackIntoPage(stack, x, y);
        this.widgetsList.add(new ItemStackWithPosition(stack, x, y, scale));
    }

}
