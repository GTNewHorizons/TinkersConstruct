package tconstruct.plugins.natura;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import mods.natura.blocks.crops.CropBlock;
import tconstruct.api.harvesting.CropHarvestHandler;

public class NaturaCropHarvestHandler implements CropHarvestHandler {

    @Override
    public boolean couldHarvest(World world, int x, int y, int z) {
        return world.getBlock(x, y, z) instanceof CropBlock;
    }

    @Override
    public boolean tryHarvest(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block instanceof CropBlock crop) {
            int meta = world.getBlockMetadata(x, y, z);
            if (meta == crop.getMaxGrowth(meta)) {
                crop.dropBlockAsItem(world, x, y, z, meta, EnchantmentHelper.getFortuneModifier(player));
                world.setBlockMetadataWithNotify(x, y, z, crop.getStartGrowth(meta), 2);
                return true;
            }
        }
        return false;
    }
}
