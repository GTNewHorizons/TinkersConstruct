package tconstruct.smeltery.inventory;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import tconstruct.TConstruct;
import tconstruct.smeltery.TinkerSmeltery;
import tconstruct.smeltery.gui.SmelteryGui;
import tconstruct.smeltery.logic.SmelteryLogic;
import tconstruct.util.network.SmelteryGuiPacket;

public class SmelteryContainer extends ActiveContainer {

    public SmelteryLogic logic;
    public InventoryPlayer playerInv;
    private int updateTicks;
    private int[] lastHeatRuns;
    private FluidStack lastFuel;
    private int lastFuelCapacity;
    private final byte[] heatLevels;
    private int slotRow;
    public int columns;
    public final int smelterySize;

    public SmelteryContainer(InventoryPlayer inventoryplayer, SmelteryLogic smeltery) {
        logic = smeltery;
        playerInv = inventoryplayer;
        slotRow = 0;
        columns = smeltery.getBlocksPerLayer() >= 16 ? 4 : 3;
        smelterySize = smeltery.getBlockCapacity();
        heatLevels = new byte[smelterySize];

        /* Smeltery inventory */

        // new rectangular smeltery
        int totalSlots = smeltery.getBlockCapacity();
        int y = 0;

        for (int i = 0; i < totalSlots; i++) {
            int x = i % columns;
            this.addDualSlotToContainer(new ActiveSlot(smeltery, x + y * columns, 2 + x * 22, 8 + y * 18, y < 8));
            if (x == columns - 1) y++;
        }

        int baseX = 90 + (columns - 3) * 22;

        /* Player inventory */
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(
                        new Slot(inventoryplayer, column + row * 9 + 9, baseX + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(inventoryplayer, column, baseX + column * 18, 142));
        }
    }

    public int updateRows(int invRow) {
        if (invRow != slotRow) {
            slotRow = invRow;
            // TConstruct.logger.info(invRow);
            int basePos = invRow * columns;
            for (int iter = 0; iter < activeInventorySlots.size(); iter++) {
                ActiveSlot slot = activeInventorySlots.get(iter);
                slot.setActive(
                        slot.activeSlotNumber >= basePos
                                && slot.activeSlotNumber < basePos + columns * SmelteryGui.maxRows);

                int xPos = (iter - basePos) % columns;
                int yPos = (iter - basePos) / columns;
                slot.xDisplayPosition = 2 + 22 * xPos;
                slot.yDisplayPosition = 8 + 18 * yPos;
            }
            return slotRow;
        }
        return -1;
    }

    public int scrollTo(float scrollPos) {
        int slots = SmelteryGui.maxRows * columns;
        float total = (logic.getSizeInventory() - slots) / columns;
        if ((logic.getSizeInventory() - slots) % columns != 0) total++;
        int rowPos = Math.round(total * scrollPos);
        return updateRows(rowPos);
    }

