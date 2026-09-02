package tconstruct.tools.inventory;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import tconstruct.library.crafting.ModifyBuilder;
import tconstruct.library.modifier.IModifyable;
import tconstruct.tools.TinkerTools;
import tconstruct.tools.gui.ChestSlot;
import tconstruct.tools.logic.CraftingStationLogic;

public class CraftingStationContainer extends Container {

    private static final int CRAFTING_RESULT_SLOT = 0;
    private static final int CRAFTING_GRID_FIRST_SLOT = 1;
    private static final int CRAFTING_GRID_END_SLOT = 10;
    private static final int PLAYER_INVENTORY_FIRST_SLOT = 10;
    private static final int PLAYER_INVENTORY_END_SLOT = 46;
    private static final int SIDE_INVENTORY_FIRST_SLOT = 46;
    private static final int SIDE_INVENTORY_PREFERENCES_SYNC_ID = 0;
    private static final int CLICK_MODE_PICKUP = 0;
    private static final int CLICK_MODE_QUICK_MOVE = 1;
    private static final int CLICK_MODE_HOTBAR_SWAP = 2;
    private static final int CLICK_MODE_DRAG = 5;
    private static final int CLICK_MODE_COLLECT = 6;

    private final World worldObj;
    private final int posX;
    private final int posY;
    private final int posZ;

    @SuppressWarnings("rawtypes")
    private final WeakReference[] inventories;

    /**
     * The crafting matrix inventory (3x3).
     */
    public InventoryCrafting craftMatrix;

    public IInventory craftResult;
    public CraftingStationLogic logic;
    EntityPlayer player;

    /** Last matched recipe, tried first to avoid rescanning the whole recipe list on every matrix change. */
    private IRecipe lastRecipe;

    /** While true, matrix changes don't recompute the result. Batches ingredient consumption into one lookup. */
    private boolean suppressCraftingUpdates;

    /** Side-inventory preference of the stack currently carried by this container's cursor. */
    private boolean carriedStackPrefersSideInventory;

    /** Last preference mask sent to clients. */
    private int lastSideInventoryPreferences = -1;

