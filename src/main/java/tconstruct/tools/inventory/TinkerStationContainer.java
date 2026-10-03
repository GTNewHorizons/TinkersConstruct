package tconstruct.tools.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import tconstruct.smeltery.inventory.ActiveContainer;
import tconstruct.tools.logic.ToolStationLogic;

/** Shift-click and click handling shared by the Tool Station and Tool Forge windows. */
public abstract class TinkerStationContainer extends ActiveContainer {

    public ToolStationLogic logic;
    private boolean clicking;
    private boolean topUpArmed;

    /** Whether the open tab's build reads this slot. */
    public abstract boolean isPartSlot(int index);

    /** Finishes a craft once its tool has left the output slot. */
    public abstract void onResultTaken(EntityPlayer player, ItemStack stack);

    abstract boolean topUpPartSlots(ItemStack stack, int startIndex, int endIndex);

    /** Where a shift-click may put a stack; the screen lights the inventory by the same test. */
    public boolean takesByShiftClick(Slot slot, ItemStack stack) {
        return stack != null && stack.getItem() != null && slot.isItemValid(stack);
    }

    /**
     * Vanilla's merge plus the isItemValid test it leaves out. Toward the station a part slot takes one part per click:
     * a stack lays one in each cell that takes it, and the next click tops those cells up.
     */
    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        final boolean station = !useEndIndex && startIndex == 1;
        // vanilla and Hodgepodge repeat a shift-click inside the click, and a repeat that lays nothing would top up
        final boolean mayTopUp = station && topUpArmed;
        if (station) topUpArmed = false;
        boolean merged = mergeItemStackRefill(stack, startIndex, endIndex, useEndIndex);
        boolean laid = stack.stackSize > 0 && mergeItemStackMove(stack, startIndex, endIndex, useEndIndex);
        if (mayTopUp && !laid) merged |= topUpPartSlots(stack, startIndex, endIndex);
        return merged || laid;
    }

    /** Vanilla's stacking pass; part slots are left to topUpPartSlots, which levels them. */
    private boolean mergeItemStackRefill(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        boolean merged = false;
        final boolean station = !useEndIndex && startIndex == 1;
        int k = useEndIndex ? endIndex - 1 : startIndex;

        if (stack.isStackable()) {
            while (stack.stackSize > 0 && (!useEndIndex && k < endIndex || useEndIndex && k >= startIndex)) {
                Slot slot = (Slot) this.inventorySlots.get(k);
                ItemStack inSlot = slot.getStack();

                if (!(station && isPartSlot(slot.getSlotIndex()))
                        && CraftingStationContainer.stacksCanMerge(inSlot, stack)
                        && (!station || slot.isItemValid(stack))) {
                    int limit = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
                    int total = inSlot.stackSize + stack.stackSize;

                    if (total <= limit) {
                        stack.stackSize = 0;
                        inSlot.stackSize = total;
                        slot.onSlotChanged();
                        merged = true;
                    } else if (inSlot.stackSize < limit) {
                        stack.stackSize -= limit - inSlot.stackSize;
                        inSlot.stackSize = limit;
                        slot.onSlotChanged();
                        merged = true;
                    }
                }

                k += useEndIndex ? -1 : 1;
            }
        }

        return merged;
    }

    /** Vanilla's empty-slot pass, with isItemValid. */
    private boolean mergeItemStackMove(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        boolean merged = false;
        final boolean station = !useEndIndex && startIndex == 1;
        int k = useEndIndex ? endIndex - 1 : startIndex;

        while (stack.stackSize > 0 && (!useEndIndex && k < endIndex || useEndIndex && k >= startIndex)) {
            Slot slot = (Slot) this.inventorySlots.get(k);

            if (slot.getStack() == null && takesByShiftClick(slot, stack)) {
                int limit = station && isPartSlot(slot.getSlotIndex()) ? 1
                        : Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
                slot.putStack(stack.splitStack(Math.min(stack.stackSize, limit)));
                slot.onSlotChanged();
                merged = true;
            }

            k += useEndIndex ? -1 : 1;
        }

        return merged;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
        ItemStack stack = null;
        Slot slot = (Slot) this.inventorySlots.get(slotID);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            stack = slotStack.copy();
            if (slotID < logic.getSizeInventory()) {
                if (slotID == 0) {
                    if (!this.mergeCraftedStack(
                            slotStack,
                            logic.getSizeInventory(),
                            this.inventorySlots.size(),
                            true,
                            player)) {
                        return null;
                    }
                } else
                    if (!this.mergeItemStack(slotStack, logic.getSizeInventory(), this.inventorySlots.size(), true)) {
                        return null;
                    }
            } else if (!this.mergeItemStack(slotStack, 1, logic.getSizeInventory(), false)) {
                return null;
            }

            if (slotStack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }

        return stack;
    }

    protected boolean mergeCraftedStack(ItemStack stack, int slotsStart, int slotsTotal, boolean playerInventory,
            EntityPlayer player) {
        boolean failedToMerge = false;
        int slotIndex = slotsStart;

        if (playerInventory) {
            slotIndex = slotsTotal - 1;
        }

        Slot otherInventorySlot;
        ItemStack copyStack = null;

        if (stack.stackSize > 0) {
            if (playerInventory) {
                slotIndex = slotsTotal - 1;
            } else {
                slotIndex = slotsStart;
            }

            while (!playerInventory && slotIndex < slotsTotal || playerInventory && slotIndex >= slotsStart) {
                otherInventorySlot = (Slot) this.inventorySlots.get(slotIndex);
                copyStack = otherInventorySlot.getStack();

                if (copyStack == null) {
                    onResultTaken(player, stack);
                    otherInventorySlot.putStack(stack.copy());
                    otherInventorySlot.onSlotChanged();
                    stack.stackSize = 0;
                    failedToMerge = true;
                    break;
                }

                if (playerInventory) {
                    --slotIndex;
                } else {
                    ++slotIndex;
                }
            }
        }

        return failedToMerge;
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
        // a shift-click calls back into slotClick; the outer call does the bookkeeping below
        if (clicking) return super.slotClick(slotId, clickedButton, mode, player);
        clicking = true;
        topUpArmed = true;
        try {
            ItemStack[] before = player.worldObj.isRemote ? null : copyStation();
            ItemStack output = super.slotClick(slotId, clickedButton, mode, player);
            // a shift-click clears the output after its craft has rebuilt it from the parts left
            if (slotId >= 0) this.logic.buildTool(this.logic.toolName);
            if (before != null) {
                // a count changed in place reaches only markDirty, which sends nothing, and the table draws the slots
                if (changed(before)) this.logic.syncContentsToClients();
                if (logic.table.receivedInput(before)) logic.table.faceToward(player);
            }
            return output;
        } finally {
            clicking = false;
            topUpArmed = false;
        }
    }

    private ItemStack[] copyStation() {
        ItemStack[] copies = new ItemStack[logic.getSizeInventory()];
        for (int i = 0; i < copies.length; i++) copies[i] = ItemStack.copyItemStack(logic.getStackInSlot(i));
        return copies;
    }

    private boolean changed(ItemStack[] before) {
        for (int i = 0; i < before.length; i++) {
            if (!ItemStack.areItemStacksEqual(before[i], logic.getStackInSlot(i))) return true;
        }
        return false;
    }
}
