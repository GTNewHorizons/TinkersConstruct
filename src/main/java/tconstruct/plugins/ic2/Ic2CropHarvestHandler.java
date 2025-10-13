package tconstruct.plugins.ic2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ic2.api.crops.ICropTile;
import tconstruct.api.harvesting.CropHarvestHandler;

public class Ic2CropHarvestHandler implements CropHarvestHandler {

    @Override
    public boolean couldHarvest(World world, int x, int y, int z) {
        return world.getTileEntity(x, y, z) instanceof ICropTile;
    }

    @Override
    public boolean tryHarvest(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
        return world.getTileEntity(x, y, z) instanceof ICropTile crop && crop.getCrop() != null
                && crop.getCrop().canBeHarvested(crop)
                && crop.harvest(true);
    }
}
