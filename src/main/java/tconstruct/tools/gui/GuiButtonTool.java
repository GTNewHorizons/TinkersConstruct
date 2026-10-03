package tconstruct.tools.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.client.ToolGuiElement;

@SideOnly(Side.CLIENT)
public class GuiButtonTool extends GuiButton {

    /**
     * True for pointing right (next page), false for pointing left (previous page).
     */
    int textureX;

    int textureY;
    public String texture;
    public ToolGuiElement element;
    /** The row of icons.png the button frame is drawn from. */
    public int backgroundV = 216;
    /** The frame always comes from this sheet: an addon's own icons.png has the frame only at row 216. */
    private static final ResourceLocation frames = new ResourceLocation("tinker", "textures/gui/icons.png");
    private final ResourceLocation background; // = new
    // ResourceLocation("tinker",
    // "textures/gui/armorextended.png");

    public GuiButtonTool(int id, int posX, int posY, int texX, int texY, String domain, String tex, ToolGuiElement e) {
        super(id, posX, posY, 18, 18, "");
        textureX = texX;
        textureY = texY;
        texture = tex;
        element = e;
        background = new ResourceLocation(domain, tex);
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            boolean var4 = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;
            int var5 = this.getHoverState(this.field_146123_n);
            int index = 18 * getHoverState(field_146123_n);
            mc.getTextureManager().bindTexture(frames);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 144 + index * 2, backgroundV, 18, 18);
            mc.getTextureManager().bindTexture(background);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, textureX * 18, textureY * 18, 18, 18);
        }
    }
}
