package tconstruct.armor.modelblock;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import tconstruct.util.ItemHelper;

@ThreadSafeISBRH(perThread = false)
public class DryingRackRender implements ISimpleBlockRenderingHandler {

    public static int model = RenderingRegistry.getNextAvailableRenderId();

    private static final float[][] RACK_BOUNDS = {
        { 0.375F, 0.375F, 0.0F, 0.625F, 0.625F, 1.0F } };

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        if (modelID == model) {
            ItemHelper.renderStandardInvBlock(renderer, block, metadata, RACK_BOUNDS);
        }
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelID,
            RenderBlocks renderer) {
        if (modelID == model) {
            int metadata = world.getBlockMetadata(x, y, z);
            if (metadata == 0) {
                renderer.setRenderBounds(0.0F, 0.0, 0.375F, 1.0F, 0.25F, 0.625F);
            }
            if (metadata == 1) {
                renderer.setRenderBounds(0.375F, 0.0, 0.0f, 0.625F, 0.25F, 1F);
            }

            if (metadata == 2) {
                renderer.setRenderBounds(0.0F, 0.75F, 0.75F, 1F, 1.0F, 1F);
            }
            if (metadata == 3) {
                renderer.setRenderBounds(0.0F, 0.75F, 0F, 1F, 1.0F, 0.25F);
            }
            if (metadata == 4) {
                renderer.setRenderBounds(0.75F, 0.75F, 0.0f, 1F, 1.0F, 1F);
            }
            if (metadata == 5) {
                renderer.setRenderBounds(0F, 0.75F, 0.0f, 0.25F, 1.0F, 1F);
            }
            renderer.renderStandardBlock(block, x, y, z);
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
