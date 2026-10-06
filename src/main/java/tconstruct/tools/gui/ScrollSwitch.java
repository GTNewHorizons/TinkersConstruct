package tconstruct.tools.gui;

import static tconstruct.tools.gui.StationDraw.icons;
import static tconstruct.tools.gui.StationDraw.panelTexture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** The button that turns the side panels' scroll bars on; drawn sunk while they are on. */
@SideOnly(Side.CLIENT)
final class ScrollSwitch extends GuiButton {

    static final int CORNER = 23, TOP = 5;
    private static final int SIZE = 18;
    private static final int PICTURE_X = 7, PICTURE_Y = 2, PICTURE_H = 14;

    private final int frameV;
    private final GuiSliderWidget picture;
    /** Set by the screen before the buttons draw. */
    boolean on;

    /** (panelRight, panelTop) is the upper panel's top-right corner, in screen coordinates. */
    ScrollSwitch(int panelRight, int panelTop, StationTheme theme) {
        super(-1, panelRight - CORNER, panelTop + TOP, SIZE, SIZE, "");
        frameV = theme.buttonV;
        picture = InfoPanel.bar(theme.sliderU);
        picture.setPosition(xPosition + PICTURE_X, yPosition + PICTURE_Y);
        picture.setSize(PICTURE_H);
        // the thumb at the top
        picture.setSliderParameters(0, 1, 1);
    }

    /** The frame is picked by {@link #on}, not by enabled: the switch always takes a click. */
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        field_146123_n = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width
                && mouseY < yPosition + height;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(icons);
        // GuiButtonTool's frame cells: 144 pressed, 180 raised, 216 lit
        int u = on ? 144 : field_146123_n ? 216 : 180;
        drawTexturedModalRect(xPosition, yPosition, u, frameV, SIZE, SIZE);
        mc.getTextureManager().bindTexture(panelTexture);
        // the metal bar's rounded ends are half transparent
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        picture.draw();
        GL11.glDisable(GL11.GL_BLEND);
    }

    String tooltip() {
        return StatCollector.translateToLocal(on ? "gui.toolstation.scroll.on" : "gui.toolstation.scroll.off");
    }
}
