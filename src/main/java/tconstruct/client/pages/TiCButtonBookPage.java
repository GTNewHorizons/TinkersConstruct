package tconstruct.client.pages;

import java.util.List;

import net.minecraft.client.gui.GuiButton;

import mantle.client.pages.BookPage;
import tconstruct.library.util.TiCGuiButton;

public abstract class TiCButtonBookPage extends BookPage {

    public static final int PAGECONTENTHEIGHT = 165;
    public static final int PAGECONTENTWIDTH = 190;

    List<TiCGuiButton> pageButtonList;

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

    public void actionPerformed(GuiButton button) {}
}
