package tconstruct.tools.gui;

import static tconstruct.tools.gui.StationButtons.COLUMN_W;
import static tconstruct.tools.gui.StationButtons.drawHooks;
import static tconstruct.tools.gui.StationPanels.PANEL_X;
import static tconstruct.tools.gui.StationSlotLayouts.CELL;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** The screen's sheets, and the pieces drawn from them in more than one place: beams, cell frames, panel frames. */
@SideOnly(Side.CLIENT)
final class StationDraw {

    private StationDraw() {}

    static final ResourceLocation background = new ResourceLocation("tinker", "textures/gui/toolstation.png");
    static final ResourceLocation icons = new ResourceLocation("tinker", "textures/gui/icons.png");
    static final ResourceLocation panelTexture = new ResourceLocation("tinker", "textures/gui/panel.png");
    private static final ResourceLocation beamsTexture = new ResourceLocation("tinker", "textures/gui/beams.png");
    static final int BEAM_CAP = 2;
    private static final int BEAM_MIDDLE = 129, BEAM_H = 7, BEAMS_W = BEAM_CAP + BEAM_MIDDLE + BEAM_CAP,
            BEAMS_H = 2 * BEAM_H;

    private static final GuiElementScalable BEAM_LEFT = piece(0, 0, BEAM_CAP, BEAM_H);
    private static final GuiElementScalable BEAM_CENTER = piece(BEAM_CAP, 0, BEAM_MIDDLE, BEAM_H);
    private static final GuiElementScalable BEAM_RIGHT = piece(BEAM_CAP + BEAM_MIDDLE, 0, BEAM_CAP, BEAM_H);
    private static final Map<StationTheme, Beam> BEAMS = new EnumMap<>(StationTheme.class);

    static {
        for (StationTheme theme : StationTheme.values()) BEAMS.put(theme, new Beam(theme.beamV));
    }

    private static final class Beam {

        final GuiElementDuex left, right;
        final GuiElementScalable center;

        Beam(int v) {
            left = BEAM_LEFT.shift(0, v);
            center = BEAM_CENTER.shift(0, v);
            right = BEAM_RIGHT.shift(0, v);
        }
    }

    /** The four-argument constructor reads a static default that every six-argument call overwrites. */
    private static GuiElementScalable piece(int u, int v, int w, int h) {
        return new GuiElementScalable(u, v, w, h, BEAMS_W, BEAMS_H);
    }

    /** The side panels, the beams and the hooks. Mouse coordinates are the screen's, untranslated. */
    static void drawFrames(ToolStationGui screen, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        screen.mc.getTextureManager().bindTexture(panelTexture);
        screen.panels.upper.draw(screen.guiLeft, screen.guiTop, mouseX, mouseY);
        screen.panels.lower.draw(screen.guiLeft, screen.guiTop, mouseX, mouseY);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        screen.mc.getTextureManager().bindTexture(beamsTexture);
        drawBeam(screen.theme(), screen.guiLeft, screen.guiTop, COLUMN_W);
        drawBeam(
                screen.theme(),
                screen.guiLeft + PANEL_X - BEAM_CAP,
                screen.guiTop,
                screen.panels.panelWidth + 2 * BEAM_CAP);

        screen.mc.getTextureManager().bindTexture(background);
        drawHooks(screen, screen.theme());
    }

    /** A cell's 1 px border: each side of the 18 px slot frame in icons.png, stretched. Expects icons.png bound. */
    static void drawCellFrame(float zLevel, int x, int y) {
        int far = CELL - 1;
        drawStretched(zLevel, x, y, far, 1, 144, 216, 17, 1);
        drawStretched(zLevel, x + far, y, 1, 1, 161, 216, 1, 1);
        drawStretched(zLevel, x, y + 1, 1, far - 1, 144, 217, 1, 16);
        drawStretched(zLevel, x, y + far, 1, 1, 144, 233, 1, 1);
        drawStretched(zLevel, x + 1, y + far, far, 1, 145, 233, 17, 1);
        drawStretched(zLevel, x + far, y + 1, 1, far - 1, 161, 217, 1, 16);
    }

    /** A beam w px wide, a cap at each end. Expects beams.png bound. */
    static void drawBeam(StationTheme theme, int x, int y, int w) {
        Beam beam = BEAMS.get(theme);
        x += beam.left.draw(x, y);
        x += beam.center.drawScaledX(x, y, w - 2 * BEAM_CAP);
        beam.right.draw(x, y);
    }

    /** A region of a 256 px sheet stretched to w x h, in the current color. */
    static void drawStretched(float zLevel, int x, int y, int w, int h, int u, int v, int uw, int vh) {
        float s = 1.0F / 256;
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x, y + h, zLevel, u * s, (v + vh) * s);
        tess.addVertexWithUV(x + w, y + h, zLevel, (u + uw) * s, (v + vh) * s);
        tess.addVertexWithUV(x + w, y, zLevel, (u + uw) * s, v * s);
        tess.addVertexWithUV(x, y, zLevel, u * s, v * s);
        tess.draw();
    }
}
