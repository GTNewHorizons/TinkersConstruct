package tconstruct.world.model;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import tconstruct.util.ItemHelper;

@ThreadSafeISBRH(perThread = false)
public class PunjiRender implements ISimpleBlockRenderingHandler {

    public static int model = RenderingRegistry.getNextAvailableRenderId();

    private static final float[][] PUNJI_BOUNDS = {
        { 0.4375F, 0.0F, 0.4375F, 0.5625F, 1.0F, 0.5625F } };

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        if (modelID == model) {
            ItemHelper.renderStandardInvBlock(renderer, block, metadata, PUNJI_BOUNDS);
        }
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelID,
            RenderBlocks renderer) {
        if (modelID == model) {
            int metadata = world.getBlockMetadata(x, y, z);
            renderer.setRenderBounds(0.4375, 0.0, 0.4375, 0.5625, 0.375f, 0.5625);
            renderer.renderStandardBlock(block, x, y, z);
            if (metadata >= 1) {
                renderer.setRenderBounds(0.125, 0.0, 0.125, 0.25, 0.375f, 0.25);
                renderer.renderStandardBlock(block, x, y, z);
            }
            if (metadata >= 2) {
                renderer.setRenderBounds(0.75, 0.0, 0.75, 0.875, 0.375f, 0.875);
                renderer.renderStandardBlock(block, x, y, z);
            }
            if (metadata >= 3) {
                renderer.setRenderBounds(0.125, 0.0, 0.75, 0.25, 0.375f, 0.875);
                renderer.renderStandardBlock(block, x, y, z);
            }
            if (metadata >= 4) {
                renderer.setRenderBounds(0.75, 0.0, 0.125, 0.875, 0.375f, 0.25);
                renderer.renderStandardBlock(block, x, y, z);
            }
            /*
             * if (metadata == 5) { renderer.setRenderBounds(0.0F, 0.0, 0.0F, 1.0F, 0.875F, 1.0F);
             * renderer.renderStandardBlock(block, x, y, z); } else { renderer.setRenderBounds(0.0F, 0.75F, 0.0F, 1.0F,
             * 1.0F, 1.0F); renderer.renderStandardBlock(block, x, y, z); renderer.setRenderBounds(0.0F, 0.0F, 0.0F,
             * 0.25F, 0.75F, 0.25F); renderer.renderStandardBlock(block, x, y, z); renderer.setRenderBounds(0.75F, 0.0F,
             * 0.0F, 1.0F, 0.75F, 0.25F); renderer.renderStandardBlock(block, x, y, z); renderer.setRenderBounds(0.0F,
             * 0.0F, 0.75F, 0.25F, 0.75F, 1.0F); renderer.renderStandardBlock(block, x, y, z);
             * renderer.setRenderBounds(0.75F, 0.0F, 0.75F, 1.0F, 0.75F, 1.0F); renderer.renderStandardBlock(block, x,
             * y, z); }
             */
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelID) {
        return true;
    }

    @Override
    public int getRenderId() {
        return model;
    }
}
