package tconstruct.tools.gui;

import static tconstruct.tools.gui.StationDraw.icons;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.event.TextureStitchEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.client.ToolGuiElement;
import tconstruct.tools.model.TexelMasks;

/** The hint icons an empty build cell shows, stretched to fill the cell. */
@SideOnly(Side.CLIENT)
public final class SlotHints {

    /** 18 px cells per side of icons.png; the last 4 px of the sheet hold none. */
    private static final int GRID = 256 / 18;
    private static final float[] WHOLE_CELL = { 0, 0, 18, 18 };
    /** A hint's longer side is stretched to an item's 16 px, so a thin crossbar reads as well as a plate. */
    private static final int HINT_SIZE = 16;
    /** Measured from icons.png on first use; null again after a resource reload. */
    private static float[][] boxes;

    @SubscribeEvent
    public void onStitch(TextureStitchEvent.Post event) {
        boxes = null;
    }

    /** The icon's box in its cell's 18 px; the whole cell when it cannot be measured. */
    private static float[] hintBox(int iconColumn, int iconRow) {
        if (iconColumn < 0 || iconRow < 0 || iconColumn >= GRID || iconRow >= GRID) return WHOLE_CELL;
        if (boxes == null) boxes = measure();
        return boxes[iconColumn + GRID * iconRow];
    }

    private static float[][] measure() {
        float[][] measured = new float[GRID * GRID][];
        Arrays.fill(measured, WHOLE_CELL);
        BufferedImage sheet;
        try (InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(icons).getInputStream()) {
            sheet = ImageIO.read(in);
        } catch (IOException | RuntimeException e) {
            return measured;
        }
        if (sheet == null) return measured;
        for (int row = 0; row < GRID; row++) {
            for (int column = 0; column < GRID; column++) measured[column + GRID * row] = boxOf(sheet, column, row);
        }
        return measured;
    }

    /** The box of the texels the GUI's alpha test keeps, in the cell's 18 px. */
    private static float[] boxOf(BufferedImage sheet, int column, int row) {
        // a resource pack may ship the sheet at a higher resolution
        float unitX = sheet.getWidth() / 256F, unitY = sheet.getHeight() / 256F;
        int x0 = Math.round(column * 18 * unitX), y0 = Math.round(row * 18 * unitY);
        int w = Math.round(18 * unitX), h = Math.round(18 * unitY);
        if (x0 + w > sheet.getWidth() || y0 + h > sheet.getHeight()) return WHOLE_CELL;
        int left = w, top = h, right = -1, bottom = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((sheet.getRGB(x0 + x, y0 + y) >>> 24) < TexelMasks.OPAQUE) continue;
                left = Math.min(left, x);
                top = Math.min(top, y);
                right = Math.max(right, x);
                bottom = Math.max(bottom, y);
            }
        }
        if (right < 0) return WHOLE_CELL;
        return new float[] { left / unitX, top / unitY, (right + 1) / unitX, (bottom + 1) / unitY };
    }

    /** Centered on (cx, cy), turned as {@link ToolGuiElement#turnOf} says; expects icons.png bound, the color set. */
    static void drawStretchedHint(float zLevel, int iconColumn, int iconRow, int cx, int cy, int turn) {
        float[] box = hintBox(iconColumn, iconRow);
        float boxW = box[2] - box[0], boxH = box[3] - box[1];
        float scale = HINT_SIZE / Math.max(boxW, boxH);
        boolean sideways = (turn & 1) != 0;
        int w = Math.round((sideways ? boxH : boxW) * scale), h = Math.round((sideways ? boxW : boxH) * scale);
        int x = cx - w / 2, y = cy - h / 2;
        // corners run top-left, top-right, bottom-right, bottom-left; screen corner k shows icon corner from[k]:
        // a mirror swaps left and right first, then each quarter turn to the left moves one corner on
        int[] from = new int[4];
        for (int k = 0; k < 4; k++) {
            int corner = (k + turn) & 3;
            from[k] = (turn & ToolGuiElement.MIRROR) != 0 ? corner ^ 1 : corner;
        }
        float s = 1F / 256;
        double u0 = (iconColumn * 18 + box[0]) * s, u1 = (iconColumn * 18 + box[2]) * s;
        double v0 = (iconRow * 18 + box[1]) * s, v1 = (iconRow * 18 + box[3]) * s;
        double[] u = { u0, u1, u1, u0 }, v = { v0, v0, v1, v1 };
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x, y + h, zLevel, u[from[3]], v[from[3]]);
        tess.addVertexWithUV(x + w, y + h, zLevel, u[from[2]], v[from[2]]);
        tess.addVertexWithUV(x + w, y, zLevel, u[from[1]], v[from[1]]);
        tess.addVertexWithUV(x, y, zLevel, u[from[0]], v[from[0]]);
        tess.draw();
    }
}
