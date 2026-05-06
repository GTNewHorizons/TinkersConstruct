package tconstruct.library.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import mantle.client.SmallFontRenderer;

public class TiCNavigationButton extends GuiButton {

    public enum ButtonSize {

        small(0.5f),
        large(2f),
        medium(1f);

        public float multi;

        ButtonSize(float multi) {
            this.multi = multi;
        }
    }

    public static int defaultHeight = 20;
    public static int defaultWidth = 20;

    RenderItem itemRender = new RenderItem();
    ButtonSize bs;
    ItemStack renderStack;
    public String target;
    public String ButtonStr;

    public TiCNavigationButton(int id, ButtonSize bs, ItemStack s, String ButtonStr, String target) {
        super(id, 0, 0, defaultHeight, defaultWidth, "");
        this.bs = bs;
        this.renderStack = s;
        this.ButtonStr = ButtonStr;
        this.target = target;
    }

    public void drawButtonWithScale(Minecraft mc, int mouseX, int mouseY, float scale, SmallFontRenderer fonts) {
        if (this.visible) {
            this.height = (int) (defaultHeight * this.bs.multi * scale);
            this.width = (int) (defaultWidth * this.bs.multi * scale);

            boolean isMouseInButton = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;

            if (isMouseInButton) {
                GuiButton.drawRect(
                        (int) (this.xPosition / scale),
                        (int) (this.yPosition / scale),
                        (int) ((this.xPosition / scale + defaultWidth * this.bs.multi)),
                        (int) ((this.yPosition / scale + defaultHeight * this.bs.multi)),
                        0xAAAAAAAA);
            }

            this.drawStrCenterAt(
                    this.ButtonStr,
                    (int) (this.xPosition + this.width / 2),
                    (int) (this.yPosition + this.height),
                    scale,
                    0x000000,
                    fonts);
            this.drawItem(mc, scale);
        }
    }

    private void drawStrCenterAt(String str, int X, int Y, float scale, int color, SmallFontRenderer fonts) {
        fonts.drawString(
                str,
                Math.round(X / scale - fonts.getStringWidth(str) / 2),
                Math.round(Y / scale - fonts.FONT_HEIGHT),
                color);
    }

    private void drawItem(final Minecraft mc, float scale) {
        GL11.glPushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(2.0f, 2.0f, 1.0f);
        this.itemRender.renderItemAndEffectIntoGUI(
                mc.fontRenderer,
                mc.renderEngine,
                this.renderStack,
                Math.round(this.xPosition / scale / 2) + 2,
                Math.round(this.yPosition / scale / 2) + 1);
        this.itemRender.renderItemOverlayIntoGUI(
                mc.fontRenderer,
                mc.renderEngine,
                this.renderStack,
                Math.round(this.xPosition / scale / 2) + 2,
                Math.round(this.yPosition / scale / 2) + 1);
        GL11.glScalef(0.5f, 0.5f, 1.0f);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();
    }

}
