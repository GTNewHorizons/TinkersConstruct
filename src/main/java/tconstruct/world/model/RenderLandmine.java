package tconstruct.world.model;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import tconstruct.mechworks.landmine.Helper;
import tconstruct.util.ItemHelper;

/**
 *
 * @author fuj1n
 *
 */
@ThreadSafeISBRH(perThread = false)
public class RenderLandmine implements ISimpleBlockRenderingHandler {

    public static int model = RenderingRegistry.getNextAvailableRenderId();

    private static final float[][] LANDMINE_BOUNDS = { { 0.0625F, 0.5F, 0.0625F, 0.9375F, 0.5625F, 0.9375F } };

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        ItemHelper.renderStandardInvBlock(renderer, block, metadata, LANDMINE_BOUNDS);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        if (modelId == getRenderId()) {
            renderBasedOnSide(world, x, y, z, block, modelId, renderer);
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("incomplete-switch")
    public void renderBasedOnSide(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        int l = world.getBlockMetadata(x, y, z);
        int i1 = l & 7;
        boolean flag = (l & 8) > 0;

        ForgeDirection dir = Helper.convertMetaToForgeOrientation(i1);

        // TConstruct.logger.info(i1 + " for " + dir);

        switch (dir) {
            case DOWN:
                renderer.setRenderBounds(0.0625F, 0.0F, 0.0625F, 1.0F - 0.0625F, 0.0625F, 1.0F - 0.0625F);
                break;
            case UP:
                renderer.setRenderBounds(0.0625F, 1.0F - 0.0625F, 0.0625F, 1.0F - 0.0625F, 1.0F, 1.0F - 0.0625F);
                break;
            case NORTH:
                renderer.setRenderBounds(0.0625F, 0.0625F, 0.0F, 1.0F - 0.0625F, 1.0F - 0.0625F, 0.0625F);
                break;
            case SOUTH:
                renderer.setRenderBounds(0.0625F, 0.0625F, 1.0F - 0.0625F, 1.0F - 0.0625F, 1.0F - 0.0625F, 1.0F);
                break;
            case EAST:
                renderer.setRenderBounds(1.0F - 0.0625F, 0.0625F, 0.0625F, 1.0F, 1.0F - 0.0625F, 1.0F - 0.0625F);
                break;
            case WEST:
                renderer.setRenderBounds(0.0F, 0.0625F, 0.0625F, 0.0625F, 1.0F - 0.0625F, 1.0F - 0.0625F);
                break;
        }

        renderer.renderStandardBlock(block, x, y, z);
    }

    @Override
    public int getRenderId() {
        return model;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }
}
