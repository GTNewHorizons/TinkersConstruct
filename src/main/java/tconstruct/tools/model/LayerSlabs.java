package tconstruct.tools.model;

import static tconstruct.tools.model.BuildPreview.BUILD_SCALE;
import static tconstruct.tools.model.BuildPreview.NO_PART;
import static tconstruct.tools.model.TableItems.HAIR;

import java.util.Arrays;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.tools.BuildInput;
import tconstruct.library.tools.ToolCore;

/** The layers of a half-built tool as slabs, each texel drawn once by the topmost layer that shows it. */
@SideOnly(Side.CLIENT)
final class LayerSlabs {

    private LayerSlabs() {}

    /** Row, column and the two diagonals. */
    private static final int[][] WAYS = { { 1, 0 }, { 0, 1 }, { 1, 1 }, { 1, -1 } };

    /**
     * Which layer draws each texel, -1 for none. A placed part leaves a hole for a missing part whose shape sits wholly
     * inside its own, and for a missing second copy of itself, except where its own texels run on through.
     */
    static int[] owners(ToolCore tool, int[] materials, IIcon[] icons, boolean[][] masks, int width, int height) {
        Item[] partByPass = new Item[BuildInput.VALUES.length];
        for (BuildInput kind : BuildInput.VALUES) partByPass[kind.renderPass] = kind.partOf(tool);
        int[] owner = new int[width * height];
        Arrays.fill(owner, -1);
        for (int pass = masks.length - 1; pass >= 0; pass--) {
            boolean[] mask = masks[pass];
            if (mask == null) continue;
            boolean[] shown = mask.clone();
            boolean[] copy = null;
            for (int above = pass + 1; above < masks.length; above++) {
                if (materials[above] != NO_PART || icons[above] == null) continue;
                boolean[] hole = TexelMasks.onGrid(icons[above], width, height);
                if (hole == null) continue;
                if (TexelMasks.inset(hole, mask, width, height)) {
                    for (int i = 0; i < shown.length; i++) shown[i] &= !hole[i];
                } else if (partByPass[pass] != null && partByPass[pass] == partByPass[above]) {
                    if (copy == null) copy = new boolean[mask.length];
                    for (int i = 0; i < copy.length; i++) copy[i] |= hole[i];
                }
            }
            if (copy != null) {
                boolean[] kept = new boolean[shown.length];
                for (int i = 0; i < kept.length; i++) kept[i] = shown[i] && !copy[i];
                for (int i = 0; i < shown.length; i++) {
                    if (shown[i] && copy[i]) shown[i] = runsThrough(kept, width, height, i % width, i / width);
                }
            }
            for (int i = 0; i < owner.length; i++) if (shown[i] && owner[i] < 0) owner[i] = pass;
        }
        return owner;
    }

    private static boolean runsThrough(boolean[] kept, int width, int height, int x, int y) {
        for (int[] way : WAYS) {
            if (at(kept, width, height, x + way[0], y + way[1]) && at(kept, width, height, x - way[0], y - way[1])) {
                return true;
            }
        }
        return false;
    }

    /** One layer's texels as a slab: top faces over each run in a row, walls only toward texels no layer draws. */
    static void drawLayer(IIcon icon, int pass, float thickness, int[] owner, int width, int height) {
        float minU = icon.getMinU(), maxU = icon.getMaxU(), minV = icon.getMinV(), maxV = icon.getMaxV();
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        for (int y = 0; y < height; y++) {
            // image rows run top down, local y runs up
            float y0 = (float) (height - 1 - y) / height, y1 = (float) (height - y) / height;
            float v0 = minV + (maxV - minV) * y / height, v1 = minV + (maxV - minV) * (y + 1) / height;
            int x = 0;
            while (x < width) {
                if (owner[y * width + x] != pass) {
                    x++;
                    continue;
                }
                int end = x + 1;
                while (end < width && owner[y * width + end] == pass) end++;
                // a flipped icon has minU > maxU and comes out mirrored, as drawn
                float x0 = (float) x / width, x1 = (float) end / width;
                float u0 = minU + (maxU - minU) * x / width, u1 = minU + (maxU - minU) * end / width;
                tess.setNormal(0F, 0F, 1F);
                tess.addVertexWithUV(x0, y0, 0, u0, v1);
                tess.addVertexWithUV(x1, y0, 0, u1, v1);
                tess.addVertexWithUV(x1, y1, 0, u1, v0);
                tess.addVertexWithUV(x0, y1, 0, u0, v0);
                for (; x < end; x++) drawWalls(tess, owner, width, height, x, y, minU, maxU, v0, v1, y0, y1, thickness);
            }
        }
        tess.draw();
    }

