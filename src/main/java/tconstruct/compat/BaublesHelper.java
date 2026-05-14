package tconstruct.compat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Optional;

public final class BaublesHelper {

    private BaublesHelper() {}

    public interface BaubleMatcher {

        boolean matches(ItemStack stack);
    }

    @Optional.Method(modid = "Baubles")
    public static ItemStack findFirstMatchingBauble(EntityPlayer player, BaubleMatcher matcher) {
        IInventory baubleInventory = getBaubleInventory(player);
        if (baubleInventory == null) {
            return null;
        }
        for (int i = 0; i < baubleInventory.getSizeInventory(); i++) {
            ItemStack stack = baubleInventory.getStackInSlot(i);
            if (matcher.matches(stack)) {
                return stack;
            }
        }
        return null;
    }

    @Optional.Method(modid = "Baubles")
    public static ItemStack[] getBaubleStacks(EntityPlayer player) {
        IInventory baubleInventory = getBaubleInventory(player);
        if (baubleInventory == null) {
            return null;
        }
        ItemStack[] stacks = new ItemStack[baubleInventory.getSizeInventory()];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = baubleInventory.getStackInSlot(i);
        }
        return stacks;
    }

    @Optional.Method(modid = "Baubles")
    private static IInventory getBaubleInventory(EntityPlayer player) {
        baubles.common.container.InventoryBaubles baubleInventory = baubles.common.lib.PlayerHandler
                .getPlayerBaubles(player);
        if (baubleInventory == null) {
            return null;
        }
        return baubleInventory;
    }
}