    public CraftingStationContainer(InventoryPlayer inventoryplayer, CraftingStationLogic logic, int x, int y, int z) {
        this.worldObj = logic.getWorldObj();
        this.player = inventoryplayer.player;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.logic = logic;
        craftMatrix = new InventoryCraftingStation(this, 3, 3, logic);
        craftResult = new InventoryCraftingStationResult(logic);
        this.inventories = logic.getInventories();

        int row, col;

        int bothOffset = 0;

        if (logic.chest != null) {
            if (logic.slotCount > 54) bothOffset += 12; // SlideBar.width

            bothOffset += 122;
        }
        final int craftingOffsetX = 30 + bothOffset;
        final int inventoryOffsetX = 8 + bothOffset;

        // 0 - crafting slot
        this.addSlotToContainer(
                new SlotCraftingStation(
                        this,
                        inventoryplayer.player,
                        this.craftMatrix,
                        this.craftResult,
                        0,
                        craftingOffsetX + 94,
                        35));

        // 1 - 9 - Crafting Matrix
        for (row = 0; row < 3; ++row) {
            for (col = 0; col < 3; ++col) {
                this.addSlotToContainer(
                        new Slot(this.craftMatrix, col + row * 3, craftingOffsetX + col * 18, 17 + row * 18));
            }
        }

        // Player Inventory 10 - 36
        for (row = 0; row < 3; ++row) {
            for (col = 0; col < 9; ++col) {
                this.addSlotToContainer(
                        new Slot(inventoryplayer, col + row * 9 + 9, inventoryOffsetX + col * 18, 84 + row * 18));
            }
        }
        // Player Hotbar - 37 - 45
        for (col = 0; col < 9; ++col) {
            this.addSlotToContainer(new Slot(inventoryplayer, col, inventoryOffsetX + col * 18, 142));
        }

        // Side inventory - 46+
        if (logic.chest != null) {
            IInventory inv = logic.getFirstInventory();
            IInventory secondInv = logic.getSecondInventory();

            final int accessSide = logic.chestDirection.getOpposite().ordinal();
            final int[] accessibleSlots = inv instanceof ISidedInventory
                    ? ((ISidedInventory) inv).getAccessibleSlotsFromSide(accessSide)
                    : null;

            int index = 0, curIndex;
            IInventory curInv;
            final int invSize = inv.getSizeInventory() * (secondInv != null ? 2 : 1);
            for (row = 0; row < logic.invRows; row++) {
                for (col = 0; col < logic.invColumns; col++) {
                    if (index >= invSize) break;
                    // Adjust the inventory to account for double chests
                    curInv = secondInv != null && index >= 27 ? secondInv : inv;
                    // Adjust the index for the inventory
                    curIndex = secondInv != null && index >= 27 ? index - 27 : index;

                    if (accessibleSlots != null) {
                        if (curIndex >= accessibleSlots.length) {
                            break;
                        } else {
                            curIndex = accessibleSlots[curIndex];
                        }
                    }

                    this.addSlotToContainer(
                            new ChestSlot(curInv, curIndex, index, 8 + col * 18, 19 + row * 18, accessSide));
                    index++;
                }
            }
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    public ItemStack modifyItem() {
        ItemStack input = craftMatrix.getStackInSlot(4);
        if (input != null) {
            Item item = input.getItem();
            if (item instanceof IModifyable) {
                ItemStack[] slots = new ItemStack[8];
                for (int i = 0; i < 4; i++) {
                    slots[i] = craftMatrix.getStackInSlot(i);
                    slots[i + 4] = craftMatrix.getStackInSlot(i + 5);
                }
                return ModifyBuilder.instance.modifyItem(input, slots);
            }
        }
        return null;
    }

    public ItemStack transferStackInSlot(EntityPlayer entityPlayer, int index) {
        Slot slot = (Slot) this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack()) {
            return null;
        }

        ItemStack itemstack = slot.getStack();
        ItemStack ret = itemstack.copy();

        boolean nothingDone = true;

        if (index == CRAFTING_RESULT_SLOT) {
            // Crafting Result
            if (ret.getItem() instanceof IModifyable) {
                nothingDone &= !this.mergeCraftedStack(itemstack, logic.getSizeInventory(), 46, true, entityPlayer);
            } else {
                // First refill the attached chests
                nothingDone &= this.refillChest(itemstack);

                // Then try moving to player inventory
                nothingDone &= moveToPlayerInventory(itemstack);
            }

            slot.onSlotChange(itemstack, ret);
        } else if (isCraftingGridSlot(index)) {
            // Side-origin stacks may use empty slots in the attached inventory.
            nothingDone &= !logic.prefersSideInventory(index) || this.moveToChest(itemstack);

            // Player inventory is always the fallback, so NEI can clear the grid.
            nothingDone &= moveToPlayerInventory(itemstack);
        } else if (index >= PLAYER_INVENTORY_FIRST_SLOT && index < PLAYER_INVENTORY_END_SLOT) {
            // Move player stacks to the attached inventory.
            nothingDone &= this.moveToChest(itemstack);
        } else { // From the Attached Chests
            // Move attached inventory stacks to the player inventory.
            nothingDone &= moveToPlayerInventory(itemstack);
        }

        if (nothingDone) {
            return null;
        }

        if (itemstack.stackSize == 0) {
            slot.putStack(null);
        } else {
            slot.onSlotChanged();
        }

        if (itemstack.stackSize == ret.stackSize) {
            return null;
        }

        slot.onPickupFromSlot(entityPlayer, itemstack);

        return ret;
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
        ItemStack carriedBefore = copyStack(player.inventory.getItemStack());
        boolean carriedPreferenceBefore = carriedStackPrefersSideInventory;
        ItemStack clickedBefore = getSlotStackCopy(slotId);

        ItemStack[] gridBefore = copySlotRange(CRAFTING_GRID_FIRST_SLOT, CRAFTING_GRID_END_SLOT);
        boolean[] gridPreferencesBefore = copyGridPreferences();
        ItemStack[] sideInventoryBefore = mode == CLICK_MODE_COLLECT
                ? copySlotRange(SIDE_INVENTORY_FIRST_SLOT, inventorySlots.size())
                : null;

        ItemStack result = super.slotClick(slotId, clickedButton, mode, player);

        ItemStack carriedAfter = player.inventory.getItemStack();
        ItemStack[] gridAfter = copySlotRange(CRAFTING_GRID_FIRST_SLOT, CRAFTING_GRID_END_SLOT);
        ItemStack[] sideInventoryAfter = sideInventoryBefore == null ? null
                : copySlotRange(SIDE_INVENTORY_FIRST_SLOT, inventorySlots.size());

        boolean sideInventoryShiftClick = mode == CLICK_MODE_QUICK_MOVE && isSideInventorySlot(slotId);
        boolean incomingStackPrefersSideInventory = sideInventoryShiftClick
                || carriedBefore != null && carriedPreferenceBefore;

        updateGridPreferences(
                gridBefore,
                gridAfter,
                gridPreferencesBefore,
                carriedBefore,
                clickedBefore,
                slotId,
                mode,
                sideInventoryShiftClick,
                incomingStackPrefersSideInventory);

        boolean collectedPreferredGridStack = collectedPreferredGridStack(
                carriedAfter,
                gridBefore,
                gridAfter,
                gridPreferencesBefore);
        boolean collectedSideInventoryStack = collectedSideInventoryStack(
                carriedAfter,
                sideInventoryBefore,
                sideInventoryAfter);
        boolean clickedStackPrefersSideInventory = isSideInventorySlot(slotId)
                || isCraftingGridSlot(slotId) && gridPreferencesBefore[slotId - CRAFTING_GRID_FIRST_SLOT];
        carriedStackPrefersSideInventory = getCarriedStackPreference(
                carriedBefore,
                carriedAfter,
                carriedPreferenceBefore,
                clickedBefore,
                clickedStackPrefersSideInventory,
                collectedPreferredGridStack,
                collectedSideInventoryStack);

        return result;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        int sideInventoryPreferences = logic.getSideInventoryPreferences();
        if (lastSideInventoryPreferences != sideInventoryPreferences) {
            for (ICrafting crafter : crafters) {
                crafter.sendProgressBarUpdate(this, SIDE_INVENTORY_PREFERENCES_SYNC_ID, sideInventoryPreferences);
            }
            lastSideInventoryPreferences = sideInventoryPreferences;
        }
    }

    @Override
    public void updateProgressBar(int id, int value) {
        if (id == SIDE_INVENTORY_PREFERENCES_SYNC_ID) {
            logic.setSideInventoryPreferences(value);
        }
    }

    private void updateGridPreferences(ItemStack[] before, ItemStack[] after, boolean[] preferencesBefore,
            ItemStack carriedBefore, ItemStack clickedBefore, int clickedSlot, int mode,
            boolean sideInventoryShiftClick, boolean incomingStackPrefersSideInventory) {
        for (int i = 0; i < after.length; i++) {
            int slot = CRAFTING_GRID_FIRST_SLOT + i;
            ItemStack current = after[i];
            if (current == null || current.stackSize <= 0) {
                logic.setSideInventoryPreference(slot, false);
                continue;
            }

            if (mode == CLICK_MODE_HOTBAR_SWAP && slot == clickedSlot) {
                // Hotbar swaps replace the grid stack with an untracked player-inventory stack.
                logic.setSideInventoryPreference(slot, false);
                continue;
            }

            ItemStack previous = before[i];
            boolean receivedCarriedStack = (mode == CLICK_MODE_PICKUP && slot == clickedSlot || mode == CLICK_MODE_DRAG)
                    && carriedBefore != null
                    && stacksCanMerge(current, carriedBefore);
            boolean receivedShiftClickedStack = sideInventoryShiftClick && clickedBefore != null
                    && stacksCanMerge(current, clickedBefore);
            boolean receivedIncomingStack = receivedCarriedStack || receivedShiftClickedStack;
            boolean prefersSideInventory = preferencesBefore[i];

            if (previous == null || !stacksCanMerge(previous, current)) {
                logic.setSideInventoryPreference(slot, receivedIncomingStack && incomingStackPrefersSideInventory);
                continue;
            }

            if (current.stackSize > previous.stackSize && receivedIncomingStack && incomingStackPrefersSideInventory) {
                prefersSideInventory = true;
            }

            logic.setSideInventoryPreference(slot, prefersSideInventory);
        }
    }

    private static boolean getCarriedStackPreference(ItemStack carriedBefore, ItemStack carriedAfter,
            boolean carriedPreferenceBefore, ItemStack clickedBefore, boolean clickedStackPrefersSideInventory,
            boolean collectedPreferredGridStack, boolean collectedSideInventoryStack) {
        if (carriedAfter == null || carriedAfter.stackSize <= 0) return false;

        if (carriedBefore != null && stacksCanMerge(carriedBefore, carriedAfter)) {
            return carriedPreferenceBefore || collectedPreferredGridStack || collectedSideInventoryStack;
        }

        if (clickedBefore != null && stacksCanMerge(clickedBefore, carriedAfter)) {
            return clickedStackPrefersSideInventory;
        }

        return collectedPreferredGridStack || collectedSideInventoryStack;
    }

    private static boolean collectedPreferredGridStack(ItemStack carriedStack, ItemStack[] before, ItemStack[] after,
            boolean[] preferencesBefore) {
        for (int i = 0; i < before.length; i++) {
            if (preferencesBefore[i] && stackWasCollected(carriedStack, before[i], after[i])) return true;
        }
        return false;
    }

    private static boolean collectedSideInventoryStack(ItemStack carriedStack, ItemStack[] before, ItemStack[] after) {
        if (before == null || after == null) return false;

        for (int i = 0; i < before.length; i++) {
            if (stackWasCollected(carriedStack, before[i], after[i])) return true;
        }
        return false;
    }

    private static boolean stackWasCollected(ItemStack carriedStack, ItemStack before, ItemStack after) {
        if (before == null || !stacksCanMerge(carriedStack, before)) return false;
        return after == null || !stacksCanMerge(before, after) || after.stackSize < before.stackSize;
    }

    private boolean[] copyGridPreferences() {
        boolean[] preferences = new boolean[CRAFTING_GRID_END_SLOT - CRAFTING_GRID_FIRST_SLOT];
        for (int i = 0; i < preferences.length; i++) {
            preferences[i] = logic.prefersSideInventory(CRAFTING_GRID_FIRST_SLOT + i);
        }
        return preferences;
    }

    private ItemStack[] copySlotRange(int start, int end) {
        ItemStack[] stacks = new ItemStack[Math.max(0, end - start)];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = copyStack(((Slot) inventorySlots.get(start + i)).getStack());
        }
        return stacks;
    }

