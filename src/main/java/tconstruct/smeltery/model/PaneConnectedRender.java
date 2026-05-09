package tconstruct.smeltery.model;

import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.WEST;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import tconstruct.smeltery.blocks.GlassPaneConnected;

@ThreadSafeISBRH(perThread = false)
public class PaneConnectedRender implements ISimpleBlockRenderingHandler {

    public static int model = RenderingRegistry.getNextAvailableRenderId();

    @Override
    public void renderInventoryBlock(Block block, int meta, int modelID, RenderBlocks renderer) {}

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        // Boilerplate partially copied from renderBlockPane / renderBlockStainedGlassPane
        Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        int multRGB = block.colorMultiplier(world, x, y, z);
        float multR = (float) (multRGB >> 16 & 255) / 255.0F;
        float multG = (float) (multRGB >> 8 & 255) / 255.0F;
        float multB = (float) (multRGB & 255) / 255.0F;

        if (EntityRenderer.anaglyphEnable) {
            float tempR = (multR * 30.0F + multG * 59.0F + multB * 11.0F) / 100.0F;
            float tempG = (multR * 30.0F + multG * 70.0F) / 100.0F;
            float tempB = (multR * 30.0F + multB * 70.0F) / 100.0F;
            multR = tempR;
            multG = tempG;
            multB = tempB;
        }

        tessellator.setColorOpaque_F(multR, multG, multB);

        // Fetch pane
        GlassPaneConnected pane = (GlassPaneConnected) block;

        boolean bottom = pane.shouldSideBeRendered(world, x, y - 1, z, 0);
        boolean top = pane.shouldSideBeRendered(world, x, y + 1, z, 1);
        boolean north = pane.canPaneConnectTo(world, x, y, z - 1, NORTH);
        boolean south = pane.canPaneConnectTo(world, x, y, z + 1, SOUTH);
        boolean west = pane.canPaneConnectTo(world, x - 1, y, z, WEST);
        boolean east = pane.canPaneConnectTo(world, x + 1, y, z, EAST);

        boolean cross = false;
        if (!north && !south && !west && !east) {
            cross = true;
            north = south = west = east = true;
        }

        IIcon bottomIcon = pane.getIcon(world, x, y, z, 0);
        IIcon topIcon = pane.getIcon(world, x, y, z, 1);
        IIcon northIcon = pane.getIcon(world, x, y, z, 2);
        IIcon southIcon = pane.getIcon(world, x, y, z, 3);
        IIcon westIcon = pane.getIcon(world, x, y, z, 4);
        IIcon eastIcon = pane.getIcon(world, x, y, z, 5);

        if (bottom) {
            renderTopOrBottom(tessellator, x, y, z, north, south, west, east, false, bottomIcon);
        }

        if (top) {
            renderTopOrBottom(tessellator, x, y, z, north, south, west, east, true, topIcon);
        }

        renderSide(tessellator, x, y, z, NORTH, east, west, north, cross, northIcon);
        renderSide(tessellator, x, y, z, SOUTH, west, east, south, cross, southIcon);
        renderSide(tessellator, x, y, z, WEST, north, south, west, cross, westIcon);
        renderSide(tessellator, x, y, z, EAST, south, north, east, cross, eastIcon);

