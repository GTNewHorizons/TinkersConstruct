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
import tconstruct.util.McTextFormatter;

public class TiCNavigationButton extends TiCGuiButton {

    public enum ButtonSize {

        small(0.8f),
        large(2f),
        medium(1f);

        public float multi;

        ButtonSize(float multi) {
            this.multi = multi;
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
    public ItemStack[] renderStack;
    public String target;
    public String ButtonStr;
    public int color;

    public long lastUpdate;
    public int counter;

    public TiCNavigationButton(int id, ButtonSize bs, ItemStack s, String ButtonStr, String target,
            TiCBookPage parentPage) {
        this(
                id,
                bs,
                new ItemStack[] { s },
                ButtonStr,
                target,
                ButtonStr.length() != 0 ? Arrays.asList(ButtonStr) : new ArrayList<>(),
                parentPage,
                0x000000);
    }

    public TiCNavigationButton(int id, ButtonSize bs, ItemStack[] s, String ButtonStr, String target,
            TiCBookPage parentPage) {
        this(
                id,
                bs,
                s,
                ButtonStr,
                target,
                ButtonStr.length() != 0 ? Arrays.asList(ButtonStr) : new ArrayList<>(),
                parentPage,
                0x000000);
    }

    public TiCNavigationButton(int id, ButtonSize bs, ItemStack[] s, String ButtonStr, String target,
            TiCBookPage parentPage, int color) {
        this(
                id,
                bs,
                s,
                ButtonStr,
                target,
                ButtonStr.length() != 0 ? Arrays.asList(ButtonStr) : new ArrayList<>(),
                parentPage,
                color);
    }

    public TiCNavigationButton(int id, ButtonSize bs, ItemStack[] s, String ButtonStr, String target,
            List<String> tooltips, TiCBookPage parentPage, int color) {
        super(id, 0, 0, defaultHeight, defaultWidth, "", parentPage);
        this.bs = bs;
        this.renderStack = s;
        this.ButtonStr = McTextFormatter.addBold(ButtonStr);
        this.target = target;
        this.toolTips = tooltips;
        this.color = color;

        this.lastUpdate = System.currentTimeMillis();
        this.counter = 0;
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
            if (fonts.getStringWidth(this.ButtonStr) <= this.width / scale / this.bs.multi * 2) {
                fonts.drawString(
                        this.ButtonStr,
                        (int) ((this.xPosition + this.width / 2) / scale / this.bs.multi * 2
                                - fonts.getStringWidth(this.ButtonStr) / 2),
                        (int) ((this.yPosition + this.height) / scale / this.bs.multi * 2 - fonts.FONT_HEIGHT),
                        this.color);
            } else {
                // does we need auto split?
                fonts.drawSplitString(
                        ButtonStr,
                        (int) (this.xPosition / scale / this.bs.multi * 2),
                        (int) ((this.yPosition + this.height) / scale / this.bs.multi * 2 - fonts.FONT_HEIGHT),
                        this.width,
                        this.color);
            }
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            // resize back
            GL11.glScalef(2.0f, 2.0f, 1.0f);
            this.drawItem(mc, scale);
            GL11.glScalef(1.0f / this.bs.multi, 1.0f / this.bs.multi, 1.0f);
        }
    }

    private void drawItem(final Minecraft mc, float scale) {
        int length = renderStack.length;
        if (length == 0) {
            return;
        }

        if (length > 1 && System.currentTimeMillis() - lastUpdate > 1000) {
            lastUpdate = System.currentTimeMillis();
            counter++;
            if (counter >= length) counter = 0;
        }

        GL11.glPushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        this.itemRender.renderItemAndEffectIntoGUI(
                mc.fontRenderer,
                mc.renderEngine,
                this.renderStack[this.counter % length],
                (int) (this.xPosition / scale / this.bs.multi + 2),
                (int) (this.yPosition / scale / this.bs.multi + (this.ButtonStr.length() == 0 ? 2 : 0)));
        this.itemRender.renderItemOverlayIntoGUI(
                mc.fontRenderer,
                mc.renderEngine,
                this.renderStack[this.counter % length],
                (int) (this.xPosition / scale / this.bs.multi + 2),
                (int) (this.yPosition / scale / this.bs.multi + (this.ButtonStr.length() == 0 ? 2 : 0)));
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();
    }

    @Override
    public boolean needRenderTips() {
        return this.needRenderTips;
    }

}
