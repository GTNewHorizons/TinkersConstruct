package tconstruct.compat;

import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import baubles.api.expanded.BaubleExpandedSlots;
import baubles.common.BaublesConfig;
import cpw.mods.fml.common.Optional;
import tconstruct.library.accessory.IHealthAccessory;

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
    public static int getBaubleHealthBoost(EntityPlayer player) {
        IInventory baubleInventory = getBaubleInventory(player);
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

        IInventory baubleInventory = getBaubleInventory(player);
        if (baubleInventory == null) {
            return stack;
        }

        ItemStack remaining = stack.copy();
        for (int i = 0; i < baubleInventory.getSizeInventory() && remaining.stackSize > 0; i++) {
            if (!baubleInventory.isItemValidForSlot(i, remaining)) {
                continue;
            }
            int limit = Math.max(
                    1,
                    Math.min(
                            remaining.getMaxStackSize(),
                            BaublesConfig.getStackLimitForSlotType(BaubleExpandedSlots.getSlotType(i))));
            ItemStack inSlot = baubleInventory.getStackInSlot(i);

            if (inSlot == null) {
                ItemStack placed = remaining.copy();
                placed.stackSize = Math.min(limit, remaining.stackSize);
                baubleInventory.setInventorySlotContents(i, placed);
                remaining.stackSize -= placed.stackSize;
            } else if (inSlot.stackSize < limit && canStack(inSlot, remaining)) {
                int moved = Math.min(limit - inSlot.stackSize, remaining.stackSize);
                ItemStack grown = inSlot.copy();
                grown.stackSize += moved;
                baubleInventory.setInventorySlotContents(i, grown);
                remaining.stackSize -= moved;
            }
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

    private static boolean canStack(ItemStack inSlot, ItemStack incoming) {
        return inSlot.isStackable() && inSlot.getItem() == incoming.getItem()
                && inSlot.getItemDamage() == incoming.getItemDamage()
                && ItemStack.areItemStackTagsEqual(inSlot, incoming);
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
