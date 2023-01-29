package tconstruct.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public interface IPlayerExtendedInventoryWrapper {

    IInventory getKnapsackInventory(EntityPlayer player);

    IInventory getAccessoryInventory(EntityPlayer player);
}
