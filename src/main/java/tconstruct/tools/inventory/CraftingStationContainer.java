package tconstruct.tools.inventory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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

import tconstruct.TConstruct;
import tconstruct.api.ExtendedStackLimitHelper;
import tconstruct.library.crafting.ModifyBuilder;
import tconstruct.library.modifier.IModifyable;
import tconstruct.tools.TinkerTools;
import tconstruct.tools.gui.ChestSlot;
import tconstruct.tools.logic.CraftingStationLogic;
import tconstruct.util.network.CraftingStationStackSyncPacket;

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
    private static final int CLICK_MODE_CREATIVE_PICK = 3;
    private static final int CLICK_MODE_DROP = 4;
    private static final int CLICK_MODE_DRAG = 5;
    private static final int CLICK_MODE_COLLECT = 6;

    private final World worldObj;
    private final int posX;
    private final int posY;
    private final int posZ;

    @SuppressWarnings("rawtypes")
    public final WeakReference[] inventories;

    /**
     * The crafting matrix inventory (3x3).
     */
    public InventoryCrafting craftMatrix;

    public IInventory craftResult;
    public CraftingStationLogic logic;
    EntityPlayer player;

    /** Last matched recipe, tried first to avoid rescanning the whole recipe list on every matrix change. */
    public IRecipe lastRecipe;

    /** While true, matrix changes don't recompute the result. Batches ingredient consumption into one lookup. */
    public boolean suppressCraftingUpdates;

    /** Side-inventory preference of the stack currently carried by this container's cursor. */
    public boolean carriedStackPrefersSideInventory;

    /** Last preference mask sent to clients. */
    public int lastSideInventoryPreferences = -1;

    /** Provider-backed side slots whose counts cannot use the vanilla byte-sized update. */
    private final boolean[] extendedStackSlots;

    /** Provider-backed side slot indices, cached because the slot layout cannot change after construction. */
    private final int[] extendedStackSlotIds;

    /** Last oversized stack state sent to each server-side listener. */
    private final Map<EntityPlayerMP, Map<Integer, ItemStack>> extendedStackStates = new HashMap<>();

    /** Slots selected by the client during an extended drag operation. */
    private final Set<Slot> extendedDragSlots = new HashSet<>();

    /** Drag distribution mode used by the current extended drag operation. */
    private int extendedDragMode = -1;

    /** Whether the current extended drag operation has passed its start phase. */
    private boolean extendedDragActive;

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

        this.extendedStackSlots = createExtendedStackSlots();
        this.extendedStackSlotIds = createExtendedStackSlotIds();
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
        Slot slot = this.inventorySlots.get(index);

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

        ItemStack result;
        if (isExtendedStackSlot(slotId) && isExtendedSlotClickMode(mode)) {
            result = handleExtendedSlotClick(slotId, clickedButton, mode, player);
        } else if (hasExtendedStackSlots() && mode == CLICK_MODE_DRAG) {
            result = handleExtendedDrag(slotId, clickedButton, player);
        } else if (hasExtendedStackSlots() && mode == CLICK_MODE_COLLECT) {
            result = handleExtendedCollect(slotId, clickedButton, player);
        } else {
            result = super.slotClick(slotId, clickedButton, mode, player);
        }

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
        suppressOversizedVanillaUpdates();
        super.detectAndSendChanges();

        int sideInventoryPreferences = logic.getSideInventoryPreferences();
        if (lastSideInventoryPreferences != sideInventoryPreferences) {
            for (ICrafting crafter : crafters) {
                crafter.sendProgressBarUpdate(this, SIDE_INVENTORY_PREFERENCES_SYNC_ID, sideInventoryPreferences);
            }
            lastSideInventoryPreferences = sideInventoryPreferences;
        }

        sendExtendedStackUpdates();
    }

    private void sendExtendedStackUpdates() {
        if (worldObj.isRemote) return;

        for (ICrafting crafter : crafters) {
            if (!(crafter instanceof EntityPlayerMP playerMP)) continue;

            Map<Integer, ItemStack> previousStacks = extendedStackStates
                    .computeIfAbsent(playerMP, ignored -> new HashMap<>());
            List<Integer> changedSlotIds = null;
            List<ItemStack> changedStacks = null;

            for (int slotId : extendedStackSlotIds) {
                ItemStack stack = inventorySlots.get(slotId).getStack();
                ItemStack previous = previousStacks.get(slotId);
                if (!isOversized(stack)) {
                    previousStacks.remove(slotId);
                    continue;
                }

                if (!ItemStack.areItemStacksEqual(previous, stack)) {
                    if (changedSlotIds == null) {
                        changedSlotIds = new ArrayList<>();
                        changedStacks = new ArrayList<>();
                    }
                    changedSlotIds.add(slotId);
                    changedStacks.add(stack);
                    previousStacks.put(slotId, stack.copy());
                }
            }

            ItemStack cursorStack = playerMP.inventory.getItemStack();
            ItemStack previousCursorStack = previousStacks.get(-1);
            boolean cursorWasOversized = isOversized(previousCursorStack);
            if (isOversized(cursorStack)) {
                if (!ItemStack.areItemStacksEqual(previousCursorStack, cursorStack)) {
                    if (changedSlotIds == null) {
                        changedSlotIds = new ArrayList<>();
                        changedStacks = new ArrayList<>();
                    }
                    changedSlotIds.add(-1);
                    changedStacks.add(cursorStack);
                    previousStacks.put(-1, cursorStack.copy());
                }
            } else {
                if (cursorWasOversized && !ItemStack.areItemStacksEqual(previousCursorStack, cursorStack)) {
                    if (changedSlotIds == null) {
                        changedSlotIds = new ArrayList<>();
                        changedStacks = new ArrayList<>();
                    }
                    changedSlotIds.add(-1);
                    changedStacks.add(cursorStack);
                }
                previousStacks.remove(-1);
            }

            if (changedSlotIds != null) {
                TConstruct.packetPipeline.sendTo(
                        new CraftingStationStackSyncPacket(
                                windowId,
                                toIntArray(changedSlotIds),
                                changedStacks.toArray(new ItemStack[0])),
                        playerMP);
            }
        }
    }

    private void suppressOversizedVanillaUpdates() {
        for (int slotId : extendedStackSlotIds) {
            ItemStack stack = inventorySlots.get(slotId).getStack();
            if (isOversized(stack) && !ItemStack.areItemStacksEqual(inventoryItemStacks.get(slotId), stack)) {
                inventoryItemStacks.set(slotId, stack.copy());
            }
        }
    }

    private static boolean isOversized(ItemStack stack) {
        return stack != null && stack.stackSize > Byte.MAX_VALUE;
    }

    private static int[] toIntArray(List<Integer> values) {
        int[] result = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    @Override
    public void updateProgressBar(int id, int value) {
        if (id == SIDE_INVENTORY_PREFERENCES_SYNC_ID) {
            logic.setSideInventoryPreferences(value);
        }
    }

    public void updateGridPreferences(ItemStack[] before, ItemStack[] after, boolean[] preferencesBefore,
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
            if (clickedSlot == CRAFTING_RESULT_SLOT && previous != null && previous.getItem() == current.getItem()) {
                // Crafting can return the same tool with different damage or NBT.
                logic.setSideInventoryPreference(slot, preferencesBefore[i]);
                continue;
            }
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

    public static boolean getCarriedStackPreference(ItemStack carriedBefore, ItemStack carriedAfter,
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

    public static boolean collectedPreferredGridStack(ItemStack carriedStack, ItemStack[] before, ItemStack[] after,
            boolean[] preferencesBefore) {
        for (int i = 0; i < before.length; i++) {
            if (preferencesBefore[i] && stackWasCollected(carriedStack, before[i], after[i])) return true;
        }
        return false;
    }

    public static boolean collectedSideInventoryStack(ItemStack carriedStack, ItemStack[] before, ItemStack[] after) {
        if (before == null || after == null) return false;

        for (int i = 0; i < before.length; i++) {
            if (stackWasCollected(carriedStack, before[i], after[i])) return true;
        }
        return false;
    }

    public static boolean stackWasCollected(ItemStack carriedStack, ItemStack before, ItemStack after) {
        if (before == null || !stacksCanMerge(carriedStack, before)) return false;
        return after == null || !stacksCanMerge(before, after) || after.stackSize < before.stackSize;
    }

    public boolean[] copyGridPreferences() {
        boolean[] preferences = new boolean[CRAFTING_GRID_END_SLOT - CRAFTING_GRID_FIRST_SLOT];
        for (int i = 0; i < preferences.length; i++) {
            preferences[i] = logic.prefersSideInventory(CRAFTING_GRID_FIRST_SLOT + i);
        }
        return preferences;
    }

    public ItemStack[] copySlotRange(int start, int end) {
        ItemStack[] stacks = new ItemStack[Math.max(0, end - start)];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = copyStack(inventorySlots.get(start + i).getStack());
        }
        return stacks;
    }

    public ItemStack getSlotStackCopy(int slot) {
        if (slot < 0 || slot >= inventorySlots.size()) return null;
        return copyStack(inventorySlots.get(slot).getStack());
    }

    public static boolean isExtendedSlotClickMode(int mode) {
        return mode == CLICK_MODE_PICKUP || mode == CLICK_MODE_HOTBAR_SWAP
                || mode == CLICK_MODE_CREATIVE_PICK
                || mode == CLICK_MODE_DROP;
    }

    public ItemStack handleExtendedSlotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
        if (slotId < 0 || slotId >= inventorySlots.size()) return null;

        Slot slot = inventorySlots.get(slotId);
        return switch (mode) {
            case CLICK_MODE_PICKUP -> handleExtendedPickup(slot, clickedButton, player);
            case CLICK_MODE_HOTBAR_SWAP -> handleExtendedHotbarSwap(slot, clickedButton, player);
            case CLICK_MODE_CREATIVE_PICK -> handleExtendedCreativePick(slot, player);
            case CLICK_MODE_DROP -> handleExtendedDrop(slot, clickedButton, player);
            default -> null;
        };
    }

    public ItemStack handleExtendedPickup(Slot slot, int clickedButton, EntityPlayer player) {
        InventoryPlayer inventory = player.inventory;
        ItemStack stored = getNonEmptyStack(slot);
        ItemStack carried = inventory.getItemStack();
        ItemStack result = copyStack(stored);

        if (stored == null) {
            if (carried == null || !slot.isItemValid(carried)) return result;

            int amount = clickedButton == 0 ? carried.stackSize : 1;
            amount = Math.min(amount, ExtendedStackLimitHelper.getStackLimit(slot, carried));
            if (amount <= 0) return result;

            ItemStack placed = carried.splitStack(amount);
            slot.putStack(placed);
            slot.onSlotChanged();
            if (carried.stackSize <= 0) inventory.setItemStack(null);
            return result;
        }

        if (!slot.canTakeStack(player)) return result;

        if (carried == null) {
            int transferLimit = getNormalStackLimit(stored);
            int amount = clickedButton == 0 ? transferLimit : transferLimit / 2 + transferLimit % 2;
            ItemStack extracted = slot.decrStackSize(Math.min(stored.stackSize, amount));
            if (extracted != null && extracted.stackSize > 0) {
                inventory.setItemStack(extracted);
                clearEmptySlot(slot);
                slot.onPickupFromSlot(player, extracted);
                slot.onSlotChanged();
            }
            return result;
        }

        if (!slot.isItemValid(carried) || !stacksCanMerge(stored, carried)) return result;

        int limit = ExtendedStackLimitHelper.getStackLimit(slot, stored);
        int available = Math.max(0, limit - stored.stackSize);
        int amount = clickedButton == 0 ? Math.min(carried.stackSize, available) : Math.min(1, available);
        if (amount > 0) {
            stored.stackSize += amount;
            carried.stackSize -= amount;
            slot.putStack(stored);
            slot.onSlotChanged();
            if (carried.stackSize <= 0) inventory.setItemStack(null);
            return result;
        }

        if (clickedButton == 0) {
            int cursorSpace = Math.max(0, getNormalStackLimit(carried) - carried.stackSize);
            int amountToTake = Math.min(stored.stackSize, cursorSpace);
            if (amountToTake > 0) {
                ItemStack extracted = slot.decrStackSize(amountToTake);
                if (extracted != null && extracted.stackSize > 0) {
                    carried.stackSize += extracted.stackSize;
                    clearEmptySlot(slot);
                    slot.onPickupFromSlot(player, extracted);
                    slot.onSlotChanged();
                }
            }
        }
        return result;
    }

    private ItemStack handleExtendedDrag(int slotId, int clickedButton, EntityPlayer player) {
        int dragEvent = Container.func_94532_c(clickedButton);
        if (dragEvent == 0) {
            extendedDragSlots.clear();
            extendedDragMode = Container.func_94529_b(clickedButton);
            extendedDragActive = player.inventory.getItemStack() != null && Container.func_94528_d(extendedDragMode);
            return null;
        }

        if (dragEvent == 1) {
            ItemStack carried = player.inventory.getItemStack();
            if (!extendedDragActive || carried == null || carried.stackSize <= extendedDragSlots.size()) {
                resetExtendedDrag();
                return null;
            }

            Slot slot = getSlotIfPresent(slotId);
            if (canAddExtendedDragSlot(slot, carried)) extendedDragSlots.add(slot);
            return null;
        }

        if (dragEvent == 2) {
            if (!extendedDragActive) return null;

            ItemStack carried = player.inventory.getItemStack();
            if (carried != null) {
                ItemStack template = carried.copy();
                int remaining = carried.stackSize;
                Set<Slot> selectedSlots = new HashSet<>(extendedDragSlots);
                int selectedSlotCount = selectedSlots.size();
                for (Slot slot : selectedSlots) {
                    if (remaining < selectedSlotCount || !canAddExtendedDragSlot(slot, carried)) continue;

                    ItemStack existing = getNonEmptyStack(slot);
                    int existingSize = existing == null ? 0 : existing.stackSize;
                    int targetSize = getDraggedStackSize(
                            template.stackSize,
                            extendedDragMode,
                            selectedSlotCount,
                            existingSize,
                            getStorageLimit(slot, template));
                    int moved = targetSize - existingSize;
                    if (moved <= 0) continue;

                    ItemStack placed = template.copy();
                    placed.stackSize = targetSize;
                    slot.putStack(placed);
                    slot.onSlotChanged();
                    remaining -= moved;
                }

                template.stackSize = remaining;
                player.inventory.setItemStack(remaining > 0 ? template : null);
            }

            resetExtendedDrag();
            return null;
        }

        resetExtendedDrag();
        return null;
    }

    private ItemStack handleExtendedCollect(int slotId, int clickedButton, EntityPlayer player) {
        if (slotId < 0) return null;

        Slot clickedSlot = getSlotIfPresent(slotId);
        if (clickedSlot != null && getNonEmptyStack(clickedSlot) != null && clickedSlot.canTakeStack(player))
            return null;

        ItemStack carried = player.inventory.getItemStack();
        if (carried == null || carried.stackSize >= carried.getMaxStackSize()) return null;

        boolean changed = false;
        int firstIndex = clickedButton == 0 ? 0 : inventorySlots.size() - 1;
        int step = clickedButton == 0 ? 1 : -1;
        for (int pass = 0; pass < 2 && carried.stackSize < carried.getMaxStackSize(); pass++) {
            for (int index = firstIndex; index >= 0 && index < inventorySlots.size()
                    && carried.stackSize < carried.getMaxStackSize(); index += step) {
                Slot slot = inventorySlots.get(index);
                ItemStack stored = getNonEmptyStack(slot);
                if (stored == null || !slot.canTakeStack(player)
                        || !func_94530_a(carried, slot)
                        || !stacksCanMerge(carried, stored))
                    continue;

                int limit = getStorageLimit(slot, carried);
                if (pass == 0 && stored.stackSize >= limit) continue;

                int amount = Math.min(carried.getMaxStackSize() - carried.stackSize, stored.stackSize);
                ItemStack extracted = slot.decrStackSize(amount);
                if (extracted == null || extracted.stackSize <= 0) continue;

                carried.stackSize += extracted.stackSize;
                clearEmptySlot(slot);
                slot.onPickupFromSlot(player, extracted);
                slot.onSlotChanged();
                changed = true;
            }
        }

        if (changed) detectAndSendChanges();
        return null;
    }

    private boolean canAddExtendedDragSlot(Slot slot, ItemStack carried) {
        if (slot == null || !slot.isItemValid(carried) || !func_94530_a(carried, slot) || !canDragIntoSlot(slot))
            return false;

        ItemStack existing = getNonEmptyStack(slot);
        return existing == null
                || stacksCanMerge(carried, existing) && existing.stackSize < getStorageLimit(slot, carried);
    }

    private static int getDraggedStackSize(int carriedSize, int dragMode, int slotCount, int existingSize, int limit) {
        long requested = dragMode == 0 ? carriedSize / (long) slotCount : 1L;
        long target = Math.min((long) existingSize + requested, limit);
        return target > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) target;
    }

    private Slot getSlotIfPresent(int slotId) {
        return slotId < 0 || slotId >= inventorySlots.size() ? null : inventorySlots.get(slotId);
    }

    private void resetExtendedDrag() {
        extendedDragSlots.clear();
        extendedDragMode = -1;
        extendedDragActive = false;
    }

    public ItemStack handleExtendedHotbarSwap(Slot slot, int hotbarIndex, EntityPlayer player) {
        if (hotbarIndex < 0 || hotbarIndex >= 9) return null;

        InventoryPlayer inventory = player.inventory;
        ItemStack stored = getNonEmptyStack(slot);
        ItemStack hotbar = inventory.getStackInSlot(hotbarIndex);
        ItemStack result = copyStack(stored);

        if (stored == null) {
            if (hotbar == null || !slot.isItemValid(hotbar)) return result;

            int amount = Math.min(hotbar.stackSize, ExtendedStackLimitHelper.getStackLimit(slot, hotbar));
            if (amount <= 0) return result;

            ItemStack placed = hotbar.splitStack(amount);
            slot.putStack(placed);
            inventory.setInventorySlotContents(hotbarIndex, hotbar.stackSize > 0 ? hotbar : null);
            slot.onSlotChanged();
            return result;
        }

        if (!slot.canTakeStack(player)) return result;

        if (hotbar == null) {
            ItemStack extracted = slot.decrStackSize(getNormalStackLimit(stored));
            if (extracted != null && extracted.stackSize > 0) {
                inventory.setInventorySlotContents(hotbarIndex, extracted);
                clearEmptySlot(slot);
                slot.onPickupFromSlot(player, extracted);
                slot.onSlotChanged();
            }
            return result;
        }

        if (stacksCanMerge(stored, hotbar) && slot.isItemValid(hotbar)) {
            int available = Math.max(0, getNormalStackLimit(hotbar) - hotbar.stackSize);
            int amount = Math.min(stored.stackSize, available);
            if (amount > 0) {
                ItemStack extracted = slot.decrStackSize(amount);
                if (extracted != null && extracted.stackSize > 0) {
                    hotbar.stackSize += extracted.stackSize;
                    inventory.setInventorySlotContents(hotbarIndex, hotbar);
                    clearEmptySlot(slot);
                    slot.onPickupFromSlot(player, extracted);
                    slot.onSlotChanged();
                }
            }
            return result;
        }

        int emptySlot = inventory.getFirstEmptyStack();
        if (emptySlot < 0) return result;

        ItemStack extracted = slot.decrStackSize(getNormalStackLimit(stored));
        if (extracted != null && extracted.stackSize > 0) {
            inventory.setInventorySlotContents(emptySlot, hotbar);
            inventory.setInventorySlotContents(hotbarIndex, extracted);
            clearEmptySlot(slot);
            slot.onPickupFromSlot(player, extracted);
            slot.onSlotChanged();
        }
        return result;
    }

    public ItemStack handleExtendedCreativePick(Slot slot, EntityPlayer player) {
        InventoryPlayer inventory = player.inventory;
        ItemStack stored = getNonEmptyStack(slot);
        if (stored == null || !player.capabilities.isCreativeMode || inventory.getItemStack() != null) {
            return copyStack(stored);
        }

        ItemStack picked = stored.copy();
        picked.stackSize = getNormalStackLimit(picked);
        inventory.setItemStack(picked);
        return stored.copy();
    }

    public ItemStack handleExtendedDrop(Slot slot, int clickedButton, EntityPlayer player) {
        InventoryPlayer inventory = player.inventory;
        ItemStack stored = getNonEmptyStack(slot);
        ItemStack result = copyStack(stored);
        if (stored == null || inventory.getItemStack() != null || !slot.canTakeStack(player)) return result;

        int amount = clickedButton == 0 ? 1 : getNormalStackLimit(stored);
        ItemStack dropped = slot.decrStackSize(amount);
        if (dropped != null && dropped.stackSize > 0) {
            clearEmptySlot(slot);
            slot.onPickupFromSlot(player, dropped);
            player.dropPlayerItemWithRandomChoice(dropped, true);
            slot.onSlotChanged();
        }
        return result;
    }

    public static void clearEmptySlot(Slot slot) {
        ItemStack stack = slot.getStack();
        if (stack != null && stack.stackSize <= 0) {
            slot.putStack(null);
        }
    }

    public static int getNormalStackLimit(ItemStack stack) {
        return Math.max(0, stack.getMaxStackSize());
    }

    public static ItemStack copyStack(ItemStack stack) {
        return stack == null ? null : stack.copy();
    }

    public static boolean stacksCanMerge(ItemStack first, ItemStack second) {
        return first != null && second != null
                && first.getItem() == second.getItem()
                && (!first.getHasSubtypes() || first.getItemDamage() == second.getItemDamage())
                && ItemStack.areItemStackTagsEqual(first, second);
    }

    public static boolean isCraftingGridSlot(int slot) {
        return slot >= CRAFTING_GRID_FIRST_SLOT && slot < CRAFTING_GRID_END_SLOT;
    }

    public boolean isSideInventorySlot(int slot) {
        return slot >= SIDE_INVENTORY_FIRST_SLOT && slot < inventorySlots.size();
    }

    private boolean[] createExtendedStackSlots() {
        boolean[] extendedSlots = new boolean[inventorySlots.size()];
        for (int slotId = SIDE_INVENTORY_FIRST_SLOT; slotId < inventorySlots.size(); slotId++) {
            Slot slot = inventorySlots.get(slotId);
            extendedSlots[slotId] = ExtendedStackLimitHelper.hasExtendedStackLimit(slot);
        }
        return extendedSlots;
    }

    private int[] createExtendedStackSlotIds() {
        int count = 0;
        for (boolean extended : extendedStackSlots) {
            if (extended) count++;
        }

        int[] slotIds = new int[count];
        int index = 0;
        for (int slotId = SIDE_INVENTORY_FIRST_SLOT; slotId < extendedStackSlots.length; slotId++) {
            if (extendedStackSlots[slotId]) slotIds[index++] = slotId;
        }
        return slotIds;
    }

    public boolean isExtendedStackSlot(int slot) {
        return slot >= 0 && slot < extendedStackSlots.length && extendedStackSlots[slot];
    }

    private boolean hasExtendedStackSlots() {
        return extendedStackSlotIds.length > 0;
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
    public ItemStack findMatchingRecipeCached() {
        // Vanilla's two-item tool repair takes precedence over the recipe list
        if (isVanillaToolRepair()) {
            return CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
        }

        IRecipe cached = this.lastRecipe;
        if (cached != null && cached.matches(this.craftMatrix, this.worldObj)) {
            return cached.getCraftingResult(this.craftMatrix);
        }
        this.lastRecipe = null;

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
    public boolean isVanillaToolRepair() {
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
    public void beginBatchCraftingUpdate() {
        suppressCraftingUpdates = true;
    }

    /** Re-enables result updates and recomputes once. */
    public void endBatchCraftingUpdate() {
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
                otherInventorySlot = this.inventorySlots.get(slotIndex);
                copyStack = getNonEmptyStack(otherInventorySlot);

                if (copyStack == null && otherInventorySlot.isItemValid(stack)) {
                    int limit = ExtendedStackLimitHelper.getStackLimit(otherInventorySlot, stack);
                    int amount = Math.min(stack.stackSize, limit);
                    if (amount > 0) {
                        ItemStack placed = stack.copy();
                        placed.stackSize = amount;
                        if (placed.hasTagCompound() && placed.getItem() instanceof IModifyable modifyable) {
                            placed.getTagCompound().getCompoundTag(modifyable.getBaseTagName()).removeTag("ToRemove");
                        }
                        otherInventorySlot.putStack(placed);
                        otherInventorySlot.onSlotChanged();
                        stack.stackSize -= amount;
                        failedToMerge = true;
                    }
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

    private static ItemStack getNonEmptyStack(Slot slot) {
        if (slot == null) return null;

        ItemStack stack = slot.getStack();
        return stack == null || stack.stackSize <= 0 ? null : stack;
    }

    private static int getStorageLimit(Slot slot, ItemStack stack) {
        return ExtendedStackLimitHelper.getStackLimit(slot, stack);
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
                slot = this.inventorySlots.get(k);
                itemstack1 = getNonEmptyStack(slot);

                if (itemstack1 != null && itemstack1.getItem() == stack.getItem()
                        && (!stack.getHasSubtypes() || stack.getItemDamage() == itemstack1.getItemDamage())
                        && ItemStack.areItemStackTagsEqual(stack, itemstack1)
                        && this.func_94530_a /* canMergeSlot */(stack, slot)) {
                    int limit = ExtendedStackLimitHelper.getStackLimit(slot, stack);
                    int space = limit - itemstack1.stackSize;
                    int amount = Math.min(stack.stackSize, Math.max(0, space));

                    if (amount > 0) {
                        stack.stackSize -= amount;
                        itemstack1.stackSize += amount;
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
            final Slot slot = this.inventorySlots.get(k);
            ItemStack itemstack1 = getNonEmptyStack(slot);

            if (itemstack1 == null && slot.isItemValid(stack) && this.func_94530_a /* canMergeSlot */(stack, slot)) {
                int limit = ExtendedStackLimitHelper.getStackLimit(slot, stack);
                int amount = Math.min(stack.stackSize, limit);
                if (amount > 0) {
                    ItemStack stack2 = stack.copy();
                    stack2.stackSize = amount;
                    stack.stackSize -= amount;
                    slot.putStack(stack2);
                    slot.onSlotChanged();
                    didSomething = true;
                }

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
