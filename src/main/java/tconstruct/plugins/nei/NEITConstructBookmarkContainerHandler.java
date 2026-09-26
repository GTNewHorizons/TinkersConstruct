package tconstruct.plugins.nei;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import codechicken.nei.api.IBookmarkContainerHandler;
import tconstruct.tools.inventory.CraftingStationContainer;
import tconstruct.tools.logic.CraftingStationLogic;

public class NEITConstructBookmarkContainerHandler implements IBookmarkContainerHandler {

    protected static Minecraft mc = Minecraft.getMinecraft();
    final int magicNumber = (5 * 9) + 1; // First 46 slots are player inventory in the station, including crafting field
                                         // and product

    @Override
    public void pullBookmarkItemsFromContainer(GuiContainer guiContainer, ArrayList<ItemStack> bookmarkItems) {
        CraftingStationLogic logic = ((CraftingStationContainer) guiContainer.inventorySlots).logic;
        if (logic.getFirstInventory() == null) {
            return;
        }

        Container container = guiContainer.inventorySlots;
        for (ItemStack bookmarkItem : bookmarkItems) {
            int remaining = bookmarkItem.stackSize;
            for (int i = magicNumber; i < container.inventorySlots.size() && remaining > 0; i++) {
                ItemStack containerItem = container.getSlot(i).getStack();
                if (containerItem != null && bookmarkItem.isItemEqual(containerItem)) {
                    remaining -= moveItems(guiContainer, i, Math.min(remaining, containerItem.stackSize));
                }
            }
        }
    }

    private int moveItems(GuiContainer container, int fromSlot, int transferAmount) {
        Slot source = container.inventorySlots.getSlot(fromSlot);
        ItemStack sourceStack = source.getStack();
        if (sourceStack == null || !source.canTakeStack(mc.thePlayer)) {
            return 0;
        }

        if (!source.isItemValid(sourceStack)) {
            // The remainder cannot be put back into an extraction-only slot, so move the whole stack.
            int toSlot = findValidPlayerInventoryDestination(container.inventorySlots, fromSlot, sourceStack.stackSize);
            if (toSlot == -1) {
                return 0;
            }
            int moved = sourceStack.stackSize;
            clickSlot(container, fromSlot, 0);
            clickSlot(container, toSlot, 0);
            return moved;
        }

        int moved = 0;
        for (int i = 0; i < transferAmount; i++) {
            int toSlot = findValidPlayerInventoryDestination(container.inventorySlots, fromSlot, 1);
            if (toSlot == -1) {
                break;
            }
            clickSlot(container, fromSlot, 0);
            clickSlot(container, toSlot, 1);
            clickSlot(container, fromSlot, 0);
            moved++;
        }
        return moved;
    }

    private void clickSlot(GuiContainer container, int slotIdx, int button) {
        mc.playerController.windowClick(container.inventorySlots.windowId, slotIdx, button, 0, mc.thePlayer);
    }

    private int findValidPlayerInventoryDestination(Container container, int fromSlot, int amount) {
        ItemStack stackToMove = container.getSlot(fromSlot).getStack();
        if (stackToMove == null) {
            return -1;
        }
        for (int i = 10; i < magicNumber; i++) { // 10 comes from the magic number including crafting field + product
            Slot destination = container.getSlot(i);
            ItemStack toStack = destination.getStack();
            int capacity = Math.min(destination.getSlotStackLimit(), stackToMove.getMaxStackSize())
                    - (toStack == null ? 0 : toStack.stackSize);
            if (capacity >= amount && (toStack == null
                    || toStack.isItemEqual(stackToMove) && ItemStack.areItemStackTagsEqual(toStack, stackToMove))) {
                return i;
            }
        }
        return -1;
    }
}
