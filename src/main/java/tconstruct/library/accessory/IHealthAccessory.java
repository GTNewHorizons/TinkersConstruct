package tconstruct.library.accessory;

import net.minecraft.item.ItemStack;

public interface IHealthAccessory extends IAccessory {

    int getHealthBoost(ItemStack item);
}
