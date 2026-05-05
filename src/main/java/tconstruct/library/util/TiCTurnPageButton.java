package tconstruct.library.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import mantle.books.BookData;

public class TiCTurnPageButton extends GuiButton {

    enum ButtonType {

        nextPage(0, 16, 18, 10),
        previousPage(0, 26, 18, 10),
        backToJumpFrom(0, 36, 18, 10),
        homePage(0, 0, 14, 16);

        int textureX;
        int textureY;
        int textureWidth;
        int textureHeight;

        ButtonType(int textureX, int textureY, int textureWidth, int textureHeight) {
            this.textureX = textureX;
            this.textureY = textureY;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
        }

    }

    public static int ARROWCOLOR = 0xFFFFD3;
    public static int ARROWCOLORHOVER = 0xFF541C;

    private final ButtonType buttonType;
    private static final ResourceLocation background = new ResourceLocation(
            "tinker",
            "textures/gui/bookleftbackground.png");

    public TiCTurnPageButton(int id, int xPosition, int yPosition, ButtonType buttonType, BookData data) {
        super(id, xPosition, yPosition, buttonType.textureWidth, buttonType.textureHeight, "");
        this.buttonType = buttonType;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        this.drawButtonWithScale(mc, mouseX, mouseY, 1.0f);
    }

    public void drawButtonWithScale(Minecraft mc, int mouseX, int mouseY, float scale) {
        if (this.visible) {
            this.width = (int) (this.buttonType.textureWidth * scale);
            this.height = (int) (this.buttonType.textureHeight * scale);

            boolean isMouseInButton = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;

            mc.getTextureManager().bindTexture(background);

            if (isMouseInButton) {
                GL11.glColor4f(255f / 255, 84f / 255, 28f / 255, 1.0F);
            } else {
                GL11.glColor4f(255f / 255, 255f / 255, 221f / 255, 1.0F);
            }

            this.drawTexturedModalRect(
                    (int) (this.xPosition / scale),
                    (int) (this.yPosition / scale),
                    this.buttonType.textureX,
                    this.buttonType.textureY,
                    this.buttonType.textureWidth,
                    this.buttonType.textureHeight);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

}
