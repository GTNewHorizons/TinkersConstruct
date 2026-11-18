package tconstruct.compat;

import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import baubles.api.expanded.BaubleExpandedSlots;
import baubles.common.BaublesConfig;
import baubles.common.lib.PlayerHandler;
import cpw.mods.fml.common.Optional;
import tconstruct.library.accessory.IHealthAccessory;
import tconstruct.util.InventoryHelper;

public final class BaublesHelper {

    private BaublesHelper() {}

    @Optional.Method(modid = "Baubles")
    public static ItemStack findFirstMatchingBauble(EntityPlayer player, Predicate<ItemStack> matcher) {
        IInventory baubleInventory = PlayerHandler.getPlayerBaubles(player);
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
        IInventory baubleInventory = PlayerHandler.getPlayerBaubles(player);
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
    public static int getBaubleHealthBoost(EntityPlayer player) {
        IInventory baubleInventory = PlayerHandler.getPlayerBaubles(player);
        if (baubleInventory == null) {
            return 0;
        }
        int bonusHP = 0;
        for (int i = 0; i < baubleInventory.getSizeInventory(); i++) {
            ItemStack stack = baubleInventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof IHealthAccessory) {
                bonusHP += ((IHealthAccessory) stack.getItem()).getHealthBoost(stack);
            }
        }
        return bonusHP;
    }

    @Optional.Method(modid = "Baubles")
    public static ItemStack tryMoveToBaubles(EntityPlayer player, ItemStack stack) {
        if (stack == null || stack.stackSize <= 0) {
            return null;
        }
        if (!(stack.getItem() instanceof baubles.api.IBauble)) {
            return stack;
        }

        IInventory baubleInventory = PlayerHandler.getPlayerBaubles(player);
        if (baubleInventory == null) {
            return stack;
        }

        ItemStack remaining = stack.copy();
        for (int i = 0; i < baubleInventory.getSizeInventory() && remaining.stackSize > 0; i++) {
            if (!baubleInventory.isItemValidForSlot(i, remaining)) {
                continue;
            }
            remaining.stackSize -= InventoryHelper.insertIntoSlot(
                    baubleInventory,
                    i,
                    remaining,
                    getSlotStackLimit(baubleInventory, i),
                    remaining.stackSize);
        }
        return remaining.stackSize > 0 ? remaining : null;
    }

    @Optional.Method(modid = "Baubles")
    public static boolean tryEquipOne(EntityPlayer player, ItemStack held) {
        if (held == null || held.stackSize <= 0) {
            return false;
        }
        ItemStack one = held.copy();
        one.stackSize = 1;
        if (tryMoveToBaubles(player, one) != null) {
            return false;
        }
        held.stackSize--;
        return true;
    }

    @Optional.Method(modid = "Baubles")
    private static int getSlotStackLimit(IInventory baubleInventory, int slot) {
        if (LoadedMods.baublesExpanded) {
            return getExpandedSlotStackLimit(slot);
        }
        return baubleInventory.getInventoryStackLimit();
    }

    @Optional.Method(modid = "Baubles|Expanded")
    private static int getExpandedSlotStackLimit(int slot) {
        return BaublesConfig.getStackLimitForSlotType(BaubleExpandedSlots.getSlotType(slot));
    }
}
