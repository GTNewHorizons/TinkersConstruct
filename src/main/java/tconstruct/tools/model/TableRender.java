package tconstruct.tools.model;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import tconstruct.tools.TinkerTools;
import tconstruct.util.ItemHelper;

@ThreadSafeISBRH(perThread = false)
public class TableRender implements ISimpleBlockRenderingHandler {

    public static int model = RenderingRegistry.getNextAvailableRenderId();

    private static final float[][] TABLE_BOUNDS = {
        { 0.0F, 0.75F, 0.0F, 1.0F, 1.0F, 1.0F },
        { 0.0F, 0.0F, 0.0F, 0.25F, 0.75F, 0.25F },
        { 0.75F, 0.0F, 0.0F, 1.0F, 0.75F, 0.25F },
        { 0.0F, 0.0F, 0.75F, 0.25F, 0.75F, 1.0F },
        { 0.75F, 0.0F, 0.75F, 1.0F, 0.75F, 1.0F } };

    private static final float[][] CHEST_BOUNDS = { { 0.0F, 0.0F, 0.0F, 1.0F, 0.875F, 1.0F } };

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        if (modelID == model) {
            if (block == TinkerTools.toolStationWood && metadata >= 5 && metadata <= 9) {
                ItemHelper.renderStandardInvBlock(renderer, block, metadata, CHEST_BOUNDS);
            } else {
                ItemHelper.renderStandardInvBlock(renderer, block, metadata, TABLE_BOUNDS);
            }
        }
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelID,
            RenderBlocks renderer) {
        if (modelID == model) {
            int metadata = world.getBlockMetadata(x, y, z);
            // until we get the new model.. finally...
            if (block == TinkerTools.toolStationWood && metadata >= 5 && metadata <= 9) {
                // pattern chest
                renderer.setRenderBounds(0, 0, 0, 1, 0.875, 1);
                renderer.renderStandardBlock(block, x, y, z);
                return true;
            }

            renderer.setRenderBounds(0.0F, 0.75F, 0.0F, 1.0F, 1.0F, 1.0F);
            renderer.renderStandardBlock(block, x, y, z);
            renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 0.25F, 0.75F, 0.25F);
            renderer.renderStandardBlock(block, x, y, z);
            renderer.setRenderBounds(0.75F, 0.0F, 0.0F, 1.0F, 0.75F, 0.25F);
            renderer.renderStandardBlock(block, x, y, z);
            renderer.setRenderBounds(0.0F, 0.0F, 0.75F, 0.25F, 0.75F, 1.0F);
            renderer.renderStandardBlock(block, x, y, z);
            renderer.setRenderBounds(0.75F, 0.0F, 0.75F, 1.0F, 0.75F, 1.0F);
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
