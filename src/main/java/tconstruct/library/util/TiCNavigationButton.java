package tconstruct.library.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import mantle.client.SmallFontRenderer;
import tconstruct.client.pages.TiCBookPage;

public class TiCNavigationButton extends TiCGuiButton {

    public enum ButtonSize {

        small(0.5f, 7),
        large(2f, 3),
        medium(1f, 5);

        public float multi;
        public int buttonEachRow;

        ButtonSize(float multi, int eachRow) {
            this.multi = multi;
            this.buttonEachRow = eachRow;
        }

        public static ButtonSize getSize(String s) {
            switch (s) {
                case "large":
                    return large;
                case "small":
                    return small;
                case "medium":
                    return medium;
                default:
                    return large;
            }
        }
    }

    public static final int defaultHeight = 20;
    public static final int defaultWidth = 20;

    RenderItem itemRender = new RenderItem();
    ButtonSize bs;
    ItemStack renderStack;
    public String target;
    public String ButtonStr;

    public TiCNavigationButton(int id, ButtonSize bs, ItemStack s, String ButtonStr, String target,
            TiCBookPage parentPage) {
        this(
                id,
                bs,
                s,
                ButtonStr,
                target,
                ButtonStr.length() != 0 ? Arrays.asList(ButtonStr) : new ArrayList<>(),
                parentPage);
    }

    public TiCNavigationButton(int id, ButtonSize bs, ItemStack s, String ButtonStr, String target,
            List<String> tooltips, TiCBookPage parentPage) {
        super(id, 0, 0, defaultHeight, defaultWidth, "", parentPage);
        this.bs = bs;
        this.renderStack = s;
        this.ButtonStr = ButtonStr;
        this.target = target;
        this.toolTips = tooltips;
    }

    public void drawButtonWithScale(Minecraft mc, int mouseX, int mouseY, float scale, SmallFontRenderer fonts) {
        if (this.visible) {
            this.height = (int) (defaultHeight * this.bs.multi * scale);
            this.width = (int) (defaultWidth * this.bs.multi * scale);

            boolean isMouseInButton = this.isHover(mouseX, mouseY);

            if (isMouseInButton) {
                GuiButton.drawRect(
                        (int) (this.xPosition / scale),
                        (int) (this.yPosition / scale),
                        (int) ((this.xPosition / scale + defaultWidth * this.bs.multi)),
                        (int) ((this.yPosition / scale + defaultHeight * this.bs.multi)),
                        0xAAAAAAAA);
            }

            // let string a half size of multi
            GL11.glScalef(this.bs.multi / 2, this.bs.multi / 2, 1.0f);
            fonts.drawString(
                    this.ButtonStr,
                    Math.round(
                            (this.xPosition + this.width / 2) / scale / this.bs.multi * 2
                                    - fonts.getStringWidth(this.ButtonStr) / 2),
                    Math.round((this.yPosition + this.height) / scale / this.bs.multi * 2 - fonts.FONT_HEIGHT),
                    0x000000);
            // resize back
            GL11.glScalef(2.0f, 2.0f, 1.0f);
            this.drawItem(mc, scale);
            GL11.glScalef(1.0f / this.bs.multi, 1.0f / this.bs.multi, 1.0f);
        }
    }

    private void drawItem(final Minecraft mc, float scale) {
        GL11.glPushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        this.itemRender.renderItemAndEffectIntoGUI(
                mc.fontRenderer,
                mc.renderEngine,
                this.renderStack,
                Math.round(this.xPosition / scale / this.bs.multi) + 2,
                Math.round(this.yPosition / scale / this.bs.multi) + (this.ButtonStr.length() == 0 ? 2 : 0));
        this.itemRender.renderItemOverlayIntoGUI(
                mc.fontRenderer,
                mc.renderEngine,
                this.renderStack,
                Math.round(this.xPosition / scale / this.bs.multi) + 2,
                Math.round(this.yPosition / scale / this.bs.multi) + (this.ButtonStr.length() == 0 ? 2 : 0));
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();
    }

}