    @Override
    public void detectAndSendChanges() {
        // The GUI closes when the structure changes size. Its old Slot objects must not read the resized inventory.
        if (smelterySize != logic.getBlockCapacity() || updateTicks++ % 5 != 0) return;
        super.detectAndSendChanges();
        if (crafters.isEmpty()) return;
        logic.updateFuelDisplay();
        int[] heatRuns = getHeatRuns();
        FluidStack fuel = logic.getFuel();
        if (!Arrays.equals(heatRuns, lastHeatRuns) || !fuel.isFluidStackIdentical(lastFuel)
                || logic.fuelCapacity != lastFuelCapacity) {
            for (Object crafter : crafters) {
                if (crafter instanceof EntityPlayerMP player) {
                    TConstruct.packetPipeline
                            .sendTo(new SmelteryGuiPacket(this, heatRuns, fuel, logic.fuelCapacity), player);
                }
            }
            lastHeatRuns = heatRuns;
            lastFuel = fuel;
            lastFuelCapacity = logic.fuelCapacity;
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafter) {
        super.addCraftingToCrafters(crafter);
        if (crafter instanceof EntityPlayerMP player) {
            logic.updateFuelDisplay();
            TConstruct.packetPipeline
                    .sendTo(new SmelteryGuiPacket(this, getHeatRuns(), logic.getFuel(), logic.fuelCapacity), player);
        }
    }

    private int[] getHeatRuns() {
        IntArrayList runs = new IntArrayList();
        int previous = -1;
        for (int i = 0; i < smelterySize; i++) {
            int temperature = logic.getTempForSlot(i) - 20;
            int target = logic.getMeltingPointForSlot(i) - 20;
            int level = temperature > 0 && target > 0 ? Math.max(1, Math.min(16, 16 * temperature / target)) : 0;
            int run = ((i + 1) << 5) | level;
            if (level == previous) runs.set(runs.size() - 1, run);
            else runs.add(run);
            previous = level;
        }
        return runs.toIntArray();
    }

    public void updateGuiState(int[] heatRuns, FluidStack fuel, int fuelCapacity) {
        int start = 0;
        for (int run : heatRuns) {
            int end = run >>> 5;
            Arrays.fill(heatLevels, start, end, (byte) (run & 31));
            start = end;
        }
        logic.setFuelDisplay(fuel, fuelCapacity);
    }

    public int getHeatLevel(int slot) {
        return heatLevels[slot];
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        Block block = logic.getWorldObj().getBlock(logic.xCoord, logic.yCoord, logic.zCoord);
        if (block != TinkerSmeltery.smeltery && block != TinkerSmeltery.smelteryNether) return false;
        return logic.isUseableByPlayer(entityplayer);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
        ItemStack stack = null;
        Slot slot = (Slot) this.inventorySlots.get(slotID);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            stack = slotStack.copy();

            if (slotID < smelterySize) {
                if (!this.mergeItemStack(slotStack, logic.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!this.mergeItemStack(slotStack, 0, smelterySize, false)) {
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

    @Override
    protected boolean mergeItemStack(ItemStack inputStack, int startSlot, int endSlot, boolean flag) {
        // TConstruct.logger.info("Merge");
        boolean merged = false;
        int slotPos = startSlot;

        if (flag) {
            slotPos = endSlot - 1;
        }

        Slot slot;
        ItemStack slotStack;

        /*
         * if (inputStack.isStackable() && startSlot >= logic.getSizeInventory()) { TConstruct.logger.info("Rawr!");
         * while (inputStack.stackSize > 0 && (!flag && slotPos < endSlot || flag && slotPos >= startSlot)) { slot =
         * (Slot) this.inventorySlots.get(slotPos); slotStack = slot.getStack(); if (slotStack != null &&
         * ItemStack.areItemStacksEqual(inputStack, slotStack) && !inputStack.getHasSubtypes()) { int totalSize =
         * slotStack.stackSize + inputStack.stackSize; if (totalSize <= inputStack.getMaxStackSize()) {
         * inputStack.stackSize = 0; slotStack.stackSize = totalSize; slot.onSlotChanged(); merged = true; } else if
         * (slotStack.stackSize < inputStack.getMaxStackSize()) { inputStack.stackSize -= inputStack.getMaxStackSize() -
         * slotStack.stackSize; slotStack.stackSize = inputStack.getMaxStackSize(); slot.onSlotChanged(); merged = true;
         * } } if (flag) { --slotPos; } else { ++slotPos; } } }
         */

        if (inputStack.isStackable() && startSlot >= logic.getSizeInventory()) {
            while (inputStack.stackSize > 0 && (!flag && slotPos < endSlot || flag && slotPos >= startSlot)) {
                slot = (Slot) this.inventorySlots.get(slotPos);
                slotStack = slot.getStack();

                if (slotStack != null && slotStack.isItemEqual(inputStack)
                        && ItemStack.areItemStackTagsEqual(slotStack, inputStack)) {
                    int l = slotStack.stackSize + inputStack.stackSize;

                    if (l <= inputStack.getMaxStackSize()) {
                        inputStack.stackSize = 0;
                        slotStack.stackSize = l;
                        slot.onSlotChanged();
                        merged = true;
                    } else if (slotStack.stackSize < inputStack.getMaxStackSize()) {
                        inputStack.stackSize -= inputStack.getMaxStackSize() - slotStack.stackSize;
                        slotStack.stackSize = inputStack.getMaxStackSize();
                        slot.onSlotChanged();
                        merged = true;
                    }
                }

                if (flag) {
                    --slotPos;
                } else {
                    ++slotPos;
                }
            }
        }

        if (inputStack.stackSize > 0) {
            if (flag) {
                slotPos = endSlot - 1;
            } else {
                slotPos = startSlot;
            }

            while (!flag && slotPos < endSlot || flag && slotPos >= startSlot) {
                slot = (Slot) this.inventorySlots.get(slotPos);
                slotStack = slot.getStack();

                if (slotStack == null) {
                    slot.putStack(inputStack.copy());
                    slot.onSlotChanged();
                    inputStack.stackSize -= 1;
                    merged = true;
                    break;
                }

                if (flag) {
                    --slotPos;
                } else {
                    ++slotPos;
                }
            }
        }

        return merged;
    }
}
