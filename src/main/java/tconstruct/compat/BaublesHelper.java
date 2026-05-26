package tconstruct.compat;

import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Optional;

public final class BaublesHelper {

    private BaublesHelper() {}

    @Optional.Method(modid = "Baubles")
    public static ItemStack findFirstMatchingBauble(EntityPlayer player, Predicate<ItemStack> matcher) {
        IInventory baubleInventory = getBaubleInventory(player);
        if (baubleInventory == null) {
            return null;
        }
        for (int i = 0; i < baubleInventory.getSizeInventory(); i++) {
            ItemStack stack = baubleInventory.getStackInSlot(i);
            if (matcher.test(stack)) {
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
    public static ItemStack tryMoveToBaubles(EntityPlayer player, ItemStack stack) {
        if (stack == null || stack.stackSize <= 0) {
            return null;
        }
        if (!(stack.getItem() instanceof baubles.api.IBauble)) {
            return stack;
        }

        IInventory baubleInventory = getBaubleInventory(player);
        if (baubleInventory == null) {
            return stack;
        }

        ItemStack remaining = stack.copy();
        for (int i = 0; i < baubleInventory.getSizeInventory() && remaining.stackSize > 0; i++) {
            if (!baubleInventory.isItemValidForSlot(i, remaining)) {
                continue;
            }
            ItemStack inSlot = baubleInventory.getStackInSlot(i);
            if (inSlot == null) {
                ItemStack placed = remaining.copy();
                placed.stackSize = 1;
                baubleInventory.setInventorySlotContents(i, placed);
                remaining.stackSize -= 1;
            }
        }
        return remaining.stackSize > 0 ? remaining : null;
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
