package tconstruct.tools.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiButtonDump extends GuiButton {

    private static final ResourceLocation TEXTURE = new ResourceLocation("tinker", "textures/gui/dump_button.png");

    public GuiButtonDump(int id, int x, int y) {
        super(id, x, y, 10, 10, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;

            float brightness = this.enabled ? (this.field_146123_n ? 1.0F : 0.8F) : 0.5F;
            GL11.glColor4f(brightness, brightness, brightness, 1.0F);

            mc.getTextureManager().bindTexture(TEXTURE);
            func_146110_a(this.xPosition, this.yPosition, 0, 0, this.width, this.height, this.width, this.height);
        }
    }
}
