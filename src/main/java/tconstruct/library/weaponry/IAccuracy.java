package tconstruct.library.weaponry;

import net.minecraft.item.ItemStack;

public interface IAccuracy {

    float minAccuracy(ItemStack itemStack);

    float maxAccuracy(ItemStack itemStack);

    float getAccuracy(ItemStack itemStack, int time);
}
