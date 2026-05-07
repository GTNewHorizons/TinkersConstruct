package tconstruct.library.util;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import tconstruct.client.pages.TiCButtonBookPage;

public class TiCTurnPageButton extends TiCGuiButton {

    public enum ButtonType {

        nextPage(0, 16, 18, 10, "nextPage"),
        previousPage(0, 26, 18, 10, "previousPage"),
        backToJumpFrom(0, 36, 18, 10, "backToJumpFrom"),
        homePage(0, 0, 14, 16, "homePage");

        int textureX;
        int textureY;
        int textureWidth;
        int textureHeight;
        List<String> tooltips;

        ButtonType(int textureX, int textureY, int textureWidth, int textureHeight, String tooltips) {
            this.textureX = textureX;
            this.textureY = textureY;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
            this.tooltips = Arrays
                    .asList(StatCollector.translateToLocal("tconstruct.manual.button.tooltip." + tooltips));
        }

    }

    public static int ARROWCOLOR = 0xFFFFD3;
    public static int ARROWCOLORHOVER = 0xFF541C;

    private final ButtonType buttonType;
    private static final ResourceLocation background = new ResourceLocation(
            "tinker",
            "textures/gui/bookleftbackground.png");

    public TiCTurnPageButton(int id, int xPosition, int yPosition, ButtonType buttonType,
            TiCButtonBookPage parentPage) {
        super(id, xPosition, yPosition, buttonType.textureWidth, buttonType.textureHeight, "", parentPage);
        this.buttonType = buttonType;
    }

    public int getWidth() {
        return this.buttonType.textureWidth;
    }

    public int getHeight() {
        return this.buttonType.textureHeight;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        this.drawButtonWithScale(mc, mouseX, mouseY, 1.0f);
    }

    public void drawButtonWithScale(Minecraft mc, int mouseX, int mouseY, float scale) {
        if (this.visible) {
            this.width = (int) (this.buttonType.textureWidth * scale);
            this.height = (int) (this.buttonType.textureHeight * scale);

            boolean isMouseInButton = this.isHover(mouseX, mouseY);

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

    @Override
    public List<String> getTooltips() {
        return this.buttonType.tooltips;
    }
}