    private ItemStack getSlotStackCopy(int slot) {
        if (slot < 0 || slot >= inventorySlots.size()) return null;
        return copyStack(((Slot) inventorySlots.get(slot)).getStack());
    }

    private static ItemStack copyStack(ItemStack stack) {
        return stack == null ? null : stack.copy();
    }

    private static boolean stacksCanMerge(ItemStack first, ItemStack second) {
        return first != null && second != null
                && first.getItem() == second.getItem()
                && (!first.getHasSubtypes() || first.getItemDamage() == second.getItemDamage())
                && ItemStack.areItemStackTagsEqual(first, second);
    }

    private static boolean isCraftingGridSlot(int slot) {
        return slot >= CRAFTING_GRID_FIRST_SLOT && slot < CRAFTING_GRID_END_SLOT;
    }

    private boolean isSideInventorySlot(int slot) {
        return slot >= SIDE_INVENTORY_FIRST_SLOT && slot < inventorySlots.size();
    }

    protected boolean refillChest(ItemStack itemstack) {
        if (itemstack == null || itemstack.stackSize <= 0 || logic.slotCount == 0) return false;

        return !this.mergeItemStackRefill(
                itemstack,
                SIDE_INVENTORY_FIRST_SLOT,
                SIDE_INVENTORY_FIRST_SLOT + logic.slotCount,
                false);
    }