        return true;
    }

    private void renderTopOrBottom(Tessellator tessellator, int x, int y, int z, boolean north, boolean south,
            boolean west, boolean east, boolean top, IIcon icon) {
        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();
        float startU = icon.getInterpolatedU(7);
        float endU = icon.getInterpolatedU(9);
        float startV = icon.getInterpolatedV(7);
        float endV = icon.getInterpolatedV(9);
        double startX = x + (7.0 / 16.0);
        double startZ = z + (7.0 / 16.0);
        double endX = x + (9.0 / 16.0);
        double endZ = z + (9.0 / 16.0);

        // All in top winding order - direction is reversed for bottom
        double[] centerSquareXZUV = new double[] { endX, startZ, endU, startV, startX, startZ, startU, startV, startX,
                endZ, startU, endV, endX, endZ, endU, endV, };
        double[] northXZUV = new double[] { endX, z, endU, minV, startX, z, startU, minV, startX, startZ, startU,
                startV, endX, startZ, endU, startV };
        double[] southXZUV = new double[] { endX, endZ, endU, endV, startX, endZ, startU, endV, startX, z + 1, startU,
                maxV, endX, z + 1, endU, maxV };
        double[] eastXZUV = new double[] { x + 1, startZ, maxU, startV, endX, startZ, endU, startV, endX, endZ, endU,
                endV, x + 1, endZ, maxU, endV };
        double[] westXZUV = new double[] { startX, startZ, startU, startV, x, startZ, minU, startV, x, endZ, minU, endV,
                startX, endZ, startU, endV };

        renderXZUV(tessellator, centerSquareXZUV, y, top);

        if (north) {
            renderXZUV(tessellator, northXZUV, y, top);
        }
        if (south) {
            renderXZUV(tessellator, southXZUV, y, top);
        }
        if (east) {
            renderXZUV(tessellator, eastXZUV, y, top);
        }
        if (west) {
            renderXZUV(tessellator, westXZUV, y, top);
        }
    }

    private void renderXZUV(Tessellator tessellator, double[] xzuv, double y, boolean top) {
        if (top) {
            for (int i = 0; i < xzuv.length; i += 4) {
                tessellator.addVertexWithUV(xzuv[i], y + 1, xzuv[i + 1], xzuv[i + 2], xzuv[i + 3]);
            }
        } else {
            for (int i = xzuv.length - 4; i >= 0; i -= 4) {
                tessellator.addVertexWithUV(xzuv[i], y - 0, xzuv[i + 1], xzuv[i + 2], xzuv[i + 3]);
            }
        }
    }

    private void renderSide(Tessellator tessellator, int x, int y, int z, ForgeDirection side, boolean left,
            boolean right, boolean front, boolean cross, IIcon icon) {
        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();
        float startU = icon.getInterpolatedU(7);
        float endU = icon.getInterpolatedU(9);
        double startX = x + (7.0 / 16.0);
        double startZ = z + (7.0 / 16.0);
        double endX = x + (9.0 / 16.0);
        double endZ = z + (9.0 / 16.0);

        switch (side) {
            case NORTH:
                double northZ = z + (7.0 / 16.0);
                if (right) {
                    tessellator.addVertexWithUV(x, y + 1, northZ, maxU, minV);
                    tessellator.addVertexWithUV(startX, y + 1, northZ, endU, minV);
                    tessellator.addVertexWithUV(startX, y, northZ, endU, maxV);
                    tessellator.addVertexWithUV(x, y, northZ, maxU, maxV);
                }

                if (left) {
                    tessellator.addVertexWithUV(endX, y + 1, northZ, startU, minV);
                    tessellator.addVertexWithUV(x + 1, y + 1, northZ, minU, minV);
                    tessellator.addVertexWithUV(x + 1, y, northZ, minU, maxV);
                    tessellator.addVertexWithUV(endX, y, northZ, startU, maxV);
                }

                if (cross) northZ = z;
                if (cross || !front) {
                    tessellator.addVertexWithUV(startX, y + 1, northZ, endU, minV);
                    tessellator.addVertexWithUV(endX, y + 1, northZ, startU, minV);
                    tessellator.addVertexWithUV(endX, y, northZ, startU, maxV);
                    tessellator.addVertexWithUV(startX, y, northZ, endU, maxV);
                }

                break;
            case SOUTH:
                double southZ = z + (9.0 / 16.0);
                if (right) {
                    tessellator.addVertexWithUV(x + 1, y + 1, southZ, maxU, minV);
                    tessellator.addVertexWithUV(endX, y + 1, southZ, endU, minV);
                    tessellator.addVertexWithUV(endX, y, southZ, endU, maxV);
                    tessellator.addVertexWithUV(x + 1, y, southZ, maxU, maxV);
                }

                if (left) {
                    tessellator.addVertexWithUV(startX, y + 1, southZ, startU, minV);
                    tessellator.addVertexWithUV(x, y + 1, southZ, minU, minV);
                    tessellator.addVertexWithUV(x, y, southZ, minU, maxV);
                    tessellator.addVertexWithUV(startX, y, southZ, startU, maxV);
                }

                if (cross) southZ = z + 1;
                if (cross || !front) {
                    tessellator.addVertexWithUV(endX, y + 1, southZ, endU, minV);
                    tessellator.addVertexWithUV(startX, y + 1, southZ, startU, minV);
                    tessellator.addVertexWithUV(startX, y, southZ, startU, maxV);
                    tessellator.addVertexWithUV(endX, y, southZ, endU, maxV);
                }

                break;
            case WEST:
                double westX = x + (7.0 / 16.0);
                if (right) {
                    tessellator.addVertexWithUV(westX, y + 1, z + 1, maxU, minV);
                    tessellator.addVertexWithUV(westX, y + 1, endZ, endU, minV);
                    tessellator.addVertexWithUV(westX, y, endZ, endU, maxV);
                    tessellator.addVertexWithUV(westX, y, z + 1, maxU, maxV);
                }

                if (left) {
                    tessellator.addVertexWithUV(westX, y + 1, startZ, startU, minV);
                    tessellator.addVertexWithUV(westX, y + 1, z, minU, minV);
                    tessellator.addVertexWithUV(westX, y, z, minU, maxV);
                    tessellator.addVertexWithUV(westX, y, startZ, startU, maxV);
                }

                if (cross) westX = x;
                if (cross || !front) {
                    tessellator.addVertexWithUV(westX, y + 1, endZ, endU, minV);
                    tessellator.addVertexWithUV(westX, y + 1, startZ, startU, minV);
                    tessellator.addVertexWithUV(westX, y, startZ, startU, maxV);
                    tessellator.addVertexWithUV(westX, y, endZ, endU, maxV);
                }

                break;
            case EAST:
                double eastX = x + (9.0 / 16.0);

                if (right) {
                    tessellator.addVertexWithUV(eastX, y + 1, z, maxU, minV);
                    tessellator.addVertexWithUV(eastX, y + 1, startZ, endU, minV);
                    tessellator.addVertexWithUV(eastX, y, startZ, endU, maxV);
                    tessellator.addVertexWithUV(eastX, y, z, maxU, maxV);
                }

                if (left) {
                    tessellator.addVertexWithUV(eastX, y + 1, endZ, startU, minV);
                    tessellator.addVertexWithUV(eastX, y + 1, z + 1, minU, minV);
                    tessellator.addVertexWithUV(eastX, y, z + 1, minU, maxV);
                    tessellator.addVertexWithUV(eastX, y, endZ, startU, maxV);
                }

                if (cross) eastX = x + 1;
                if (cross || !front) {
                    tessellator.addVertexWithUV(eastX, y + 1, startZ, endU, minV);
                    tessellator.addVertexWithUV(eastX, y + 1, endZ, startU, minV);
                    tessellator.addVertexWithUV(eastX, y, endZ, startU, maxV);
                    tessellator.addVertexWithUV(eastX, y, startZ, endU, maxV);
                }

                break;
        }
    }

    @Override
    public boolean shouldRender3DInInventory(int modelID) {
        return false;
    }

    @Override
    public int getRenderId() {
        return model;
    }
}
