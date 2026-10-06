package tconstruct.tools.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotTool extends Slot {

    private final ToolStationContainer container;

    public SlotTool(ToolStationContainer container, IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.container = container;
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    public boolean isItemValid(ItemStack stack) {
        return false;
        // return stack.getItem() instanceof ToolCore;
    }

    public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack stack) {
        container.onResultTaken(par1EntityPlayer, stack);
        // stack.setUnlocalizedName("\u00A7f" + toolName);
        super.onPickupFromSlot(par1EntityPlayer, stack);
    }
}
