package tconstruct.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public final class InventoryHelper {

    private InventoryHelper() {}

    public static int insertIntoSlot(IInventory inventory, int slot, ItemStack incoming, int slotLimit, int amount) {
        if (incoming == null || incoming.stackSize <= 0 || amount <= 0) {
            return 0;
        }
        int limit = Math.max(1, Math.min(incoming.getMaxStackSize(), slotLimit));
        int movable = Math.min(amount, incoming.stackSize);
        ItemStack inSlot = inventory.getStackInSlot(slot);

        if (inSlot == null) {
            ItemStack placed = incoming.copy();
            placed.stackSize = Math.min(limit, movable);
            inventory.setInventorySlotContents(slot, placed);
            return placed.stackSize;
        }
        if (inSlot.stackSize >= limit || !canStack(inSlot, incoming)) {
            return 0;
        }
        int moved = Math.min(limit - inSlot.stackSize, movable);
        ItemStack grown = inSlot.copy();
        grown.stackSize += moved;
        inventory.setInventorySlotContents(slot, grown);
        return moved;
    }

    private static boolean canStack(ItemStack inSlot, ItemStack incoming) {
        return inSlot.isStackable() && inSlot.getItem() == incoming.getItem()
                && inSlot.getItemDamage() == incoming.getItemDamage()
                && ItemStack.areItemStackTagsEqual(inSlot, incoming);
    }
}