    protected boolean moveToChest(ItemStack itemstack) {
        if (itemstack == null || itemstack.stackSize <= 0 || logic.slotCount == 0) return false;

        return !this.mergeItemStack(
                itemstack,
                SIDE_INVENTORY_FIRST_SLOT,
                SIDE_INVENTORY_FIRST_SLOT + logic.slotCount,
                false);
    }

    protected boolean moveToPlayerInventory(ItemStack itemstack) {
        if (itemstack == null || itemstack.stackSize <= 0) return false;

        return !this.mergeItemStack(itemstack, PLAYER_INVENTORY_FIRST_SLOT, PLAYER_INVENTORY_END_SLOT, false);
    }

    public boolean func_94530_a /* canMergeSlot */(ItemStack par1ItemStack, Slot par2Slot) {
        return par2Slot.inventory != this.craftResult && super.func_94530_a(par1ItemStack, par2Slot);
    }

    @Override
    public void onContainerClosed(EntityPlayer par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);

        if (!this.worldObj.isRemote) {
            for (int i = 0; i < 9; ++i) {
                ItemStack itemstack = this.craftMatrix.getStackInSlotOnClosing(i);

                if (itemstack != null) {
                    par1EntityPlayer.dropPlayerItemWithRandomChoice(itemstack, false);
                }
            }
        }
    }

    public void onCraftMatrixChanged(IInventory par1IInventory) {
        if (suppressCraftingUpdates) return;

        ItemStack tool = modifyItem();
        if (tool != null) this.craftResult.setInventorySlotContents(0, tool);
        else this.craftResult.setInventorySlotContents(0, findMatchingRecipeCached());
    }

    /** Like {@link CraftingManager#findMatchingRecipe} but tries the last matched recipe first. */
    private ItemStack findMatchingRecipeCached() {
        // Vanilla's two-item tool repair takes precedence over the recipe list
        if (isVanillaToolRepair()) {
            return CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
        }

        IRecipe cached = this.lastRecipe;
        if (cached != null && cached.matches(this.craftMatrix, this.worldObj)) {
            return cached.getCraftingResult(this.craftMatrix);
        }
        this.lastRecipe = null;

        @SuppressWarnings("unchecked")
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        for (int i = 0; i < recipes.size(); i++) {
            IRecipe recipe = recipes.get(i);
            if (recipe.matches(this.craftMatrix, this.worldObj)) {
                this.lastRecipe = recipe;
                return recipe.getCraftingResult(this.craftMatrix);
            }
        }
        return null;
    }

    /**
     * Mirrors the repair check in {@link CraftingManager#findMatchingRecipe}: two size-1 stacks of one repairable item.
     */
    private boolean isVanillaToolRepair() {
        ItemStack first = null;
        ItemStack second = null;
        int found = 0;

        for (int i = 0; i < this.craftMatrix.getSizeInventory(); i++) {
            ItemStack stack = this.craftMatrix.getStackInSlot(i);
            if (stack == null) continue;

            found++;
            if (found == 1) first = stack;
            else if (found == 2) second = stack;
            else return false;
        }

        return found == 2 && first.getItem() == second.getItem()
                && first.stackSize == 1
                && second.stackSize == 1
                && first.getItem().isRepairable();
    }

    /**
     * Suppresses result updates until {@link #endBatchCraftingUpdate}; each consumed ingredient fires a lookup
     * otherwise.
     */
    void beginBatchCraftingUpdate() {
        suppressCraftingUpdates = true;
    }

    /** Re-enables result updates and recomputes once. */
    void endBatchCraftingUpdate() {
        suppressCraftingUpdates = false;
        this.onCraftMatrixChanged(this.craftMatrix);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        Block block = worldObj.getBlock(this.posX, this.posY, this.posZ);
        if (block != TinkerTools.craftingStationWood && block != TinkerTools.craftingSlabWood) return false;

        if (!this.logic.isUseableByPlayer(player) || !CraftingStationLogic.isUseableByPlayer(player, this.inventories))
            return false;

        return player.getDistanceSq((double) this.posX + 0.5D, (double) this.posY + 0.5D, (double) this.posZ + 0.5D)
                <= 64.0D;
    }

    protected boolean mergeCraftedStack(ItemStack stack, int slotsStart, int slotsTotal, boolean playerInventory,
            EntityPlayer player) {
        boolean failedToMerge = false;
        int slotIndex = slotsStart;

        if (playerInventory) {
            slotIndex = slotsTotal - 1;
        }

        Slot otherInventorySlot;
        ItemStack copyStack;

        if (stack.stackSize > 0) {
            while (!playerInventory && slotIndex < slotsTotal || playerInventory && slotIndex >= slotsStart) {
                otherInventorySlot = (Slot) this.inventorySlots.get(slotIndex);
                copyStack = otherInventorySlot.getStack();

                if (copyStack == null && otherInventorySlot.isItemValid(stack)) {
                    ItemStack placed = stack.copy();
                    if (placed.hasTagCompound() && placed.getItem() instanceof IModifyable modifyable) {
                        placed.getTagCompound().getCompoundTag(modifyable.getBaseTagName()).removeTag("ToRemove");
                    }
                    otherInventorySlot.putStack(placed);
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
    protected boolean mergeItemStack(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        boolean ret = mergeItemStackRefill(stack, startIndex, endIndex, useEndIndex);
        if (stack.stackSize > 0) {
            ret |= mergeItemStackMove(stack, startIndex, endIndex, useEndIndex);
        }
        return ret;
    }

    // only refills items that are already present
    protected boolean mergeItemStackRefill(@Nonnull ItemStack stack, int startIndex, int endIndex,
            boolean useEndIndex) {
        if (stack.stackSize <= 0) {
            return false;
        }

        boolean didSomething = false;
        int k = useEndIndex ? endIndex - 1 : startIndex;

        Slot slot;
        ItemStack itemstack1;

        if (stack.isStackable()) {
            while (stack.stackSize > 0 && (!useEndIndex && k < endIndex || useEndIndex && k >= startIndex)) {
                slot = (Slot) this.inventorySlots.get(k);
                itemstack1 = slot.getStack();

                if (itemstack1 != null && itemstack1.getItem() == stack.getItem()
                        && (!stack.getHasSubtypes() || stack.getItemDamage() == itemstack1.getItemDamage())
                        && ItemStack.areItemStackTagsEqual(stack, itemstack1)
                        && this.func_94530_a /* canMergeSlot */(stack, slot)) {
                    int l = itemstack1.stackSize + stack.stackSize;
                    int limit = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());

                    if (l <= limit) {
                        stack.stackSize = 0;
                        itemstack1.stackSize = l;
                        slot.onSlotChanged();
                        didSomething = true;
                    } else if (itemstack1.stackSize < limit) {
                        stack.stackSize -= (limit - itemstack1.stackSize);
                        itemstack1.stackSize = limit;
                        slot.onSlotChanged();
                        didSomething = true;
                    }
                }

                if (useEndIndex) --k;
                else++k;
            }
        }

        return didSomething;
    }

    // only moves items into empty slots
    protected boolean mergeItemStackMove(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        if (stack.stackSize <= 0) {
            return false;
        }

        boolean didSomething = false;
        int k = useEndIndex ? endIndex - 1 : startIndex;

        while (!useEndIndex && k < endIndex || useEndIndex && k >= startIndex) {
            final Slot slot = (Slot) this.inventorySlots.get(k);
            ItemStack itemstack1 = slot.getStack();

            if ((itemstack1 == null || itemstack1.stackSize == 0) && slot.isItemValid(stack)
                    && this.func_94530_a /* canMergeSlot */(stack, slot)) {
                // Forge: Make sure to respect isItemValid in the slot.
                int limit = slot.getSlotStackLimit();
                ItemStack stack2 = stack.copy();
                if (stack2.stackSize > limit) {
                    stack2.stackSize = limit;
                    stack.stackSize -= limit;
                } else {
                    stack.stackSize = 0;
                }
                slot.putStack(stack2);
                slot.onSlotChanged();
                didSomething = true;

                if (stack.stackSize <= 0) {
                    break;
                }
            }

            if (useEndIndex) --k;
            else++k;
        }

        return didSomething;
    }

    // Dump crafting grid to connected chests
    public void dumpCraftingGrid() {
        if (logic.slotCount == 0) return;

        beginBatchCraftingUpdate();
        try {
            // 46 is the first slot index of the attached inventory
            for (int i = 0; i < 9; i++) {
                ItemStack stack = craftMatrix.getStackInSlot(i);
                if (stack != null && stack.stackSize > 0) {
                    if (mergeItemStack(stack, 46, 46 + logic.slotCount, false)) {
                        craftMatrix.setInventorySlotContents(i, stack.stackSize > 0 ? stack : null);
                    }
                }
            }
        } finally {
            endBatchCraftingUpdate();
        }
    }
}
