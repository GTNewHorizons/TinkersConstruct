package tconstruct.api.harvesting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface CropHarvestHandler {

    boolean couldHarvest(World world, int x, int y, int z);

    boolean tryHarvest(ItemStack stack, EntityPlayer player, World world, int x, int y, int z);
}
