package tconstruct.tools.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.opengl.GL11;

public class GuiButtonDump extends GuiButton {

    public GuiButtonDump(int id, int x, int y) {
        super(id, x, y, 12, 12, "D");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRenderer;
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            int color = this.enabled ? (this.field_146123_n ? 0xFFAAAAAA : 0xFF555555) : 0xFF333333;
            drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, color);

            drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + 1, 0xFF000000); // Top
            drawRect(this.xPosition, this.yPosition, this.xPosition + 1, this.yPosition + this.height, 0xFF000000); // Left
            drawRect(
                    this.xPosition + this.width - 1,
                    this.yPosition,
                    this.xPosition + this.width,
                    this.yPosition + this.height,
                    0xFF000000); // Right
            drawRect(
                    this.xPosition,
                    this.yPosition + this.height - 1,
                    this.xPosition + this.width,
                    this.yPosition + this.height,
                    0xFF000000); // Bottom

            int textColor = this.enabled ? 0xFFFFFF : 0x666666;
            this.drawCenteredString(
                    fontrenderer,
                    this.displayString,
                    this.xPosition + this.width / 2,
                    this.yPosition + (this.height - 8) / 2,
                    textColor);
        }
    }
}
