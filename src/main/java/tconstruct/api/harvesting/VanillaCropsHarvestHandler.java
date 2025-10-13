package tconstruct.api.harvesting;

import net.minecraft.block.BlockCrops;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class VanillaCropsHarvestHandler implements CropHarvestHandler {

    @Override
    public boolean couldHarvest(World world, int x, int y, int z) {
        return world.getBlock(x, y, z) instanceof BlockCrops;
    }

    @Override
    public boolean tryHarvest(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
        int blockMetadata = world.getBlockMetadata(x, y, z);
        boolean shouldHarvest = couldHarvest(world, x, y, z) && blockMetadata >= 7;
        if (shouldHarvest) {
            world.getBlock(x, y, z).dropBlockAsItem(
                    world,
                    x,
                    y,
                    z,
                    world.getBlockMetadata(x, y, z),
                    EnchantmentHelper.getFortuneModifier(player));
            world.setBlockMetadataWithNotify(x, y, z, 0, 2);
        }
        return shouldHarvest;
    }
}
