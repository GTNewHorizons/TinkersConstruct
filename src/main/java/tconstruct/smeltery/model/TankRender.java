package tconstruct.smeltery.model;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import tconstruct.client.BlockSkinRenderHelper;
import tconstruct.smeltery.logic.LavaTankLogic;
import tconstruct.util.ItemHelper;

@ThreadSafeISBRH(perThread = false)
public class TankRender implements ISimpleBlockRenderingHandler {

    public static int tankModelID = RenderingRegistry.getNextAvailableRenderId();

    private static final float[][] TANK_TOP_BOUNDS = {
        { 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F },
        { 0.1875F, 0.0F, 0.1875F, 0.8125F, 0.125F, 0.8125F } };

    private static final float[][] TANK_FULL_BOUNDS = { { 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F } };

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        if (modelID == tankModelID) {
            if (metadata == 0) {
                ItemHelper.renderStandardInvBlock(renderer, block, metadata, TANK_TOP_BOUNDS);
            } else {
                ItemHelper.renderStandardInvBlock(renderer, block, metadata, TANK_FULL_BOUNDS);
            }
        }
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelID,
            RenderBlocks renderer) {
        if (modelID == tankModelID) {
            // Liquid
            if (ForgeHooksClient.getWorldRenderPass() == 0) {
                LavaTankLogic logic = (LavaTankLogic) world.getTileEntity(x, y, z);
                if (logic != null && logic.containsFluid()) {
                    FluidStack liquid = logic.tank.getFluid();
                    renderer.setRenderBounds(0.001, 0.001, 0.001, 0.999, logic.getFluidAmountScaled(), 0.999);
                    Fluid fluid = liquid.getFluid();
                    BlockSkinRenderHelper.renderLiquidBlock(
                            fluid.getStillIcon(),
                            fluid.getStillIcon(),
                            x,
                            y,
                            z,
                            renderer,
                            world,
                            false,
                            fluid.getColor(liquid));

                    return true;
                }
                return false;
            }
            // Block
            else {
                int meta = world.getBlockMetadata(x, y, z);
                if (meta == 0 && world.getBlock(x, y + 1, z) == Blocks.air) {
                    renderer.setRenderBounds(0.1875, 0, 0.1875, 0.8125, 0.125, 0.8125);
                    renderer.renderStandardBlock(block, x, y + 1, z);
                }
                renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
                return renderer.renderStandardBlock(block, x, y, z);
            }
        }
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelID) {
        return true;
    }

    @Override
    public int getRenderId() {
        return tankModelID;
    }

    }
