package tconstruct.client.pages;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import mantle.books.BookData;
import mantle.client.pages.BookPage;
import tconstruct.library.util.TiCGuiButton;
import tconstruct.util.ItemStackWithPosition;

public abstract class TiCBookPage extends BookPage {

    public static final int PAGECONTENTHEIGHT = 165;
    public static final int PAGECONTENTWIDTH = 190;

    List<TiCGuiButton> pageButtonList;
    public List<ItemStackWithPosition> pageItemStackList = new ArrayList<>();

    public void updateButtonPositionAndRender(int startX, int startY, float scale, int mouseX, int mouseY,
            List<GuiButton> parentButtonList) {
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
}
