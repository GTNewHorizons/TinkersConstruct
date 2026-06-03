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

        boolean rawNorth = pane.canPaneConnectTo(world, x, y, z - 1, NORTH);
        boolean rawSouth = pane.canPaneConnectTo(world, x, y, z + 1, SOUTH);
        boolean rawWest = pane.canPaneConnectTo(world, x - 1, y, z, WEST);
        boolean rawEast = pane.canPaneConnectTo(world, x + 1, y, z, EAST);
        boolean cross = !rawNorth && !rawSouth && !rawWest && !rawEast;
        boolean north = cross || rawNorth;
        boolean south = cross || rawSouth;
        boolean west = cross || rawWest;
        boolean east = cross || rawEast;

        // Per-segment visibility for the top and bottom faces. Whole-face culling is too coarse:
        // stacked panes with mismatched arm configs (e.g. an E-W line below an N-S line) leave
        // arm tops with nothing covering them, so they have to render even though the centers
        // do not.
        int bottomSegments = pane.getVisibleVerticalSegments(world, x, y, z, -1, north, south, west, east);
        int topSegments = pane.getVisibleVerticalSegments(world, x, y, z, 1, north, south, west, east);

        IIcon bottomIcon = pane.getIcon(world, x, y, z, 0);
        IIcon topIcon = pane.getIcon(world, x, y, z, 1);

        boolean connEast = pane.shouldConnectToBlock(
                world,
                x,
                y,
                z,
                world.getBlock(x + 1, y, z),
                world.getBlockMetadata(x + 1, y, z));
        boolean connWest = pane.shouldConnectToBlock(
                world,
                x,
                y,
                z,
                world.getBlock(x - 1, y, z),
                world.getBlockMetadata(x - 1, y, z));
        boolean connNorth = pane.shouldConnectToBlock(
                world,
                x,
                y,
                z,
                world.getBlock(x, y, z - 1),
                world.getBlockMetadata(x, y, z - 1));
        boolean connSouth = pane.shouldConnectToBlock(
                world,
                x,
                y,
                z,
                world.getBlock(x, y, z + 1),
                world.getBlockMetadata(x, y, z + 1));

        // Each side face is split into three strips (two arms + center). Per-strip vertical
        // openness mirrors the corresponding segment's bit in {top,bottom}Segments — i.e. the
        // exact same culling that decides whether the top/bottom face is rendered. In the cross
        // case the "center" strip is actually the front-facing arm's edge, so it uses that
        // arm's segment instead of SEGMENT_CENTER.
        int northCenterSeg = cross ? GlassPaneConnected.SEGMENT_NORTH : GlassPaneConnected.SEGMENT_CENTER;
        int southCenterSeg = cross ? GlassPaneConnected.SEGMENT_SOUTH : GlassPaneConnected.SEGMENT_CENTER;
        int westCenterSeg = cross ? GlassPaneConnected.SEGMENT_WEST : GlassPaneConnected.SEGMENT_CENTER;
        int eastCenterSeg = cross ? GlassPaneConnected.SEGMENT_EAST : GlassPaneConnected.SEGMENT_CENTER;

        IIcon northLeftIcon = pane.getPaneSegmentTexture(
                world, x, y, z, GlassPaneConnected.SEGMENT_EAST, topSegments, bottomSegments, connEast, connWest);
        IIcon northRightIcon = pane.getPaneSegmentTexture(
                world, x, y, z, GlassPaneConnected.SEGMENT_WEST, topSegments, bottomSegments, connEast, connWest);
        IIcon northCenterIcon = pane.getPaneSegmentTexture(
                world, x, y, z, northCenterSeg, topSegments, bottomSegments, connEast, connWest);

        IIcon southLeftIcon = pane.getPaneSegmentTexture(
                world, x, y, z, GlassPaneConnected.SEGMENT_WEST, topSegments, bottomSegments, connWest, connEast);
        IIcon southRightIcon = pane.getPaneSegmentTexture(
                world, x, y, z, GlassPaneConnected.SEGMENT_EAST, topSegments, bottomSegments, connWest, connEast);
        IIcon southCenterIcon = pane.getPaneSegmentTexture(
                world, x, y, z, southCenterSeg, topSegments, bottomSegments, connWest, connEast);

        IIcon westLeftIcon = pane.getPaneSegmentTexture(
                world, x, y, z, GlassPaneConnected.SEGMENT_NORTH, topSegments, bottomSegments, connNorth, connSouth);
        IIcon westRightIcon = pane.getPaneSegmentTexture(
                world, x, y, z, GlassPaneConnected.SEGMENT_SOUTH, topSegments, bottomSegments, connNorth, connSouth);
        IIcon westCenterIcon = pane.getPaneSegmentTexture(
                world, x, y, z, westCenterSeg, topSegments, bottomSegments, connNorth, connSouth);

        IIcon eastLeftIcon = pane.getPaneSegmentTexture(
                world, x, y, z, GlassPaneConnected.SEGMENT_SOUTH, topSegments, bottomSegments, connSouth, connNorth);
        IIcon eastRightIcon = pane.getPaneSegmentTexture(
                world, x, y, z, GlassPaneConnected.SEGMENT_NORTH, topSegments, bottomSegments, connSouth, connNorth);
        IIcon eastCenterIcon = pane.getPaneSegmentTexture(
                world, x, y, z, eastCenterSeg, topSegments, bottomSegments, connSouth, connNorth);

        renderTopOrBottom(tessellator, x, y, z, bottomSegments, false, bottomIcon);
        renderTopOrBottom(tessellator, x, y, z, topSegments, true, topIcon);

        renderSide(tessellator, x, y, z, NORTH, east, west, north, cross,
                northLeftIcon, northRightIcon, northCenterIcon);
        renderSide(tessellator, x, y, z, SOUTH, west, east, south, cross,
                southLeftIcon, southRightIcon, southCenterIcon);
        renderSide(tessellator, x, y, z, WEST, north, south, west, cross,
                westLeftIcon, westRightIcon, westCenterIcon);
        renderSide(tessellator, x, y, z, EAST, south, north, east, cross,
                eastLeftIcon, eastRightIcon, eastCenterIcon);

        return true;
    }

    private void renderTopOrBottom(Tessellator tessellator, int x, int y, int z, int segments, boolean top,
            IIcon icon) {
        if ((segments & ~GlassPaneConnected.SEGMENT_CONNECTED) == 0) {
            return;
        }

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

        if ((segments & GlassPaneConnected.SEGMENT_CENTER) != 0) {
            renderXZUV(tessellator, centerSquareXZUV, y, top);
        }
        if ((segments & GlassPaneConnected.SEGMENT_NORTH) != 0) {
            renderXZUV(tessellator, northXZUV, y, top);
        }
        if ((segments & GlassPaneConnected.SEGMENT_SOUTH) != 0) {
            renderXZUV(tessellator, southXZUV, y, top);
        }
        if ((segments & GlassPaneConnected.SEGMENT_EAST) != 0) {
            renderXZUV(tessellator, eastXZUV, y, top);
        }
        if ((segments & GlassPaneConnected.SEGMENT_WEST) != 0) {
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
                tessellator.addVertexWithUV(xzuv[i], y, xzuv[i + 1], xzuv[i + 2], xzuv[i + 3]);
            }
        }
    }

    private void renderSide(Tessellator tessellator, int x, int y, int z, ForgeDirection side, boolean left,
            boolean right, boolean front, boolean cross,
            IIcon leftIcon, IIcon rightIcon, IIcon centerIcon) {
        double startX = x + (7.0 / 16.0);
        double startZ = z + (7.0 / 16.0);
        double endX = x + (9.0 / 16.0);
        double endZ = z + (9.0 / 16.0);

        switch (side) {
            case NORTH:
                double northZ = z + (7.0 / 16.0);
                if (right) {
                    float minV = rightIcon.getMinV();
                    float maxV = rightIcon.getMaxV();
                    float maxU = rightIcon.getMaxU();
                    float endU = rightIcon.getInterpolatedU(9);
                    tessellator.addVertexWithUV(x, y + 1, northZ, maxU, minV);
                    tessellator.addVertexWithUV(startX, y + 1, northZ, endU, minV);
                    tessellator.addVertexWithUV(startX, y, northZ, endU, maxV);
                    tessellator.addVertexWithUV(x, y, northZ, maxU, maxV);
                }

                if (left) {
                    float minV = leftIcon.getMinV();
                    float maxV = leftIcon.getMaxV();
                    float minU = leftIcon.getMinU();
                    float startU = leftIcon.getInterpolatedU(7);
                    tessellator.addVertexWithUV(endX, y + 1, northZ, startU, minV);
                    tessellator.addVertexWithUV(x + 1, y + 1, northZ, minU, minV);
                    tessellator.addVertexWithUV(x + 1, y, northZ, minU, maxV);
                    tessellator.addVertexWithUV(endX, y, northZ, startU, maxV);
                }

                if (cross) northZ = z;
                if (cross || !front) {
                    float minV = centerIcon.getMinV();
                    float maxV = centerIcon.getMaxV();
                    float startU = centerIcon.getInterpolatedU(7);
                    float endU = centerIcon.getInterpolatedU(9);
                    tessellator.addVertexWithUV(startX, y + 1, northZ, endU, minV);
                    tessellator.addVertexWithUV(endX, y + 1, northZ, startU, minV);
                    tessellator.addVertexWithUV(endX, y, northZ, startU, maxV);
                    tessellator.addVertexWithUV(startX, y, northZ, endU, maxV);
                }

                break;
            case SOUTH:
                double southZ = z + (9.0 / 16.0);
                if (right) {
                    float minV = rightIcon.getMinV();
                    float maxV = rightIcon.getMaxV();
                    float maxU = rightIcon.getMaxU();
                    float endU = rightIcon.getInterpolatedU(9);
                    tessellator.addVertexWithUV(x + 1, y + 1, southZ, maxU, minV);
                    tessellator.addVertexWithUV(endX, y + 1, southZ, endU, minV);
                    tessellator.addVertexWithUV(endX, y, southZ, endU, maxV);
                    tessellator.addVertexWithUV(x + 1, y, southZ, maxU, maxV);
                }

                if (left) {
                    float minV = leftIcon.getMinV();
                    float maxV = leftIcon.getMaxV();
                    float minU = leftIcon.getMinU();
                    float startU = leftIcon.getInterpolatedU(7);
                    tessellator.addVertexWithUV(startX, y + 1, southZ, startU, minV);
                    tessellator.addVertexWithUV(x, y + 1, southZ, minU, minV);
                    tessellator.addVertexWithUV(x, y, southZ, minU, maxV);
                    tessellator.addVertexWithUV(startX, y, southZ, startU, maxV);
                }

                if (cross) southZ = z + 1;
                if (cross || !front) {
                    float minV = centerIcon.getMinV();
                    float maxV = centerIcon.getMaxV();
                    float startU = centerIcon.getInterpolatedU(7);
                    float endU = centerIcon.getInterpolatedU(9);
                    tessellator.addVertexWithUV(endX, y + 1, southZ, endU, minV);
                    tessellator.addVertexWithUV(startX, y + 1, southZ, startU, minV);
                    tessellator.addVertexWithUV(startX, y, southZ, startU, maxV);
                    tessellator.addVertexWithUV(endX, y, southZ, endU, maxV);
                }

                break;
            case WEST:
                double westX = x + (7.0 / 16.0);
                if (right) {
                    float minV = rightIcon.getMinV();
                    float maxV = rightIcon.getMaxV();
                    float maxU = rightIcon.getMaxU();
                    float endU = rightIcon.getInterpolatedU(9);
                    tessellator.addVertexWithUV(westX, y + 1, z + 1, maxU, minV);
                    tessellator.addVertexWithUV(westX, y + 1, endZ, endU, minV);
                    tessellator.addVertexWithUV(westX, y, endZ, endU, maxV);
                    tessellator.addVertexWithUV(westX, y, z + 1, maxU, maxV);
                }

                if (left) {
                    float minV = leftIcon.getMinV();
                    float maxV = leftIcon.getMaxV();
                    float minU = leftIcon.getMinU();
                    float startU = leftIcon.getInterpolatedU(7);
                    tessellator.addVertexWithUV(westX, y + 1, startZ, startU, minV);
                    tessellator.addVertexWithUV(westX, y + 1, z, minU, minV);
                    tessellator.addVertexWithUV(westX, y, z, minU, maxV);
                    tessellator.addVertexWithUV(westX, y, startZ, startU, maxV);
                }

                if (cross) westX = x;
                if (cross || !front) {
                    float minV = centerIcon.getMinV();
                    float maxV = centerIcon.getMaxV();
                    float startU = centerIcon.getInterpolatedU(7);
                    float endU = centerIcon.getInterpolatedU(9);
                    tessellator.addVertexWithUV(westX, y + 1, endZ, endU, minV);
                    tessellator.addVertexWithUV(westX, y + 1, startZ, startU, minV);
                    tessellator.addVertexWithUV(westX, y, startZ, startU, maxV);
                    tessellator.addVertexWithUV(westX, y, endZ, endU, maxV);
                }

                break;
            case EAST:
                double eastX = x + (9.0 / 16.0);

                if (right) {
                    float minV = rightIcon.getMinV();
                    float maxV = rightIcon.getMaxV();
                    float maxU = rightIcon.getMaxU();
                    float endU = rightIcon.getInterpolatedU(9);
                    tessellator.addVertexWithUV(eastX, y + 1, z, maxU, minV);
                    tessellator.addVertexWithUV(eastX, y + 1, startZ, endU, minV);
                    tessellator.addVertexWithUV(eastX, y, startZ, endU, maxV);
                    tessellator.addVertexWithUV(eastX, y, z, maxU, maxV);
                }

                if (left) {
                    float minV = leftIcon.getMinV();
                    float maxV = leftIcon.getMaxV();
                    float minU = leftIcon.getMinU();
                    float startU = leftIcon.getInterpolatedU(7);
                    tessellator.addVertexWithUV(eastX, y + 1, endZ, startU, minV);
                    tessellator.addVertexWithUV(eastX, y + 1, z + 1, minU, minV);
                    tessellator.addVertexWithUV(eastX, y, z + 1, minU, maxV);
                    tessellator.addVertexWithUV(eastX, y, endZ, startU, maxV);
                }

                if (cross) eastX = x + 1;
                if (cross || !front) {
                    float minV = centerIcon.getMinV();
                    float maxV = centerIcon.getMaxV();
                    float startU = centerIcon.getInterpolatedU(7);
                    float endU = centerIcon.getInterpolatedU(9);
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
