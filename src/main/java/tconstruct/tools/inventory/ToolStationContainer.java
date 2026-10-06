package tconstruct.tools.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import tconstruct.TConstruct;
import tconstruct.library.event.ToolCraftedEvent;
import tconstruct.library.modifier.IModifyable;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.TinkerTools;
import tconstruct.tools.logic.ToolStationLogic;
import tconstruct.util.network.ToolStationSelectionPacket;

public class ToolStationContainer extends TinkerStationContainer {

    private static final int INVENTORY_X = 118;
    private static final int INVENTORY_Y = 114;

    public InventoryPlayer invPlayer;
    public Slot[] slots;
    public SlotTool toolSlot;
    public Random random = new Random();
    /** The open tab's tool; null on Repair & Modify. */
    public ToolCore selectedTool;
    /** A tab clicked here outranks the server's word on which tab the screen opened on. */
    private boolean tabPicked;

    public ToolStationContainer(InventoryPlayer inventoryplayer, ToolStationLogic builderlogic) {
        initializeContainer(inventoryplayer, builderlogic);
        // the client's copy of the tile may be behind; the server corrects the tab in addCraftingToCrafters
        selectTool(logic.tabs.tabOf(inventoryplayer.player), false);
    }

    public void initializeContainer(InventoryPlayer inventoryplayer, ToolStationLogic builderlogic) {
        invPlayer = inventoryplayer;
        logic = builderlogic;

        toolSlot = new SlotTool(this, builderlogic, 0, 234, 38);
        this.addSlotToContainer(toolSlot);
        slots = new Slot[] { new SlotToolStationIn(this, builderlogic, 1, 167, 29),
                new SlotToolStationIn(this, builderlogic, 2, 149, 38),
                new SlotToolStationIn(this, builderlogic, 3, 167, 47),
                new SlotToolStationIn(this, builderlogic, 4, 149, 47),
                new SlotToolStationIn(this, builderlogic, 5, 149, 29),
                new SlotToolStationIn(this, builderlogic, 6, 167, 38) };

        for (int iter = 0; iter < slots.length; iter++) this.addSlotToContainer(slots[iter]);

        addPlayerInventory(inventoryplayer, INVENTORY_X, INVENTORY_Y);
    }

    protected void addPlayerInventory(InventoryPlayer playerInventory, int xCorner, int yCorner) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(
                        new Slot(playerInventory, col + row * 9 + 9, xCorner + col * 18, yCorner + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(playerInventory, col, xCorner + col * 18, yCorner + 58));
        }
    }

    /** A tab click; every stack the tab refuses goes back to the player. */
    public void selectTool(ToolCore tool) {
        tabPicked = true;
        selectTool(tool, true);
    }

    /** The tab the server opened this screen on. */
    public void selectOpeningTab(ToolCore tool) {
        if (!tabPicked && tool != selectedTool) selectTool(tool, false);
    }

    private void selectTool(ToolCore tool, boolean handBack) {
        selectedTool = tool;
        if (logic.setSelectedTool(tool, invPlayer.player) && !invPlayer.player.worldObj.isRemote) logic.markDirty();
        if (handBack) {
            for (int i = 1; tool != null && i < logic.getSizeInventory(); i++) {
                ItemStack stack = logic.getStackInSlot(i);
                if (stack != null && !slots[i - 1].isItemValid(stack)) transferStackInSlot(invPlayer.player, i);
            }
        }
        logic.buildTool(logic.toolName);
        logic.syncContentsToClients();
    }

    public boolean isRestricting() {
        return selectedTool != null;
    }

    public boolean isPartSlot(int index) {
        return isRestricting() && logic.isPartSlot(logic.partsOf(selectedTool), index);
    }

    /** A slot the build does not read stays active while something is in it, so the player can take it out. */
    public boolean isActiveSlot(int index) {
        return !isRestricting() || logic.getStackInSlot(index) != null || isPartSlot(index);
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        Block block = logic.getWorldObj().getBlock(logic.xCoord, logic.yCoord, logic.zCoord);
        if (block != TinkerTools.toolStationWood && block != TinkerTools.craftingSlabWood) return false;
        return logic.isUseableByPlayer(entityplayer);
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafter) {
        super.addCraftingToCrafters(crafter);
        if (crafter instanceof EntityPlayerMP player) {
            TConstruct.packetPipeline.sendTo(new ToolStationSelectionPacket(selectedTool), player);
        }
    }

    boolean topUpPartSlots(ItemStack stack, int startIndex, int endIndex) {
        if (!stack.isStackable()) return false;
        List<Slot> holders = new ArrayList<>();
        for (int k = startIndex; k < endIndex; k++) {
            Slot slot = (Slot) this.inventorySlots.get(k);
            if (isPartSlot(slot.getSlotIndex()) && CraftingStationContainer.stacksCanMerge(slot.getStack(), stack)
                    && takesByShiftClick(slot, stack)) {
                holders.add(slot);
            }
        }
        boolean merged = false;
        while (stack.stackSize > 0) {
            Slot fewest = null;
            for (Slot slot : holders) {
                int count = slot.getStack().stackSize;
                if (count < Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit())
                        && (fewest == null || count < fewest.getStack().stackSize)) {
                    fewest = slot;
                }
            }
            if (fewest == null) break;
            fewest.getStack().stackSize++;
            stack.stackSize--;
            merged = true;
        }
        if (merged) for (Slot slot : holders) slot.onSlotChanged();
        return merged;
    }

    public void onResultTaken(EntityPlayer player, ItemStack stack) {
        boolean full = logic.takeCraftInputs(stack);
        if (!logic.getWorldObj().isRemote && full) playCraftSound(player);
        if (stack.getItem() instanceof IModifyable) {
            MinecraftForge.EVENT_BUS.post(new ToolCraftedEvent(logic, player, stack));
        }
    }

    protected void playCraftSound(EntityPlayer player) {
        logic.getWorldObj().playSoundEffect(
                logic.xCoord + 0.5D,
                logic.yCoord + 0.5D,
                logic.zCoord + 0.5D,
                "tinker:little_saw",
                1.0F,
                (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
    }
}