    private static void drawWalls(Tessellator tess, int[] owner, int width, int height, int x, int y, float minU,
            float maxU, float v0, float v1, float y0, float y1, float thickness) {
        float x0 = (float) x / width, x1 = (float) (x + 1) / width;
        float u0 = minU + (maxU - minU) * x / width, u1 = minU + (maxU - minU) * (x + 1) / width;
        float uc = (u0 + u1) / 2F, vc = (v0 + v1) / 2F;
        if (!covered(owner, width, height, x - 1, y)) {
            tess.setNormal(-1F, 0F, 0F);
            tess.addVertexWithUV(x0, y0, -thickness, uc, vc);
            tess.addVertexWithUV(x0, y0, 0, uc, vc);
            tess.addVertexWithUV(x0, y1, 0, uc, vc);
            tess.addVertexWithUV(x0, y1, -thickness, uc, vc);
        }
        if (!covered(owner, width, height, x + 1, y)) {
            tess.setNormal(1F, 0F, 0F);
            tess.addVertexWithUV(x1, y1, -thickness, uc, vc);
            tess.addVertexWithUV(x1, y1, 0, uc, vc);
            tess.addVertexWithUV(x1, y0, 0, uc, vc);
            tess.addVertexWithUV(x1, y0, -thickness, uc, vc);
        }
        if (!covered(owner, width, height, x, y - 1)) {
            tess.setNormal(0F, 1F, 0F);
            tess.addVertexWithUV(x0, y1, 0, uc, vc);
            tess.addVertexWithUV(x1, y1, 0, uc, vc);
            tess.addVertexWithUV(x1, y1, -thickness, uc, vc);
            tess.addVertexWithUV(x0, y1, -thickness, uc, vc);
        }
        if (!covered(owner, width, height, x, y + 1)) {
            tess.setNormal(0F, -1F, 0F);
            tess.addVertexWithUV(x1, y0, 0, uc, vc);
            tess.addVertexWithUV(x0, y0, 0, uc, vc);
            tess.addVertexWithUV(x0, y0, -thickness, uc, vc);
            tess.addVertexWithUV(x1, y0, -thickness, uc, vc);
        }
    }

    /** For a tool whose textures cannot be read: each layer whole, a hair above the one below. */
    static void drawWhole(IIcon icon, int pass, float thickness) {
        GL11.glPushMatrix();
        // local z is up once the display lies flat
        GL11.glTranslatef(0F, 0F, pass * HAIR / BUILD_SCALE);
        // U arguments swapped, as in LooseItems.renderFlatItem
        ItemRenderer.renderItemIn2D(
                Tessellator.instance,
                icon.getMinU(),
                icon.getMinV(),
                icon.getMaxU(),
                icon.getMaxV(),
                icon.getIconWidth(),
                icon.getIconHeight(),
                thickness);
        GL11.glPopMatrix();
    }

    private static boolean at(boolean[] mask, int width, int height, int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height && mask[y * width + x];
    }

    private static boolean covered(int[] owner, int width, int height, int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height && owner[y * width + x] >= 0;
    }
}
