package tconstruct.tools.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import mantle.books.BookData;

public class TiCTurnPageButton extends GuiButton {

    private final boolean nextPage;
    private static ResourceLocation background;// = new ResourceLocation("tinker", "textures/gui/bookleft.png");

    public TiCTurnPageButton(int par1, int par2, int par3, boolean par4, BookData data) {
        super(par1, par2, par3, 23, 13, "");
        this.nextPage = par4;
        background = data.leftImage;
    }

    public void drawButton(Minecraft par1Minecraft, int par2, int par3) {
        if (this.visible) {
            boolean var4 = par2 >= this.xPosition && par3 >= this.yPosition
                    && par2 < this.xPosition + this.width
                    && par3 < this.yPosition + this.height;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            par1Minecraft.getTextureManager().bindTexture(background);
            int var5 = 0;
            int var6 = 192;

            if (var4) {
                var5 += 23;
            }

            if (!this.nextPage) {
                var6 += 13;
            }

            this.drawTexturedModalRect(this.xPosition, this.yPosition, var5, var6, 23, 13);
        }
    }

    public void drawButtonWithScale(Minecraft par1Minecraft, int par2, int par3, float scale) {
        if (this.visible) {
            this.width = (int) (23 * scale);
            this.height = (int) (13 * scale);

            boolean var4 = par2 >= this.xPosition && par3 >= this.yPosition
                    && par2 < this.xPosition + this.width
                    && par3 < this.yPosition + this.height;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            par1Minecraft.getTextureManager().bindTexture(background);
            int var5 = 0;
            int var6 = 192;

            if (var4) {
                var5 += 23;
            }

            if (!this.nextPage) {
                var6 += 13;
            }
            this.drawTexturedModalRect(
                    (int) (this.xPosition / scale),
                    (int) (this.yPosition / scale),
                    var5,
                    var6,
                    23,
                    13);

        }
    }

}
